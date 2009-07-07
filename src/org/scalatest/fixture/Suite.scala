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
import org.scalatest.Suite.checkForPublicNoArgConstructor
import org.scalatest.Suite.TestMethodPrefix
import org.scalatest.Suite.IgnoreAnnotation
import Suite.FixtureAndInformerInParens
import Suite.FixtureInParens
import Suite.testMethodTakesInformer
import Suite.simpleNameForTest
import Suite.argsArrayForTestName
import org.scalatest.events._

abstract class Suite extends org.scalatest.Suite { thisSuite =>

  type Fixture

  protected def withFixture(testFun: Fixture => Unit)

  // Need to override this one becaue it call getMethodForTestName
  override def tags: Map[String, Set[String]] = {

    def getTags(testName: String) =
/* AFTER THE DEPRECATION CYCLE FOR GROUPS TO TAGS (0.9.8), REPLACE THE FOLLOWING FOR LOOP WITH THIS COMMENTED OUT ONE
   THAT MAKES SURE ANNOTATIONS ARE TAGGED WITH TagAnnotation.
      for {
        a <- getMethodForTestName(testName).getDeclaredAnnotations
        annotationClass = a.annotationType
        if annotationClass.isAnnotationPresent(classOf[TagAnnotation])
      } yield annotationClass.getName
*/
      for (a <- getMethodForTestName(testName).getDeclaredAnnotations)
        yield a.annotationType.getName

    val elements =
      for (testName <- testNames; if !getTags(testName).isEmpty)
        yield testName -> (Set() ++ getTags(testName))

    Map() ++ elements
  }

  override def testNames: Set[String] = {

    def takesTwoParamsOfTypesObjectAndInformer(m: Method) = {
      val paramTypes = m.getParameterTypes
      val hasTwoParams = paramTypes.length == 2
      hasTwoParams &&
          classOf[Object].isAssignableFrom(paramTypes(0)) &&
          classOf[Informer].isAssignableFrom(paramTypes(1))
    }

    def takesOneParamOfTypeObject(m: Method) = {
      val paramTypes = m.getParameterTypes
      val hasOneParam = paramTypes.length == 1
      hasOneParam && classOf[Object].isAssignableFrom(paramTypes(0))
    }

    def isTestMethod(m: Method) = {

      val isInstanceMethod = !Modifier.isStatic(m.getModifiers())

      // name must have at least 4 chars (minimum is "test")
      val simpleName = m.getName
      val firstFour = if (simpleName.length >= 4) simpleName.substring(0, 4) else "" 

      // Don't need to check for testNames in this case, because will discover both
      // testNames(Object) and testNames(Object, Informer). Reason is if I didn't discover these
      // it would likely just be silently ignored, and that might waste users' time
      isInstanceMethod && (firstFour == "test") && (takesOneParamOfTypeObject(m) ||
              takesTwoParamsOfTypesObjectAndInformer(m))
    }

    val testNameArray =
      for (m <- getClass.getMethods; if isTestMethod(m)) yield
        if (takesOneParamOfTypeObject(m))
          m.getName + FixtureInParens
        else
          m.getName + FixtureAndInformerInParens

    TreeSet[String]() ++ testNameArray
  }

  protected override def runTest(testName: String, reporter: Reporter, stopper: Stopper, goodies: Map[String, Any], tracker: Tracker) {

    if (testName == null || reporter == null || stopper == null || goodies == null || tracker == null)
      throw new NullPointerException

    val stopRequested = stopper
    val report = wrapReporterIfNecessary(reporter)
    val method = getMethodForTestName(testName)

    // Create a Rerunner if the Suite has a no-arg constructor
    val hasPublicNoArgConstructor = checkForPublicNoArgConstructor(getClass)

    val rerunnable =
      if (hasPublicNoArgConstructor)
        Some(new TestRerunner(getClass.getName, testName))
      else
        None

    val testStartTime = System.currentTimeMillis

    report(TestStarting(tracker.nextOrdinal(), thisSuite.suiteName, Some(thisSuite.getClass.getName), testName, None, rerunnable))


    try {
      val testFun: Fixture => Unit = {
        (fixture: Fixture) => {
          val anyRefFixture: AnyRef = fixture.asInstanceOf[AnyRef]
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
              Array(anyRefFixture, informer)
            }
            else Array(anyRefFixture)

          method.invoke(this, args: _*)
        }
      }
      withFixture(testFun)

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
  private def getMethodForTestName(testName: String) = {
    val candidateMethods = getClass.getMethods.filter(_.getName == simpleNameForTest(testName))
    val found =
      if (testMethodTakesInformer(testName))
        candidateMethods.find(
          candidateMethod => {
            val paramTypes = candidateMethod.getParameterTypes
            paramTypes.length == 2 && paramTypes(1) == classOf[Informer]
          }
        )
      else
        candidateMethods.find(_.getParameterTypes.length == 1)
     found match {
       case Some(method) => method
       case None => throw new RuntimeException("Can't find a test method with name: " + testName)
     }
  }
}

private object Suite {

  val FixtureAndInformerInParens = "(Fixture, Informer)"
  val FixtureInParens = "(Fixture)"

  private def testMethodTakesInformer(testName: String) = testName.endsWith(FixtureAndInformerInParens)

  private def simpleNameForTest(testName: String) =
    if (testName.endsWith(FixtureAndInformerInParens))
      testName.substring(0, testName.length - FixtureAndInformerInParens.length)
    else
      testName.substring(0, testName.length - FixtureInParens.length)

  private def argsArrayForTestName(testName: String): Array[Class[_]] =
    if (testMethodTakesInformer(testName))
      Array(classOf[Object], classOf[Informer])
    else
      Array(classOf[Informer])
}
