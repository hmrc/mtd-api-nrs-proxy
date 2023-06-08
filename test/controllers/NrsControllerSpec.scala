/*
 * Copyright 2023 HM Revenue & Customs
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
import models.auth.UserDetails
import models.errors.{DownstreamError, BadRequestError}
import org.joda.time.DateTime
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import uk.gov.hmrc.http.HeaderCarrier
import utils.DateUtils

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class NrsControllerSpec
    extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockCurrentDateTime
    with MockNrsService
    with MockIdGenerator
    with MockMtdIdLookupService {

  val date: DateTime        = DateTime.parse("2017-01-01T00:00:00.000Z")
  val fmt: String           = DateUtils.dateTimePattern
  val nino: String          = "AA123456A"
  val vrn: String           = "123456789"
  val notableEvent: String  = "submit"
  val correlationId: String = "X-ID"
  val uid: String           = "a5894863-9cd7-4d0d-9eee-301ae79cbae6"
  val periodKey: String     = "A1A2"

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val mockedMetrics: Metrics = new MockMetrics

    val controller: NrsController = new NrsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      nrsService = mockNrsService,
      cc = cc,
      dateTime = mockCurrentDateTime,
      idGenerator = mockIdGenerator
    )

    def setUpMocks(): Unit = {

      MockCurrentDateTime.getCurrentDate.returns(date).anyNumberOfTimes()
      MockIdGenerator.getUid.returns(uid).once()
      MockIdGenerator.getUid.returns(correlationId).anyNumberOfTimes()
    }

  }

  val submitRequestBodyJson: JsValue = Json.parse(
    """
      |{
      |   "finalised": true
      |}
      |""".stripMargin
  )

  "submit" should {
    "return status 200 for a self-assessment request" when {
      "auth call is successful" in new Test {

        setUpMocks()
        MockEnrolmentsAuthService.authoriseUser().returns(Future.successful(Right(UserDetails("id", "Individual", None, None))))
        MockMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))

        MockNrsService
          .submit(nino, notableEvent, submitRequestBodyJson, uid, date)
          .returns(Future.successful((): Unit))

        private val result: Future[Result] = controller.submit(nino, "submit")(fakePostRequest(submitRequestBodyJson))

        status(result) shouldBe OK
      }

      "return status 500 for a self-assessment request" when {
        "auth call is failed" in new Test {

          MockEnrolmentsAuthService.authoriseUser().returns(Future.successful(Left(DownstreamError)))
          MockMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))

          private val result: Future[Result] = controller.submit(nino, "submit")(fakePostRequest(submitRequestBodyJson))

          status(result) shouldBe INTERNAL_SERVER_ERROR
        }
      }

      "return status 500 for a self-assessment request" when {
        "mtd id lookup call is failed" in new Test {

          MockMtdIdLookupService.lookup(nino).returns(Future.successful(Left(BadRequestError)))

          private val result: Future[Result] = controller.submit(nino, "submit")(fakePostRequest(submitRequestBodyJson))

          status(result) shouldBe INTERNAL_SERVER_ERROR
        }
      }
    }
  }

}
