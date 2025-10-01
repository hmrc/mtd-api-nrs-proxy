/*
 * Copyright 2025 HM Revenue & Customs
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

package models.auth

import models.request.IdentityData
import play.api.libs.json.*
import support.UnitSpec
import utils.NrsTestData.IdentityDataTestData.correctModel

class UserDetailsSpec extends UnitSpec {

  "UserDetails" should {

    "serialize to JSON correctly" in {
      val user = UserDetails(
        enrolmentIdentifier = "ABC123",
        userType = "Individual",
        agentReferenceNumber = Some("ARN123"),
        identityData = Some(correctModel)
      )

      Json.toJson(user)(Json.writes[UserDetails])
    }

    "deserialize from JSON correctly" in {
      val json = Json.parse(
        """
          |{
          | "enrolmentIdentifier": "ABC123",
          | "userType": "Individual",
          | "agentReferenceNumber": "ARN123"
          |}
        """.stripMargin
      )

      json.as[UserDetails](Json.reads[UserDetails])
    }

    "allow optional fields to be None" in {
      val user = UserDetails(
        enrolmentIdentifier = "XYZ789",
        userType = "Agent",
        agentReferenceNumber = None
      )
      user.identityData shouldBe None
    }
  }

}
