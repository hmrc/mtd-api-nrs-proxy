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

import play.api.libs.json.{JsValue, Json}

protected trait Identifier {
  val identifierName: String
  val identifierValue: String

  val toJson: JsValue = Json.toJson(s"""$identifierName: $identifierValue""")
}

case class NINO(identifierValue: String) extends Identifier {
  val identifierName: String = "nino"
}

case class VRN(identifierValue: String) extends Identifier {
  val identifierName: String = "vrn"
}
