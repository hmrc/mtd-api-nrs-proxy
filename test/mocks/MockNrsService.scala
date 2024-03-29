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

package mocks

import controllers.UserRequest
import org.joda.time.DateTime
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import play.api.libs.json.JsValue
import services.NrsService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

trait MockNrsService extends MockFactory {

  val mockNrsService: NrsService = mock[NrsService]

  object MockNrsService {

    def submit(identifier: String, notableEvent: String, body: JsValue, nrsId: String, dateTime: DateTime): CallHandler[Future[Unit]] = {
      (mockNrsService
        .submit(_: String, _: String, _: JsValue, _: String, _: DateTime)(_: UserRequest[_], _: HeaderCarrier, _: ExecutionContext, _: String))
        .expects(identifier, *, *, *, *, *, *, *, *)
    }

  }

}
