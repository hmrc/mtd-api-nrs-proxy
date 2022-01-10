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

package models.request

import org.joda.time.LocalDate
import play.api.libs.json._
import utils.DateUtils

case class SearchKeys(identifier: Option[Identifier] = None,
                      companyName: Option[String] = None,
                      taxPeriodEndDate: Option[LocalDate] = None,
                      periodKey: Option[String] = None)

object SearchKeys {
  implicit val dateFormats: Format[LocalDate] = DateUtils.dateFormat

  implicit val writes: OWrites[SearchKeys] = (o: SearchKeys) => JsObject.apply(
    o.identifier.fold(Seq.empty[(String, JsValue)])(
      identifier => Seq(identifier.identifierName -> Json.toJson(identifier.identifierValue))
    ) ++
      o.companyName.fold(Seq.empty[(String, JsValue)])(
        companyName => Seq("companyName" -> Json.toJson(companyName))
      ) ++
      o.taxPeriodEndDate.fold(Seq.empty[(String, JsValue)])(
        taxPeriodEndDate => Seq("taxPeriodEndDate" -> Json.toJson(taxPeriodEndDate))
      ) ++
      o.periodKey.fold(Seq.empty[(String, JsValue)])(
        periodKey => Seq("periodKey" -> Json.toJson(periodKey))
      )
  )
}
