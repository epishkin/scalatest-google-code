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

  test("if conduct is called twice, the second time it throws an IllegalStateException") {
    val conductor = new Conductor
    conductor.conduct()
    intercept[IllegalStateException] { conductor.conduct() }
  }

  test("if conduct has not been called, testWasStarted should return false"){
    val conductor = new Conductor
    conductor.conductingHasBegun should be (false)
  }

  test("if conduct has been called, testWasStarted should return true") {
    val conductor = new Conductor
    conductor.conduct
    conductor.conductingHasBegun should be (true)
  }

  test("if thread {} is called after the test has been conducted, it throws an IllegalStateException" +
           "with a detail message that explains the problem") {
    val conductor = new Conductor
    conductor.conduct
    intercept[IllegalStateException] {
      conductor.thread("name"){ 1 should be (1) }
    }.getMessage should be ("Test already completed.")
  }

  test("if thread(String) {} is called after the test has been conducted, it throws IllegalStateException" +
          "with a detail message that explains the problem"){
    val conductor = new Conductor    
    conductor.conduct
    intercept[IllegalStateException] {
      conductor.thread("name"){ 1 should be (1) }
    }.getMessage should be ("Test already completed.")
  }

  test("if whenFinished is called twice on the same conductor, an IllegalStateException is thrown that explains it" +
          "can only be called once"){
    val conductor = new Conductor    
    conductor.whenFinished{ 1 should be (1) }
    intercept[IllegalStateException] {
      conductor.whenFinished{ 1 should be (1) }
    }.getMessage should be ("Conductor can only be run once!")
  }

  test("if thread(String) is called twice with the same String name, the second invocation results" +
          "in an IllegalArgumentException that explains each thread in a multi-threaded test" +
          "must have a unique name") (pending)

 // TODO: I think withConductorFrozen may just be returning a function rather
 // than executing it? Judging from the inferred result type. Write a test
 // that makes sure the function actually gets invoked before withConductorFrozen
 // returns.
}
