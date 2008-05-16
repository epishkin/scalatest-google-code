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

import org.scalatest.Suite

/**
 * @author Bill Venners
 */
abstract class SpecSuite(specName: String) extends Suite {

  def this() = this("")

  class Example(name: String) {
    def in(f: => Unit) {
      println("it should " + name)
    }
  }

  class CousinIt {
    def should(exampleName: String) = new Example(exampleName)
    def should(behaveWord: CousinBehave) = new CousinBehave
  }

  class CousinBehave {
    def like(sharedBehaviorName: String) = println("it should behave like " + sharedBehaviorName)
  }

  protected def it = new CousinIt

  protected def behave = new CousinBehave

  protected def describe(name: String)(f: => Unit) { println("describing " + name); f }
  protected def share(name: String)(f: => Unit) { println("sharing " + name); f }

/*
This is really neat. It enables this code:

import org.scalatest.specs.SpecSuite

class MySuite extends SpecSuite {

  share("a non-empty stack") {
    it should "return the top when sent #peek" in {
      println("and how")
    }
  }

  describe("Stack") {

    it should "work right the first time" in {
      println("and how")
    }

    it should behave like "a non-empty stack"
  }
}
*/

/*
  override def suiteName = specName

  protected def doBefore(f: => Unit) {
    println("registered doBefore")
  }

  protected def doAfter(f: => Unit) {
    println("registered doAfter")
  }

  protected def ignore(f: => Unit) {
    println("registered ignore")
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

  class InWrapper(spec: SpecSuite, example: String) {
    def >>(f: => Unit) {
      in(f)
    }
    def in(f: => Unit) {
      spec.registerExample(example, f)
    }
  }

  class CanWrapper(spec: SpecSuite, sut: String) {
    def can(f: => Unit) {
      spec.registerCan(sut)
      f
      spec.unregisterCan(sut)
    }
  }

  class ShouldWrapper(spec: SpecSuite, sut: String) {
    def should(f: => Unit) {
      spec.registerShould(sut)
      f
      spec.unregisterShould(sut)
    }
  }

  implicit def declare(sut: String): ShouldWrapper = new ShouldWrapper(this, sut)
  implicit def forExample(example: String): InWrapper = new InWrapper(this, example)
  implicit def declareCan(sut: String): CanWrapper = new CanWrapper(this, sut)
*/
}

