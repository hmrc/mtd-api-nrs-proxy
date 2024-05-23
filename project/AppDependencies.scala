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

import play.core.PlayVersion
import play.core.PlayVersion.current
import play.sbt.PlayImport.*
import sbt.*

object AppDependencies {

  val bootstrapPlay30Version = "8.1.0"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc"                  %% "bootstrap-backend-play-30" % bootstrapPlay30Version,
    "org.playframework"            %% "play-json-joda"            % "3.0.3",
    "com.fasterxml.jackson.module" %% "jackson-module-scala"      % "2.17.1"
  )

  def test(scope: String = "test, it"): Seq[sbt.ModuleID] = Seq(
    "uk.gov.hmrc"          %% "bootstrap-test-play-30" % bootstrapPlay30Version % scope,
    "org.playframework"    %% "play-test"              % current                % scope,
    "org.scalatest"        %% "scalatest"              % "3.2.15"               % scope,
    "com.vladsch.flexmark"  % "flexmark-all"           % "0.64.6"               % scope,
    "org.scalamock"        %% "scalamock"              % "5.2.0"                % scope,
    "com.miguno.akka"      %% "akka-mock-scheduler"    % "0.5.5"                % scope,
    "com.github.pjfanning" %% "pekko-mock-scheduler"   % "0.6.0"                % scope
  )

}
