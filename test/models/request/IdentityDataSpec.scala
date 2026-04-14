/*
 * Copyright 2026 HM Revenue & Customs
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

package models.request

import play.api.libs.json.*
import support.UnitSpec
import uk.gov.hmrc.auth.core.retrieve._
import uk.gov.hmrc.auth.core.{AffinityGroup, ConfidenceLevel, CredentialRole}
import utils.NrsTestData.IdentityDataTestData.*
import utils.NrsTestData.ItmpNameTestData.*

import java.time.Instant

import models.request.IdentityData.{
  given_OFormat_Credentials,
  given_OFormat_AgentInformation,
  given_OFormat_MdtpInformation,
  given_OFormat_ItmpAddress,
  given_OFormat_LoginTimes
}

class IdentityDataSpec extends UnitSpec {

  "IdentityData JSON format" should {

    "write correctly to JSON" in {
      Json.toJson(correctModel) shouldBe correctJson
    }

    "read correctly from JSON" in {
      correctJson.as[IdentityData] shouldBe correctModel
    }

    "fail to read when fields have wrong types" in {
      val invalidJson = correctJson.as[JsObject] + ("affinityGroup" -> JsNumber(123))
      invalidJson.validate[IdentityData] shouldBe a[JsError]
    }

    "fail to deserialize when a required field is missing" in {
      val invalidJson = correctJson.as[JsObject] - "confidenceLevel"
      invalidJson.validate[IdentityData] shouldBe a[JsError]
    }

    "handle optional fields when absent" in {
      val jsonWithoutOptional = correctJson.as[JsObject] - "email" - "nino" - "externalId"
      jsonWithoutOptional.validate[IdentityData] shouldBe a[JsSuccess[?]]
    }

    "handle mixed optional fields" in {
      val modelWithMixed = correctModel.copy(email = None, nino = Some("DH00475D"), externalId = None)
      val json           = Json.toJson(modelWithMixed)
      json.validate[IdentityData] shouldBe JsSuccess(modelWithMixed)
    }

    "handle different affinity group" in {
      val modelWithIndividual = correctModel.copy(affinityGroup = Some(AffinityGroup.Individual))
      val json                = Json.toJson(modelWithIndividual)
      json.validate[IdentityData] shouldBe JsSuccess(modelWithIndividual)
    }
  }

  "ItmpNameData JSON format" should {

    "serialize to JSON correctly" in {
      Json.toJson(model)(IdentityData.given_OFormat_ItmpName) shouldBe json
    }

    "deserialize from JSON correctly" in {
      json.as[ItmpName](IdentityData.given_OFormat_ItmpName) shouldBe model
    }

  }

  "Credentials JSON format" should {

    val creds = Credentials("12345-credId", "GovernmentGateway")
    val json  = Json.obj("providerId" -> "12345-credId", "providerType" -> "GovernmentGateway")

    "write correctly to JSON" in {
      Json.toJson(creds).shouldBe(json)
    }

    "read correctly from JSON" in {
      json.as[Credentials].shouldBe(creds)
    }

    "fail to read when providerId is missing" in {
      val invalidJson = json - "providerId"
      invalidJson.validate[Credentials].isError shouldBe true
    }

    "fail to read when providerType is missing" in {
      val invalidJson = json - "providerType"
      invalidJson.validate[Credentials].isError shouldBe true
    }

    "fail to read when providerId has wrong type" in {
      val invalidJson = json ++ Json.obj("providerId" -> 123)
      invalidJson.validate[Credentials].isError shouldBe true
    }

    "fail to read when providerType has wrong type" in {
      val invalidJson = json ++ Json.obj("providerType" -> 123)
      invalidJson.validate[Credentials].isError shouldBe true
    }

  }

  "AgentInformation JSON format" should {

    val agentInfo = AgentInformation(agentCode = Some("TZRXXV"), agentFriendlyName = Some("Bodgitt & Legget LLP"), agentId = Some("BDGL"))
    val json = Json.obj(
      "agentId"           -> "BDGL",
      "agentCode"         -> "TZRXXV",
      "agentFriendlyName" -> "Bodgitt & Legget LLP"
    )

    "write correctly to JSON" in {
      Json.toJson(agentInfo).shouldBe(json)
    }

    "read correctly from JSON" in {
      json.as[AgentInformation].shouldBe(agentInfo)
    }

    "handle optional fields correctly" in {
      val agentInfoOptional = AgentInformation(agentCode = None, agentFriendlyName = None, agentId = None)
      val jsonOptional      = Json.obj()
      Json.toJson(agentInfoOptional).shouldBe(jsonOptional)
      jsonOptional.as[AgentInformation].shouldBe(agentInfoOptional)
    }

    "fail to read when agentId has wrong type" in {
      val invalidJson = json ++ Json.obj("agentId" -> 123)
      invalidJson.validate[AgentInformation].isError shouldBe true
    }

  }

  "MdtpInformation JSON format" should {

    val mdtpInfo = MdtpInformation("DeviceId", "SessionId")
    val json     = Json.obj("deviceId" -> "DeviceId", "sessionId" -> "SessionId")

    "write correctly to JSON" in {
      Json.toJson(mdtpInfo).shouldBe(json)
    }

    "read correctly from JSON" in {
      json.as[MdtpInformation].shouldBe(mdtpInfo)
    }

    "fail to read when deviceId is missing" in {
      val invalidJson = json - "deviceId"
      invalidJson.validate[MdtpInformation].isError shouldBe true
    }

    "fail to read when sessionId is missing" in {
      val invalidJson = json - "sessionId"
      invalidJson.validate[MdtpInformation].isError shouldBe true
    }

    "fail to read when deviceId has wrong type" in {
      val invalidJson = json ++ Json.obj("deviceId" -> 123)
      invalidJson.validate[MdtpInformation].isError shouldBe true
    }

  }

  "ItmpAddress JSON format" should {

    val itmpAddr = ItmpAddress(None, None, None, None, None, None, None, None)
    val json     = Json.obj()

    "write correctly to JSON" in {
      Json.toJson(itmpAddr).shouldBe(json)
    }

    "read correctly from JSON" in {
      json.as[ItmpAddress].shouldBe(itmpAddr)
    }

    "handle all optional fields correctly" in {
      val itmpAddrWithValues = ItmpAddress(
        Some("line1"),
        Some("line2"),
        Some("line3"),
        Some("line4"),
        Some("line5"),
        Some("postcode"),
        Some("countryName"),
        Some("countryCode"))
      val jsonWithValues = Json.obj(
        "line1"       -> "line1",
        "line2"       -> "line2",
        "line3"       -> "line3",
        "line4"       -> "line4",
        "line5"       -> "line5",
        "postCode"    -> "postcode",
        "countryName" -> "countryName",
        "countryCode" -> "countryCode"
      )
      Json.toJson(itmpAddrWithValues).shouldBe(jsonWithValues)
      jsonWithValues.as[ItmpAddress].shouldBe(itmpAddrWithValues)
    }

  }

  "LoginTimes JSON format" should {

    val loginTimes = LoginTimes(
      Instant.parse("2016-11-27T09:00:00Z"),
      Some(Instant.parse("2016-11-01T12:00:00Z"))
    )
    val json = Json.obj(
      "currentLogin"  -> "2016-11-27T09:00:00Z",
      "previousLogin" -> "2016-11-01T12:00:00Z"
    )

    "write correctly to JSON" in {
      Json.toJson(loginTimes).shouldBe(json)
    }

    "read correctly from JSON" in {
      json.as[LoginTimes].shouldBe(loginTimes)
    }

    "handle optional previousLogin correctly" in {
      val loginTimesNoPrevious = LoginTimes(Instant.parse("2016-11-27T09:00:00Z"), None)
      val jsonNoPrevious       = Json.obj("currentLogin" -> "2016-11-27T09:00:00Z")
      Json.toJson(loginTimesNoPrevious).shouldBe(jsonNoPrevious)
      jsonNoPrevious.as[LoginTimes].shouldBe(loginTimesNoPrevious)
    }

    "fail to read when currentLogin is missing" in {
      val invalidJson = json - "currentLogin"
      invalidJson.validate[LoginTimes].isError shouldBe true
    }

    "fail to read when currentLogin has wrong type" in {
      val invalidJson = json ++ Json.obj("currentLogin" -> Json.obj("invalid" -> "object"))
      invalidJson.validate[LoginTimes].isError shouldBe true
    }

    "fail to read when previousLogin has wrong type" in {
      val invalidJson = json ++ Json.obj("previousLogin" -> Json.arr("invalid", "array"))
      invalidJson.validate[LoginTimes].isError shouldBe true
    }

  }

}
