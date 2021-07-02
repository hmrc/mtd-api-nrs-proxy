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

import javax.inject.{Inject, Singleton}
import play.api.libs.json.JsValue
import play.api.mvc.{Action, ControllerComponents}
import services.{EnrolmentsAuthService, MtdIdLookupService, NrsService}
import utils.{CurrentDateTime, IdGenerator, Logging}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class NrsController @Inject()(val authService: EnrolmentsAuthService,
                              val lookupService: MtdIdLookupService,
                              nrsService: NrsService,
                              val idGenerator: IdGenerator,
                              dateTime: CurrentDateTime,
                              cc: ControllerComponents)(implicit ec: ExecutionContext)
  extends AuthorisedController(cc) with Logging {

  def submit(identifier: String, notableEvent: String): Action[JsValue] =
    authorisedAction(identifier).async(parse.json) { implicit request =>

      implicit val correlationId: String = request.headers.get("CorrelationId") match {
        case None => idGenerator.getUid
        case Some(id) => id
      }
      val nrsId = idGenerator.getUid
      val submissionTimestamp = dateTime.getDateTime

      logger.info(s"[NrsController] [submit] NRS submission request received for $notableEvent")

      nrsService.submit(identifier, notableEvent, request.body, nrsId, submissionTimestamp)
          Future.successful(Ok)
    }
}
