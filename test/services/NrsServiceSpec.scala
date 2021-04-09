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
import controllers.UserRequest
import mocks.{MockMetrics, MockNrsConnector}
import models.auth.UserDetails
import models.request.{Metadata, NrsSubmission, SearchKeys}
import models.response.{NrsFailure, NrsResponse}
import org.joda.time.DateTime
import play.api.libs.json.Json
import play.api.test.FakeRequest
import utils.NrsTestData.IdentityDataTestData
import utils.MockHashUtil

import scala.concurrent.Future

class NrsServiceSpec extends ServiceSpec {

  val metrics: Metrics = new MockMetrics

  private val nino: String = "123456789"
  private val notableEvent: String = "submit"

  private val timestamp: DateTime = DateTime.parse("2018-04-07T12:13:25.156Z")

  private val submitRequestBodyString = Json.toJson(
    """{
      |"test":"test123"
      |}""".stripMargin)


  private val encodedString: String = "encodedString"
  private val checksum: String = "checksum"

  private val nrsId: String = "a5894863-9cd7-4d0d-9eee-301ae79cbae6"

  private val nrsSubmission: NrsSubmission =
    NrsSubmission(
      payload = encodedString,
      metadata = Metadata(
        businessId = "itsa",
        notableEvent = notableEvent,
        payloadContentType = "application/json",
        payloadSha256Checksum = checksum,
        userSubmissionTimestamp = timestamp,
        identityData = Some(IdentityDataTestData.correctModel),
        userAuthToken = "Bearer aaaa",
        headerData = Json.toJson(Map(
          "Host" -> "localhost",
          "Authorization" -> "Bearer aaaa",
          "dummyHeader1" -> "dummyValue1",
          "dummyHeader2" -> "dummyValue2"
        )),
        searchKeys =
          SearchKeys(
            nino = Some(nino),
            companyName = None,
            periodKey = None,
            taxPeriodEndDate = None
          )
      )
    )

  trait Test extends MockNrsConnector with MockHashUtil {

    implicit val userRequest: UserRequest[_] =
      UserRequest(
        userDetails =
          UserDetails(
            enrolmentIdentifier = "id",
            userType = "Individual",
            agentReferenceNumber = None,
            identityData = Some(IdentityDataTestData.correctModel)
          ),
        request = FakeRequest().withHeaders(
          "Authorization" -> "Bearer aaaa",
          "dummyHeader1" -> "dummyValue1",
          "dummyHeader2" -> "dummyValue2"
        )
      )

    val service: NrsService = new NrsService(
      mockNrsConnector,
      mockHashUtil,
      metrics
    )
  }

  "service" when {
    "service call successful" must {
      "return the expected result" in new Test {

        MockNrsConnector.submitNrs(nrsSubmission)
          .returns(Future.successful(Right(NrsResponse(nrsId))))

        MockedHashUtil.encode(submitRequestBodyString.toString()).returns(encodedString)
        MockedHashUtil.getHash(submitRequestBodyString.toString()).returns(checksum)

        await(service.submit(nino, notableEvent, submitRequestBodyString, nrsId, timestamp)) shouldBe ((): Unit)
      }
    }

    "service call unsuccessful" must {
      "map 4xx errors correctly" in new Test {

        MockedHashUtil.encode(submitRequestBodyString.toString()).returns(encodedString)
        MockedHashUtil.getHash(submitRequestBodyString.toString()).returns(checksum)

        MockNrsConnector.submitNrs(nrsSubmission)
          .returns(Future.successful(Left(NrsFailure.ExceptionThrown)))

        await(service.submit(nino, notableEvent, submitRequestBodyString, nrsId, timestamp)) shouldBe ((): Unit)
      }
    }
  }
}
