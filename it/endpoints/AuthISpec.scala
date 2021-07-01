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

package endpoints

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import stubs.{AuditStub, AuthStub, MtdIdLookupStub, NrsStub}
import support.IntegrationBaseSpec

class AuthISpec extends IntegrationBaseSpec {

  private trait Test {
    val nino: String = "AA123456A"
    val notableEvent: String = "itsa"
    val correlationId: String = "X-123"

    val requestBodyJson: JsValue = Json.parse(
      s"""
         |{
         |  "submit": "submit"
         |}
      """.stripMargin
    )

    def uri: String = s"/$nino/$notableEvent"

    val nrsUrl: String = s".*/submission.*"

    def setupStubs(): StubMapping

    def request(): WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"), ("Authorization", "Bearer testtoken"))
    }
  }

  val nrsSuccess: JsValue = Json.parse(
    s"""
       |{
       |  "nrSubmissionId":"2dd537bc-4244-4ebf-bac9-96321be13cdc",
       |  "cadesTSignature":"30820b4f06092a864886f70111111111c0445c464",
       |  "timestamp":""
       |}
         """.stripMargin)

  "Calling the nrs endpoint" should {
    "return a 200 status code" when {
      "any valid request is made" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          NrsStub.onSuccess(NrsStub.POST, nrsUrl, ACCEPTED, nrsSuccess)
        }

        val response: WSResponse = await(request().post(requestBodyJson))
        response.status shouldBe OK
      }
    }

    "an MTD ID is successfully retrieve from the NINO and the user is authorised" should {
      "return 200" in new Test {
        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          NrsStub.onSuccess(NrsStub.POST, nrsUrl, ACCEPTED, nrsSuccess)
        }

        val response: WSResponse = await(request().post(requestBodyJson))
        response.status shouldBe OK
      }
    }

    "an MTD ID is successfully retrieve from the NINO and the user is NOT logged in" should {
      "return 500" in new Test {
        override val nino: String = "AA123456A"

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          MtdIdLookupStub.ninoFound(nino)
          AuthStub.unauthorisedNotLoggedIn()
        }

        val response: WSResponse = await(request().post(requestBodyJson))
        response.status shouldBe INTERNAL_SERVER_ERROR
      }
    }

    "an MTD ID is successfully retrieve from the NINO and the user is NOT authorised" should {
      "return 500" in new Test {
        override val nino: String = "AA123456A"

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          MtdIdLookupStub.ninoFound(nino)
          AuthStub.unauthorisedOther()
        }

        val response: WSResponse = await(request().post(requestBodyJson))
        response.status shouldBe INTERNAL_SERVER_ERROR
      }
    }
  }
}
