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

package controllers

import com.kenshoo.play.metrics.Metrics
import mocks._
import models.response.NrsResponse
import org.joda.time.DateTime
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import uk.gov.hmrc.http.HeaderCarrier
import utils.DateUtils

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class NrsControllerSpec
  extends ControllerBaseSpec
    with MockCurrentDateTime
    with MockNrsService
    with MockIdGenerator {

  val date: DateTime = DateTime.parse("2017-01-01T00:00:00.000Z")
  val fmt: String = DateUtils.dateTimePattern
  val nino: String = "AA12345678"
  val notableEvent: String = "submit"
  val correlationId: String = "X-ID"
  val uid: String = "a5894863-9cd7-4d0d-9eee-301ae79cbae6"
  val periodKey: String = "A1A2"

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val mockedMetrics: Metrics = new MockMetrics

    val controller: NrsController = new NrsController(
      nrsService = mockNrsService,
      cc = cc,
      dateTime = mockCurrentDateTime,
      idGenerator = mockIdGenerator
    )

    MockCurrentDateTime.getCurrentDate.returns(date).anyNumberOfTimes()
    MockIdGenerator.getUid.returns(uid).once()
    MockIdGenerator.getUid.returns(correlationId).anyNumberOfTimes()
  }

  val submitRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "finalised": true
      |}
      |""".stripMargin
  )

  "submitReturn" when {
    "a valid request is supplied" should {
      "return the expected data on a successful service call" in new Test {

        MockNrsService
          .submit(nino, notableEvent, submitRequestBodyJson, uid, date)
          .returns(Future.successful(Some(NrsResponse("submissionId"))))

        private val result: Future[Result] = controller.submit(nino, "submit")(fakePostRequest(submitRequestBodyJson))

        status(result) shouldBe OK
      }
    }
  }
}
