/*
 * Copyright 2001-2011 Artima, Inc.
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
package org.scalatest.prop

import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers

class TableStyleSpec extends Spec with ShouldMatchers with TableDrivenPropertyChecks {

  object FiboGen {
    private var prev: Option[(Option[Int], Int)] = None
    def next: Int =
      prev match {
        case None =>
          prev = Some(None, 0)
          0
        case Some((None, 0)) =>
          prev = Some((Some(0), 1))
          1
        case Some((Some(prev1), prev2)) =>
          val result = prev1 + prev2
          prev = Some((Some(prev2), result))
          result
      }
  }

  val first14FiboNums =
    Table("n", 0, 1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89, 144, 233)

  var prev: Option[(Int, Int)] = None
  forAll (first14FiboNums) { n =>
    prev match {
      case None => 
        prev = Some((FiboGen.next, FiboGen.next))
      case Some((prev1, prev2)) =>
        val next = FiboGen.next
        next should equal (prev1 + prev2)
        prev = Some((prev2, next))
    }
  }
}

