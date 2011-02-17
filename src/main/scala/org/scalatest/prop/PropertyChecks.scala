/*
 * Copyright 2001-2008 Artima, Inc.
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

import org.scalatest._
import org.scalacheck.Arbitrary
import org.scalacheck.Shrink
import org.scalacheck.Arg
import org.scalacheck.Prop
import org.scalacheck.Test
import org.scalacheck.Prop._

trait PropertyChecks extends Checkers { this: Suite =>

  def whenever(condition: => Boolean)(fun: => Unit) {
    if (!condition)
      throw new UnmetConditionException(condition _)
    fun
  }

  def forAll[A, B](table: TableFor2[A, B])(fun: (A, B) => Unit) {
    table(fun)
  }

  def forAll[A, B, C](table: TableFor3[A, B, C])(fun: (A, B, C) => Unit) {
    table(fun)
  }

  def forAll[A, B, C, D](table: TableFor4[A, B, C, D])(fun: (A, B, C, D) => Unit) {
    table(fun)
  }

  def forAll[A, B](fun: (A, B) => Unit)
    (implicit
      p: Boolean => Prop,
      a1: Arbitrary[A], s1: Shrink[A],
      a2: Arbitrary[B], s2: Shrink[B]
    ) {
    check((a: A, b: B) => {
      val (unmetCondition, exception) =
        try {
          fun(a, b)
          (false, None)
        }
        catch {
          case e: UnmetConditionException => (true, None)
          case e => (false, Some(e))
        }
        !unmetCondition ==> (if (exception.isEmpty) true else throw exception.get)
      }
    )
  }
}

