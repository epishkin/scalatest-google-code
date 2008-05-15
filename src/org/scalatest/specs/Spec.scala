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
package org.scalatest.specs

import org.scalatest.fun.FunSuite
import org.scalatest.matchers.SpecsMatchers

/**
 * @author Bill Venners
 */
abstract class Spec(specName: String) extends FunSuite {

  def this() = this("")

  override def suiteName = specName

  protected def doBefore(f: => Unit) {
    println("registered doBefore")
  }

  protected def doAfter(f: => Unit) {
    println("registered doAfter")
  }

  protected def skip(f: => Unit) {
    println("registered skip")
  }

  private def registerShould(sut: String) {
    println("registered sut with should: " + sut)
  }

  private def registerCan(sut: String) {
    println("registered sut with can: " + sut)
  }

  private def unregisterShould(sut: String) {
    println("unregistered sut with should: " + sut)
  }

  private def unregisterCan(sut: String) {
    println("unregistered sut with can: " + sut)
  }

  private def registerExample(example: String, f: => Unit) {
    println("registered example: " + example)
  }

  class InWrapper(spec: Spec, example: String) {
    def >>(f: => Unit) {
      in(f)
    }
    def in(f: => Unit) {
      spec.registerExample(example, f)
    }
  }

  class CanWrapper(spec: Spec, sut: String) {
    def can(f: => Unit) {
      spec.registerCan(sut)
      f
      spec.unregisterCan(sut)
    }
  }

  class ShouldWrapper(spec: Spec, sut: String) {
    def should(f: => Unit) {
      spec.registerShould(sut)
      f
      spec.unregisterShould(sut)
    }
  }

  implicit def declare(sut: String): ShouldWrapper = new ShouldWrapper(this, sut)
  implicit def forExample(example: String): InWrapper = new InWrapper(this, example)
  implicit def declareCan(sut: String): CanWrapper = new CanWrapper(this, sut)
}

