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

package models.request

import play.api.libs.json.*
import support.UnitSpec
import uk.gov.hmrc.auth.core.retrieve.ItmpName
import utils.NrsTestData.IdentityDataTestData.*
import utils.NrsTestData.ItmpNameTestData.*

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
  }

  "ItmpNameData JSON format" should {

    "serialize to JSON correctly" in {
      Json.toJson(model)(IdentityData.given_OFormat_ItmpName) shouldBe json
    }

    "deserialize from JSON correctly" in {
      json.as[ItmpName](IdentityData.given_OFormat_ItmpName) shouldBe model
    }

  }

}
