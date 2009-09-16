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
package org.scalatest.concurrent

import org.scalatest._
import matchers.ShouldMatchers
import Thread.State._

class ConductorSuite extends FunSuite with ShouldMatchers {

  val baseLineNumber = 24

  test("if conduct is called twice, the second time it throws an NotAllowedException") {
    val conductor = new Conductor
    conductor.conduct()
    val caught = intercept[NotAllowedException] { conductor.conduct() }
    caught.getMessage should be ("A Conductor's conduct method can only be invoked once.")
    caught.failedCodeFileNameAndLineNumberString match {
      case Some(s) => s should equal ("ConductorSuite.scala:" + (baseLineNumber + 5))
      case None => fail("Didn't produce a file name and line number string: ", caught)
    }
  }

  test("if conduct has not been called, conductingHasBegun should return false"){
    val conductor = new Conductor
    conductor.conductingHasBegun should be (false)
  }

  test("if conduct has been called, conductingHasBegun should return true") {
    val conductor = new Conductor
    conductor.conduct
    conductor.conductingHasBegun should be (true)
  }

  test("if thread {} is called after the test has been conducted, it throws an NotAllowedException" +
           "with a detail message that explains the problem") {
    val conductor = new Conductor
    conductor.conduct
    val caught =
      intercept[NotAllowedException] {
        conductor.thread("name") { 1 should be (1) }
      }
    caught.getMessage should be ("Cannot invoke the thread method on Conductor after its multi-threaded test has completed.")
    caught.failedCodeFileNameAndLineNumberString match {
      case Some(s) => s should equal ("ConductorSuite.scala:" + (baseLineNumber + 30))
      case None => fail("Didn't produce a file name and line number string: ", caught)
    }
  }

  test("if thread(String) {} is called after the test has been conducted, it throws NotAllowedException" +
          "with a detail message that explains the problem"){
    val conductor = new Conductor    
    conductor.conduct
    val caught =
      intercept[NotAllowedException] {
        conductor.thread("name") { 1 should be (1) }
      }
    caught.getMessage should be ("Cannot invoke the thread method on Conductor after its multi-threaded test has completed.")
    caught.failedCodeFileNameAndLineNumberString match {
      case Some(s) => s should equal ("ConductorSuite.scala:" + (baseLineNumber + 45))
      case None => fail("Didn't produce a file name and line number string: ", caught)
    }
  }

  test("if whenFinished is called twice on the same conductor, a NotAllowedException is thrown that explains it " +
          "can only be called once") {
    val conductor = new Conductor    
    conductor.whenFinished { 1 should be (1) }
    val caught =
      intercept[NotAllowedException] {
        conductor.whenFinished { 1 should be (1) }
      }
    caught.getMessage should be ("Cannot invoke whenFinished after conduct (which is called by whenFinished) has been invoked.")
    caught.failedCodeFileNameAndLineNumberString match {
      case Some(s) => s should equal ("ConductorSuite.scala:" + (baseLineNumber + 60))
      case None => fail("Didn't produce a file name and line number string: ", caught)
    }
  }

  test("if thread(String) is called twice with the same String name, the second invocation results " +
          "in an IllegalArgumentException that explains each thread in a multi-threaded test " +
          "must have a unique name") {

    val conductor = new Conductor
    conductor.thread("Fiesta del Mar") { 1 should be (1) }
    val caught =
      intercept[NotAllowedException] {
        conductor.thread("Fiesta del Mar") { 2 should be (2) }
      }
    caught.getMessage should be ("Cannot register two threads with the same name. Duplicate name: Fiesta del Mar.")
    caught.failedCodeFileNameAndLineNumberString match {
      case Some(s) => s should equal ("ConductorSuite.scala:" + (baseLineNumber + 77))
      case None => fail("Didn't produce a file name and line number string: ", caught)
    }
  }

  test("waitForBeat throws NotAllowedException if is called with zero or a negative number") {
    val conductor = new Conductor
    val caught =
      intercept[NotAllowedException] {
        conductor.waitForBeat(0)
      }
    caught.failedCodeFileNameAndLineNumberString match {
      case Some(s) => s should equal ("ConductorSuite.scala:" + (baseLineNumber + 90))
      case None => fail("Didn't produce a file name and line number string: ", caught)
    }
    val caught2 =
      intercept[NotAllowedException] {
        conductor.waitForBeat(-1)
      }
    caught2.failedCodeFileNameAndLineNumberString match {
      case Some(s) => s should equal ("ConductorSuite.scala:" + (baseLineNumber + 98))
      case None => fail("Didn't produce a file name and line number string: ", caught)
    }
  }

  test("If a non-positive number is passed to conduct for clockPeriod, it will throw NotAllowedException") (pending)

  test("If a non-positive number is passed to conduct for runLimit, it will throw NotAllowedException") (pending)

 // TODO: I think withConductorFrozen may just be returning a function rather
 // than executing it? Judging from the inferred result type. Write a test
 // that makes sure the function actually gets invoked before withConductorFrozen
 // returns.
}
