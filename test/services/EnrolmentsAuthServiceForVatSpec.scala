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

import fixtures.AuthFixture._
import mocks.{MockAppConfig, MockAuthConnector}
import models.errors.{DownstreamError, MtdError}
import org.joda.time.LocalDate
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Individual, Organisation}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals._

import scala.collection.Seq
import scala.concurrent.Future

class EnrolmentsAuthServiceForVatSpec extends ServiceSpec with MockAppConfig{

  trait Test extends MockAuthConnector {

    val service: EnrolmentsAuthService = new EnrolmentsAuthService(mockAuthConnector, mockAppConfig)

    val authRetrievalsAffinity: Retrieval[Option[AffinityGroup] ~ Enrolments] = affinityGroup and allEnrolments
    val authRetrievalsAgentCode: Retrieval[Option[String] ~ Enrolments] = agentCode and allEnrolments

    val authRetrievalsAffinityWithNrs:
      Retrieval[Option[AffinityGroup] ~ Enrolments ~ Option[String] ~ Option[String] ~ Option[String] ~ Option[Credentials] ~ ConfidenceLevel ~ Option[String] ~ Option[String] ~ Option[Name] ~ Option[LocalDate] ~ Option[String] ~ AgentInformation ~ Option[String] ~ Option[CredentialRole] ~ Option[MdtpInformation] ~ Option[String] ~ LoginTimes] =
      affinityGroup and allEnrolments and internalId and externalId and agentCode and credentials and confidenceLevel and nino and saUtr and name and dateOfBirth and email and agentInformation and groupIdentifier and credentialRole and mdtpInformation and credentialStrength and loginTimes

    val predicate: Enrolment = Enrolment("HMRC-MTD-VAT")
      .withIdentifier("VRN", "123456789")
      .withDelegatedAuthRule("mtd-vat-auth")
  }

  "authorised" when {

    "the user is an authorised individual" should {
      "return the 'Individual' user type in the user details" in new Test {

        private val retrievalsResultAffinity = authResponse(indIdentityData, vatEnrolments)

        val expected = Right(userDetails(Individual, AgentInformation(
          agentId = None,
          agentCode = None,
          agentFriendlyName = None)))

        MockAuthConnector.authorised(predicate, authRetrievalsAffinityWithNrs)
          .returns(Future.successful(retrievalsResultAffinity))

        private val result = await(service.authorised(predicate))

        result shouldBe expected
      }
    }

    "the user is an authorised organisation" should {
      "return the 'Organisation' user type in the user details" in new Test {

        private val retrievalsResultAffinity = authResponse(orgIdentityData, vatEnrolments)

        val expected = Right(userDetails(Organisation, AgentInformation(
          agentId = None,
          agentCode = None,
          agentFriendlyName = None)))

        MockAuthConnector.authorised(predicate, authRetrievalsAffinityWithNrs)
          .returns(Future.successful(retrievalsResultAffinity))

        private val result = await(service.authorised(predicate))

        result shouldBe expected
      }
    }

    "the user is an authorised agent" should {
      "return the 'Agent' user type in the user details" in new Test {

        private val retrievalsResultAffinity = authResponse(agentIdentityData, vatAgentEnrolments)

        val expected = Right(userDetails(Agent, AgentInformation(
          agentCode = Some("AGENT007"),
          agentFriendlyName = Some("James"),
          agentId = Some("987654321"))))

        MockAuthConnector.authorised(predicate, authRetrievalsAffinityWithNrs)
          .returns(Future.successful(retrievalsResultAffinity))

        private val result = await(service.authorised(predicate))

        result shouldBe expected
      }
    }

    "the user belongs to an unsupported affinity group" should {
      "return an unauthorised error" in new Test {

        case object OtherAffinity extends AffinityGroup

        private val retrievalsResultAffinity = authResponse(orgIdentityData.copy(affinityGroup = Some(OtherAffinity)), vatEnrolments)

        MockAuthConnector.authorised(predicate, authRetrievalsAffinityWithNrs)
          .returns(Future.successful(retrievalsResultAffinity))

        private val result = await(service.authorised(predicate))

        result shouldBe Left(DownstreamError)
      }
    }

    "an exception occurs during enrolment authorisation" must {
      "map the exceptions correctly" when {

        def serviceException(exception: RuntimeException, mtdError: MtdError): Unit = {
          s"the exception '${exception.getClass.getSimpleName}' occurs" should {
            s"return the MtdError '$mtdError'" in new Test {

              MockAuthConnector.authorised(predicate, authRetrievalsAffinityWithNrs)
                .returns(Future.failed(exception))

              private val result = await(service.authorised(predicate))

              print(result)
              result shouldBe Left(mtdError)
            }
          }
        }

        case class UnmappedException(msg: String = "Some text") extends AuthorisationException(msg)

        val authServiceErrorMap: Seq[(RuntimeException, MtdError)] =
          Seq(
            (UnmappedException(), DownstreamError)
          )

        authServiceErrorMap.foreach(args => (serviceException _).tupled(args))
      }
    }

    "the arn and vrn are missing from the authorisation response" should {
      "not throw an error" in new Test {

        private val retrievalsResultAffinity = authResponse(orgIdentityData.copy(affinityGroup = Some(Agent)), vatEnrolments)

        val expected = Right(userDetails(Agent, AgentInformation(
          agentCode = None,
          agentFriendlyName = None,
          agentId = None)))

        MockAuthConnector.authorised(predicate, authRetrievalsAffinityWithNrs)
          .returns(Future.successful(retrievalsResultAffinity))

        private val result = await(service.authorised(predicate))

        result shouldBe expected
      }
    }
  }

  "getAgentReferenceFromEnrolments" when {
    "a valid enrolment with an arn exists" should {
      "return the expected client ARN" in new Test {

        val arn: String = "123456789"

        val enrolments: Enrolments =
          Enrolments(
            enrolments = Set(
              Enrolment(
                key = "HMRC-AS-AGENT",
                identifiers = Seq(EnrolmentIdentifier("AgentReferenceNumber", arn)),
                state = "Active"
              )
            )
          )

        service.getAgentReferenceFromEnrolments(enrolments) shouldBe Some(arn)
      }
    }

    "a valid enrolment with an arn does not exist" should {
      "return None" in new Test {

        val enrolments: Enrolments =
          Enrolments(
            enrolments = Set(
              Enrolment(
                key = "HMRC-AS-AGENT",
                identifiers = Seq(EnrolmentIdentifier("SomeOtherIdentifier", "id")),
                state = "Active"
              )
            )
          )

        service.getAgentReferenceFromEnrolments(enrolments) shouldBe None
      }
    }
  }
}
