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

package config

import javax.inject.{Inject, Singleton}
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import utils.Retrying

import scala.concurrent.duration.{Duration, FiniteDuration}

trait AppConfig {

  def apiStatus(version: String): String

  def featureSwitch: Option[Configuration]

  // NRS config items
  def nrsApiKey: String
  def nrsRetries: List[FiniteDuration]

  def appName: String

  def nrsBaseUrl: String
}

@Singleton
class AppConfigImpl @Inject()(config: ServicesConfig, configuration: Configuration) extends AppConfig {

  // NRS config items
  val nrsApiKey: String = config.getString("access-keys.xApiKey")
  val appName: String = config.getString("appName")

  val nrsBaseUrl: String = config.baseUrl("non-repudiation")

  lazy val nrsRetries: List[FiniteDuration] =
    Retrying.fibonacciDelays(getFiniteDuration(nrsConfig, "initialDelay"), nrsConfig.get[Int]("numberOfRetries"))

  private val nrsConfig = configuration.get[Configuration]("microservice.services.non-repudiation")

  private final def getFiniteDuration(config: Configuration, path: String): FiniteDuration = {
    val string = config.get[String](path)

    Duration.create(string) match {
      case f: FiniteDuration => f
      case _                 => throw new RuntimeException(s"Not a finite duration '$string' for $path")
    }
  }

  def apiStatus(version: String): String = config.getString(s"api.$version.status")

  def featureSwitch: Option[Configuration] = configuration.getOptional[Configuration](s"feature-switch")
}

