/*
 * Copyright 2022 HM Revenue & Customs
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

import config.AppConfig
import javax.inject.{Inject, Singleton}
import models.auth.UserDetails
import models.errors.{DownstreamError, MtdError}
import models.request.IdentityData
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals._
import uk.gov.hmrc.auth.core.retrieve.{ItmpAddress, ItmpName, ~}
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EnrolmentsAuthService @Inject() (val connector: AuthConnector, val appConfig: AppConfig) extends Logging {

  private val authFunction: AuthorisedFunctions = new AuthorisedFunctions {
    override def authConnector: AuthConnector = connector
  }

  def authorised(predicate: Predicate)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[AuthOutcome] = {
    authFunction
      .authorised(predicate)
      .retrieve(
        affinityGroup and allEnrolments
          and internalId and externalId and agentCode and credentials
          and confidenceLevel and nino and saUtr and name and dateOfBirth
          and email and agentInformation and groupIdentifier and credentialRole
          and mdtpInformation and credentialStrength and loginTimes
          and itmpName and itmpDateOfBirth and itmpAddress) {
        case affGroup ~ enrolments ~ inId ~ exId ~ agCode ~ creds
            ~ confLevel ~ ni ~ saRef ~ nme ~ dob
            ~ eml ~ agInfo ~ groupId ~ credRole
            ~ mdtpInfo ~ credStrength ~ logins
            ~ itmpName ~ itmpDateOfBirth ~ itmpAddress
            if affGroup.contains(AffinityGroup.Organisation) || affGroup.contains(AffinityGroup.Individual) || affGroup.contains(
              AffinityGroup.Agent) =>
          val emptyItmpName: ItmpName       = ItmpName(None, None, None)
          val emptyItmpAddress: ItmpAddress = ItmpAddress(None, None, None, None, None, None, None, None)

          val identityData =
            IdentityData(
              inId,
              exId,
              agCode,
              creds,
              confLevel,
              ni,
              saRef,
              nme,
              dob,
              eml,
              agInfo,
              groupId,
              credRole,
              mdtpInfo,
              itmpName.getOrElse(emptyItmpName),
              itmpDateOfBirth,
              itmpAddress.getOrElse(emptyItmpAddress),
              affGroup,
              credStrength,
              logins
            )

          createUserDetailsWithLogging(affinityGroup = affGroup.get.toString, enrolments, Some(identityData))
        case _ =>
          logger.warn(s"[EnrolmentsAuthService][authorised] Authorisation failed due to unsupported affinity group.")
          Future.successful(Left(DownstreamError))
      } recoverWith { case error =>
      logger.warn(s"[EnrolmentsAuthService][authorised] An unexpected error occurred: $error")
      Future.successful(Left(DownstreamError))
    }
  }

  private def createUserDetailsWithLogging(affinityGroup: String,
                                           enrolments: Enrolments,
                                           identityData: Option[IdentityData]): Future[Right[MtdError, UserDetails]] = {

    val userDetails = UserDetails(
      enrolmentIdentifier = "",
      userType = affinityGroup,
      agentReferenceNumber = None,
      identityData
    )

    if (affinityGroup != "Agent") {
      Future.successful(Right(userDetails))
    } else {
      Future.successful(Right(userDetails.copy(agentReferenceNumber = getAgentReferenceFromEnrolments(enrolments))))
    }
  }

  def getAgentReferenceFromEnrolments(enrolments: Enrolments): Option[String] = enrolments
    .getEnrolment("HMRC-AS-AGENT")
    .flatMap(_.getIdentifier("AgentReferenceNumber"))
    .map(_.value)

}
