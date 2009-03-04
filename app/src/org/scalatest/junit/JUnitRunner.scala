
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
package org.scalatest.junit

import org.junit.runner.notification.RunNotifier
import org.junit.runner.Description

class JUnitRunner(suiteClass: java.lang.Class[Suite]) extends org.junit.runner.Runner {

  private val canInstantiate = Suite.checkForPublicNoArgConstructor(suiteClass)
  require(canInstantiate, "Must pass an org.scalatest.Suite with a public no-arg constructor")

  private val suiteToRun = suiteClass.newInstance

  def getDescription() = Description.createSuiteDescription(suiteClass)

  def run(notifier: RunNotifier) {
    suiteToRun.execute(None, new RunNotifierReporter(notifier), new Stopper {}, Set(), Set(), Map(), None)
  }

  override def testCount() = suiteToRun.expectedTestCount(Set(), Set())
}

