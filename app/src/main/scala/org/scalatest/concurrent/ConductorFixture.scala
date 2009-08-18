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

import org.scalatest.fixture.FixtureSuite

/**
 * Trait that can pass a new <code>Conductor</code> fixture into tests.
 *
 * @author Bill Venners
 */
trait ConductorFixture { this: FixtureSuite =>


  /**
   * Defines type <code>Fixture</code> to be <code>Conductor</code>.
   */
  type Fixture = Conductor
  
  /**
   * Creates a new <code>Conductor</code>, passes the <code>Conductor</code> to the
   * specified test function, and ensures that <code>conductTest</code> gets invoked
   * on the <code>Conductor</code>.
   *
   * <p>
   * After the test function returns (so long as it returns normally and doesn't
   * complete abruptly with an exception), this method will determine whether the
   * <code>conductTest</code> method has already been called (by invoking
   * <code>conductTestWasCalled</code> on the <code>Conductor</code>). If not,
   * this method will invoke <code>conductTest</code> to ensure that the
   * multi-threaded test is actually conducted.
   * </p>
   *
   */
  def withFixture(fun: TestFunction) {
    val conductor = new Conductor
    fun(conductor)
    if (!conductor.conductTestWasCalled)
      conductor.conductTest()
  }
}