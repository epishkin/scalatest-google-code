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

import tools.DistributedTestRunnerSuite

trait ParallelTestExecution extends OneInstancePerTest {

  this: Suite =>

  private[scalatest] def runOneTest(testName: String, reporter: Reporter, stopper: Stopper,
                         config: Map[String, Any], tracker: Tracker) {

    runTest(testName, reporter, stopper, config, tracker)
  }

  protected abstract override def runTests(testName: Option[String], reporter: Reporter, stopper: Stopper, filter: Filter,
                             config: Map[String, Any], distributor: Option[Distributor], tracker: Tracker) {


    // testName distributor
    //    None    None      call super, because no distributor
    //    Some    None      call super, because no distributor
    //    None    Some      wrap a newInstance and put it in the distributor
    //    Some    Some      this would be the one where we need to actually run the test, ignore the distributor
    distributor match {
      // If there's no distributor, then just run sequentially, via the regular OneInstancePerTest
      // algorithm
      case None => super.runTests(testName, reporter,stopper, filter, config, distributor, tracker)
      case Some(distribute) =>
        testName match {
          // The only way both testName and distributor should be defined is if someone called from the
          // outside and did this. Fir run is called with testName None and a defined Distributor, it
          // will not get here. So in this case, just do the usual OneInstancePerTest thing.
          // TODO: Make sure it doesn't get back here. Walk through the scenarios.
          case Some(tn) => super.runTests(testName, reporter, stopper, filter, config, distributor, tracker)
          case None =>
            for (tn <- testNames) {
              val wrappedInstance =
                new DistributedTestRunnerSuite(
                  newInstance.asInstanceOf[ParallelTestExecution],
                  tn
                )
              distribute(wrappedInstance, tracker.nextTracker)
            }
        }
    }
  }
}
