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
package org.scalatest.spec

import org.scalatest.fun.FunSuite
import org.scalatest.fun.Group
import org.scalatest.matchers.SpecsMatchers

 /**
 * @author Bill Venners
 */
abstract class Spec(specName: String) extends FunSuite {

  def this() = this("")

  override def suiteName = specName

  private def registerSut(sut: String) {
    println("registered sut: " + sut)
  }

  private def registerExample(example: String, f: => Unit) {
    println("registered example: " + example)
  }

  class InWrapper(spec: Spec, example: String) {
    def in(f: => Unit) {
      spec.registerExample(example, f)
    }
  }
  class ShouldWrapper(spec: Spec, sut: String) {
    spec.registerSut(sut)
    def should(f: => Unit) {
      f
    }
  }
  implicit def stringToShouldWrapper(sut: String): ShouldWrapper = new ShouldWrapper(this, sut)
  implicit def stringToInWrapper(example: String): InWrapper = new InWrapper(this, example)
}
