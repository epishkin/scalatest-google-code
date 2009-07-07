/*
 * Copyright 2001-2009 Artima, Inc.
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
package org.scalatest

/**
 * Trait that facilitates a style of testing in which each test is run in its own instance
 * of the suite class to isolate each test from the side effects of the other tests in the
 * suite.
 *
 * <p>
 * If you mix this trait into a <code>Suite</code>, you can initialize shared reassignable
 * fixture variables as well as shared mutable fixture objects in the constructor of the
 * class. Because each test will run in its own instance of the class, each test will
 * get a fresh copy of the instance variables. This is the approach to isolation taken,
 * for example, by the JUnit framework.
 * </p>
 *
 * @author Bill Venners
 */
trait OneInstancePerTest extends RunMethods {
  
  this: Suite =>

  protected abstract override def runTests(testName: Option[String], reporter: Reporter, stopper: Stopper, filter: Filter,
                             goodies: Map[String, Any], tracker: Tracker) {
    testName match {
      case Some(tn) => super.runTests(testName, reporter, stopper, filter, goodies, tracker)
      case None =>
        for (tn <- testNames) {
          val oneInstance = newInstance
          oneInstance.run(Some(tn), reporter, stopper, filter, goodies, None, tracker)
        }
    }
  }
}