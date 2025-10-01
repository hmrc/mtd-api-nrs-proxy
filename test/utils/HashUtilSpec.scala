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

package utils

import support.UnitSpec

import java.nio.charset.StandardCharsets
import java.util.Base64

class HashUtilSpec extends UnitSpec {

  private val hashUtil = new HashUtil()

  "HashUtil" should {

    "encode a string to Base64 correctly" in {
      val input    = "test user"
      val expected = Base64.getEncoder.encodeToString(input.getBytes(StandardCharsets.UTF_8))

      hashUtil.encode(input) shouldBe expected
    }

    "produce different hashes for different inputs" in {
      val hash1 = hashUtil.getHash("test")
      val hash2 = hashUtil.getHash("user")

      hash1 should not be hash2
    }

    "produce a consistent hash for the same input" in {
      val hash1 = hashUtil.getHash("consistent")
      val hash2 = hashUtil.getHash("consistent")

      hash1 shouldBe hash2
    }
  }

}
