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
package org.scalatest.fixture

import collection.immutable.TreeSet
import java.lang.reflect.{InvocationTargetException, Method, Modifier}
import org.scalatest.events._

abstract class Suite extends org.scalatest.Suite { thisSuite =>

  import Suite.TestMethodPrefix, Suite.InformerInParens, Suite.IgnoreAnnotation

  type Fixture

  override def testNames: Set[String] = {

    def takesInformer(m: Method) = {
      val paramTypes = m.getParameterTypes
      paramTypes.length == 1 && classOf[Informer].isAssignableFrom(paramTypes(0))
    }

    def isTestMethod(m: Method) = {

      val isInstanceMethod = !Modifier.isStatic(m.getModifiers())

      // name must have at least 4 chars (minimum is "test")
      val simpleName = m.getName
      val firstFour = if (simpleName.length >= 4) simpleName.substring(0, 4) else "" 

      val paramTypes = m.getParameterTypes
      val hasNoParams = paramTypes.length == 0

      val isTestNames = simpleName == "testNames"

      isInstanceMethod && (firstFour == "test") && ((hasNoParams && !isTestNames) || takesInformer(m))
    }

    val testNameArray =
      for (m <- getClass.getMethods; if isTestMethod(m)) 
        yield if (takesInformer(m)) m.getName + InformerInParens else m.getName

    TreeSet[String]() ++ testNameArray
  }

  protected override def runTest(testName: String, reporter: Reporter, stopper: Stopper, goodies: Map[String, Any], tracker: Tracker) {

    if (testName == null || reporter == null || stopper == null || goodies == null)
      throw new NullPointerException

    val stopRequested = stopper
    val report = wrapReporterIfNecessary(reporter)
    val method = getMethodForTestName(testName)

    // Create a Rerunner if the Suite has a no-arg constructor
    val hasPublicNoArgConstructor = Suite.checkForPublicNoArgConstructor(getClass)

    val rerunnable =
      if (hasPublicNoArgConstructor)
        Some(new TestRerunner(getClass.getName, testName))
      else
        None

    val testStartTime = System.currentTimeMillis

    report(TestStarting(tracker.nextOrdinal(), thisSuite.suiteName, Some(thisSuite.getClass.getName), testName, None, rerunnable))

    val args: Array[Object] =
      if (testMethodTakesInformer(testName)) {
        val informer =
          new Informer {
            def apply(message: String) {
              if (message == null)
                throw new NullPointerException
              report(InfoProvided(tracker.nextOrdinal(), message, Some(NameInfo(thisSuite.suiteName, Some(thisSuite.getClass.getName), Some(testName)))))
            }
          }
        Array(informer)  
      }
      else Array()

    try {
      val testFun: Fixture => Unit = (fixture: Fixture) => method.invoke(this, args: _*)
      runTestWithFixture(testName, reporter, stopper, goodies, tracker, testFun)

      val duration = System.currentTimeMillis - testStartTime
      report(TestSucceeded(tracker.nextOrdinal(), thisSuite.suiteName, Some(thisSuite.getClass.getName), testName, Some(duration), None, rerunnable))
    }
    catch { 
      case ite: InvocationTargetException =>
        val t = ite.getTargetException
        t match {
          case _: TestPendingException =>
            report(TestPending(tracker.nextOrdinal(), thisSuite.suiteName, Some(thisSuite.getClass.getName), testName))
          case _ =>
            val duration = System.currentTimeMillis - testStartTime
            handleFailedTest(t, hasPublicNoArgConstructor, testName, rerunnable, report, tracker, duration)
        }

      case e: Exception => {
        val duration = System.currentTimeMillis - testStartTime
        handleFailedTest(e, hasPublicNoArgConstructor, testName, rerunnable, report, tracker, duration)
      }
      case ae: AssertionError => {
        val duration = System.currentTimeMillis - testStartTime
        handleFailedTest(ae, hasPublicNoArgConstructor, testName, rerunnable, report, tracker, duration)
      }
    }
  }

  protected def runTestWithFixture(testName: String, reporter: Reporter, stopper: Stopper, goodies: Map[String, Any],
          tracker: Tracker, testFun: Fixture => Unit)

  // TODO: This is identical with the one in Suite. Factor it out to an object somewhere.
  private def handleFailedTest(throwable: Throwable, hasPublicNoArgConstructor: Boolean, testName: String,
      rerunnable: Option[Rerunner], report: Reporter, tracker: Tracker, duration: Long) {

    val message =
      if (throwable.getMessage != null) // [bv: this could be factored out into a helper method]
        throwable.getMessage
      else
        throwable.toString

    report(TestFailed(tracker.nextOrdinal(), message, thisSuite.suiteName, Some(thisSuite.getClass.getName), testName, Some(throwable), Some(duration), None, rerunnable))
  }

  // TODO: This is also identical with the one in Suite, but it probably will change when I start looking
  // for an argument of type Fixture, which will probably have to be Object.
  private def getMethodForTestName(testName: String) =
    getClass.getMethod(
      simpleNameForTest(testName),
      (if (testMethodTakesInformer(testName)) Array(classOf[Informer]) else new Array[Class[_]](0)): _*
    )

  // TODO: This is also copied from Suite, but again will probably change when I start looking
  // for the Fixture in the signature.
  private def testMethodTakesInformer(testName: String) = testName.endsWith(InformerInParens)

  // TODO: Also copied, but also may change later
  private def simpleNameForTest(testName: String) =
    if (testName.endsWith(InformerInParens))
      testName.substring(0, testName.length - InformerInParens.length)
    else
      testName
}
