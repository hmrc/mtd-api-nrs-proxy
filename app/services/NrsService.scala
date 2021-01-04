/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package services

import com.kenshoo.play.metrics.Metrics
import connectors.{NrsConnector, NrsOutcome}
import javax.inject.{Inject, Singleton}
import models.request.{Metadata, NrsSubmission, SearchKeys}
import models.response.NrsResponse
import org.joda.time.DateTime
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Request
import uk.gov.hmrc.http.HeaderCarrier
import utils.{HashUtil, Logging, Timer}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class NrsService @Inject()(connector: NrsConnector,
                           hashUtil: HashUtil,
                           override val metrics: Metrics) extends Timer with Logging {

  def submit(nino: String, notableEvent: String, body: JsValue, generatedNrsId: String, submissionTimestamp: DateTime)(
    implicit request: Request[_],
    hc: HeaderCarrier,
    ec: ExecutionContext,
    correlationId: String): Future[Option[NrsResponse]] = {

    val nrsSubmission = buildNrsSubmission(nino, notableEvent, body, submissionTimestamp, request)

    def audit(resp: NrsOutcome): Future[Unit] = resp match {
      case Left(err) =>
        logger.info(s"Error occurred in NRS Submission :: $err")
        Future.successful((): Unit)
      case Right(_) => logger.info(s"NRS Submission is successful")
        Future.successful((): Unit)

    }

    timeFuture("NRS Submission", "nrs.submission") {
      connector.submit(nrsSubmission).map { response =>
        audit(response)
        response.toOption
      }
    }
  }

  def buildNrsSubmission(nino: String,
                         notableEvent: String,
                         body: JsValue,
                         submissionTimestamp: DateTime,
                         request: Request[_]): NrsSubmission = {

    val payloadString = body.toString()
    val encodedPayload = hashUtil.encode(payloadString)
    val sha256Checksum = hashUtil.getHash(payloadString)

    NrsSubmission(
      payload = encodedPayload,
      Metadata(
        businessId = "itsa",
        notableEvent = s"$notableEvent",
        payloadContentType = "application/json",
        payloadSha256Checksum = sha256Checksum,
        userSubmissionTimestamp = submissionTimestamp,
        identityData = None,
        userAuthToken = request.headers.get("Authorization").get,
        headerData = Json.toJson(request.headers.toMap.map { h => h._1 -> h._2.head }),
        searchKeys =
          SearchKeys(
            nino = Some(nino),
            companyName = None
          )
      )
    )
  }


}
