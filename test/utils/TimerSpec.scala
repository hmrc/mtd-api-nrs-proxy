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

package utils

import mocks.MockMetrics
import org.scalatest.matchers.Matcher
import support.UnitSpec
import uk.gov.hmrc.play.bootstrap.metrics.Metrics

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global

class TimerSpec extends UnitSpec {

  class Test extends Timer with Logging {
    val metrics: Metrics = new MockMetrics

    val timings = mutable.ListBuffer[Long]()

    override def stopAndLog[A](name: String, timer: com.codahale.metrics.Timer.Context): Unit = {
      timings += timer.stop() / 1000000
    }

    def lastTiming: Long = timings.lastOption.getOrElse(fail("No timing recorded"))
  }

  "Timer" should {

    "Time a future correctly" in new Test {
      val sleepMs = 300
      await(timeFuture("test timer", "test.sleep") {
        Thread.sleep(sleepMs)
      })
      val beWithinTolerance: Matcher[Long] = be >= sleepMs.toLong and be <= (sleepMs + 100).toLong
      lastTiming should beWithinTolerance
    }

    "Time a future incorrectly" in new Test {
      val sleepMs = 300
      override def stopAndLog[A](name: String, timer: com.codahale.metrics.Timer.Context): Unit =
        timings += timer.stop() / 100000
      await(timeFuture("test timer", "test.sleep") {
        Thread.sleep(sleepMs)
      })
      val beWithinTolerance: Matcher[Long] = be >= sleepMs.toLong and be <= (sleepMs + 100).toLong
      lastTiming shouldNot beWithinTolerance
    }

    "Time a block correctly" in new Test {
      val sleepMs = 300
      await(time("test timer", "test.sleep") {
        Thread.sleep(sleepMs)
      })
      val beWithinTolerance: Matcher[Long] = be >= sleepMs.toLong and be <= (sleepMs + 100).toLong
      lastTiming should beWithinTolerance
    }

    "Time a block incorrectly" in new Test {
      val sleepMs = 300
      override def stopAndLog[A](name: String, timer: com.codahale.metrics.Timer.Context): Unit =
        timings += timer.stop() / 100000
      await(time("test timer", "test.sleep") {
        Thread.sleep(sleepMs)
      })
      val beWithinTolerance: Matcher[Long] = be >= sleepMs.toLong and be <= (sleepMs + 100).toLong
      lastTiming shouldNot beWithinTolerance
    }
  }

}
