import java.io.File
import java.io.FileWriter
import java.io.BufferedWriter

object GenFunSuiteN extends Application {

val template = """/*
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
package org.scalatest.fun

import scala.collection.immutable.ListSet
import java.util.ConcurrentModificationException
import java.util.concurrent.atomic.AtomicReference

/**
$if (moreThanOne)$
 * A suite of tests in which each test is represented as a function value that takes $num$ parameters that
 * are intended to serve as fixture objects for the suite's tests. Clients must parameterize <code>FunSuite$num$</code> with the types of each 
 * of the fixture objects. Most often this will be done explicitly by subclasses that have tests that need the fixture objects. Here's an example:
$else$
 * A suite of tests in which each test is represented as a function value that takes 1 parameter that
 * is intended to serve as a fixture object for the suite's tests. Clients must parameterize <code>FunSuite1</code> with the type of the
 * fixture object. Most often this will be done explicitly by subclasses that have tests that need the fixture object. Here's an example:
$endif$
 <pre>
 * class MySuite extends FunSuite$num$[$exampleParams$] {
 *
 *   testWithFixture("example test") {
 *     ($exampleArgs$) => {
 *       // test code that uses the passed fixture object$if (moreThanOne)$s$endif$...
 *     }
 *   }
 *
 *   def withFixture(f: ($exampleParams$) => Unit) {
 *
 *      // Create the fixture objects
 *      $argDefinitions$
 *
 *      // Pass the fixture objects to the test function
 *      f($exampleArgs$)
 *   }
 * }
 * </pre>
 */
trait FunSuite$num$[$typeParams$] extends Suite {

  // Until it shows up in Predef
  private def require(b: Boolean, msg: String) { if (!b) throw new IllegalArgumentException(msg) }

  private val IgnoreGroupName = "org.scalatest.Ignore"

  private trait Test
  private case class PlainOldTest(testName: String, f: () => Unit) extends Test
  private case class ReporterTest(testName: String, f: (Reporter) => Unit) extends Test
  private case class FixtureTest(msg: String, f: ($typeParams$) => Unit) extends Test
  private case class FixtureReporterTest(msg: String, f: ($typeParams$, Reporter) => Unit) extends Test

  // Access to the testNamesList, testsMap, and groupsMap must be synchronized, because the test methods are invoked by
  // the primary constructor, but testNames, groups, and runTest get invoked directly or indirectly
  // by execute. When running tests concurrently with ScalaTest Runner, different threads can
  // instantiate and execute the Suite. Instead of synchronizing, I put them in an immutable Bundle object (and
  // all three collections--testNamesList, testsMap, and groupsMap--are immuable collections), then I put the Bundle
  // in an AtomicReference. Since the expected use case is the test, testWithReporter, etc., methods will be called
  // from the primary constructor, which will be all done by one thread, I just in effect use optimistic locking on the Bundle.
  // If two threads every called test at the same time, they could get a ConcurrentModificationException.
  // Test names are in reverse order of test registration method invocations
  private class Bundle private(val testNamesList: List[String], val testsMap: Map[String, Test], val groupsMap: Map[String, Set[String]]) {
    def unpack = (testNamesList, testsMap, groupsMap)
  }
  private object Bundle {
    def apply(testNamesList: List[String], testsMap: Map[String, Test], groupsMap: Map[String, Set[String]]): Bundle =
      new Bundle(testNamesList, testsMap, groupsMap)
  }

  private val atomic = new AtomicReference[Bundle](Bundle(Nil, Map(), Map()))

  private def updateAtomic(oldBundle: Bundle, newBundle: Bundle) {
    if (!atomic.compareAndSet(oldBundle, newBundle))
      throw new ConcurrentModificationException
  }

  protected def withFixture(f: ($typeParams$) => Unit) // this must be abstract

  protected def test(testName: String, testGroups: Group*)(f: => Unit) {

    val oldBundle = atomic.get
    var (testNamesList, testsMap, groupsMap) = oldBundle.unpack

    require(!testsMap.keySet.contains(testName), "Duplicate test name: " + testName)

    testsMap += (testName -> PlainOldTest(testName, f _))
    testNamesList ::= testName
    val groupNames = Set[String]() ++ testGroups.map(_.name)
    if (!groupNames.isEmpty)
      groupsMap += (testName -> groupNames)

    updateAtomic(oldBundle, Bundle(testNamesList, testsMap, groupsMap))
  }

  protected def testWithReporter(testName: String, testGroups: Group*)(f: (Reporter) => Unit) {

    val oldBundle = atomic.get
    var (testNamesList, testsMap, groupsMap) = oldBundle.unpack

    require(!testsMap.keySet.contains(testName), "Duplicate test name: " + testName)

    testsMap += (testName -> ReporterTest(testName, f))
    testNamesList ::= testName
    val groupNames = Set[String]() ++ testGroups.map(_.name)
    if (!groupNames.isEmpty)
      groupsMap += (testName -> groupNames)

    updateAtomic(oldBundle, Bundle(testNamesList, testsMap, groupsMap))
  }

  protected def ignore(testName: String, testGroups: Group*)(f: => Unit) {

    test(testName)(f) // Call test without passing the groups

    val oldBundle = atomic.get
    var (testNamesList, testsMap, groupsMap) = oldBundle.unpack

    val groupNames = Set[String]() ++ testGroups.map(_.name)
    groupsMap += (testName -> (groupNames + IgnoreGroupName))

    updateAtomic(oldBundle, Bundle(testNamesList, testsMap, groupsMap))
  }

  protected def ignoreWithReporter(testName: String, testGroups: Group*)(f: (Reporter) => Unit) {

    testWithReporter(testName)(f) // Call testWithReporter without passing the groups

    val oldBundle = atomic.get
    var (testNamesList, testsMap, groupsMap) = oldBundle.unpack

    val groupNames = Set[String]() ++ testGroups.map(_.name)
    groupsMap += (testName -> (groupNames + IgnoreGroupName))

    updateAtomic(oldBundle, Bundle(testNamesList, testsMap, groupsMap))
  }

  protected def testWithFixture(testName: String, testGroups: Group*)(f: ($typeParams$) => Unit) {

    val oldBundle = atomic.get
    var (testNamesList, testsMap, groupsMap) = oldBundle.unpack

    require(!testsMap.keySet.contains(testName), "Duplicate test name: " + testName)

    testsMap = testsMap + (testName -> FixtureTest(testName, f))
    testNamesList ::= testName
    val groupNames = Set[String]() ++ testGroups.map(_.name)
    if (!groupNames.isEmpty)
      groupsMap += (testName -> groupNames)

    updateAtomic(oldBundle, Bundle(testNamesList, testsMap, groupsMap))
  }

  protected def testWithFixtureAndReporter(testName: String, testGroups: Group*)(f: ($typeParams$, Reporter) => Unit) {

    val oldBundle = atomic.get
    var (testNamesList, testsMap, groupsMap) = oldBundle.unpack

    require(!testsMap.keySet.contains(testName), "Duplicate test name: " + testName)

    testsMap = testsMap + (testName -> FixtureReporterTest(testName, f))
    testNamesList ::= testName
    val groupNames = Set[String]() ++ testGroups.map(_.name)
    if (!groupNames.isEmpty)
      groupsMap += (testName -> groupNames)

    updateAtomic(oldBundle, Bundle(testNamesList, testsMap, groupsMap))
  }

  protected def ignoreWithFixture(testName: String, testGroups: Group*)(f: ($typeParams$) => Unit) {

    testWithFixture(testName)(f) // Call test without passing the groups

    val oldBundle = atomic.get
    var (testNamesList, testsMap, groupsMap) = oldBundle.unpack

    val groupNames = Set[String]() ++ testGroups.map(_.name)
    groupsMap += (testName -> (groupNames + IgnoreGroupName))

    updateAtomic(oldBundle, Bundle(testNamesList, testsMap, groupsMap))
  }

  protected def ignoreWithFixtureAndReporter(testName: String, testGroups: Group*)(f: ($typeParams$, Reporter) => Unit) {

    testWithFixtureAndReporter(testName)(f) // Call testWithReporter without passing the groups

    val oldBundle = atomic.get
    var (testNamesList, testsMap, groupsMap) = oldBundle.unpack

    val groupNames = Set[String]() ++ testGroups.map(_.name)
    groupsMap += (testName -> (groupNames + IgnoreGroupName))

    updateAtomic(oldBundle, Bundle(testNamesList, testsMap, groupsMap))
  }

  override def testNames: Set[String] = {
    ListSet(atomic.get.testNamesList.toArray: _*)
  }

  protected override def runTest(testName: String, reporter: Reporter, stopper: Stopper, properties: Map[String, Any]) {

    if (testName == null || reporter == null || stopper == null || properties == null)
      throw new NullPointerException

    val wrappedReporter = reporter

    val report = new Report(testName, this.getClass.getName)

    wrappedReporter.testStarting(report)

    try {

      atomic.get.testsMap(testName) match {
        case PlainOldTest(testName, f) => f()
        case ReporterTest(testName, f) => f(reporter)
        case FixtureTest(msg, f) => withFixture(f)
        case FixtureReporterTest(msg, f) => withFixture(f($underscores$, reporter))
      }

      val report = new Report(getTestNameForReport(testName), this.getClass.getName)

      wrappedReporter.testSucceeded(report)
    }
    catch { 
      case e: Exception => {
        handleFailedTest(e, false, testName, None, wrappedReporter)
      }
      case ae: AssertionError => {
        handleFailedTest(ae, false, testName, None, wrappedReporter)
      }
    }
  }

  private def handleFailedTest(t: Throwable, hasPublicNoArgConstructor: Boolean, testName: String,
      rerunnable: Option[Rerunnable], reporter: Reporter) {

    val msg =
      if (t.getMessage != null) // [bv: this could be factored out into a helper method]
        t.getMessage
      else
        t.toString

    val report = new Report(getTestNameForReport(testName), msg, Some(t), None)

    reporter.testFailed(report)
  }

  override def groups: Map[String, Set[String]] = atomic.get.groupsMap
}
"""

  val typeParams = Array(
    "F",
    "F, U",
    "F, U, N",
    "F, U, N, C",
    "F, U, N, C, T",
    "F, U, N, C, T, I",
    "F, U, N, C, T, I, O",
    "F, U, N, C, T, I, O, NA",
    "F, U, N, C, T, I, O, NA, L"
  )

  val underscores = Array(
    "_",
    "_, _",
    "_, _, _",
    "_, _, _, _",
    "_, _, _, _, _",
    "_, _, _, _, _, _",
    "_, _, _, _, _, _, _",
    "_, _, _, _, _, _, _, _",
    "_, _, _, _, _, _, _, _, _"
  )

  val exampleParams = Array(
    "Float",
    "Float, Int",
    "Float, Int, Long",
    "Float, Int, Long, String",
    "Float, Int, Long, String, Boolean",
    "Float, Int, Long, String, Boolean, Short",
    "Float, Int, Long, String, Boolean, Short, Double",
    "Float, Int, Long, String, Boolean, Short, Double, Byte",
    "Float, Int, Long, String, Boolean, Short, Double, Byte, String"
  )

  val exampleArgs = Array(
    "f",
    "f, i",
    "f, i, x",
    "f, i, x, t",
    "f, i, x, t, ",
    "f, i, x, t, u, r",
    "f, i, x, t, u, r, e",
    "f, i, x, t, u, r, e, o",
    "f, i, x, t, u, r, e, o, b"
  )

  val argDefinitions = Array(
    "val f = 1.1f",
    "val f = 1.1f<br />      val float = 1.1f",
    "val f = 1.1f<br />      val i = 7<br />      val x = 21L",
    "val f = 1.1f<br />      val i = 7<br />      val x = 21L<br />      val t = \"hi\"",
    "val f = 1.1f<br />      val i = 7<br />      val x = 21L<br />      val t = \"hi\"<br />      val u = true",
    "val f = 1.1f<br />      val i = 7<br />      val x = 21L<br />      val t = \"hi\"<br />      val u = true, val r = 2",
    "val f = 1.1f<br />      val i = 7<br />      val x = 21L<br />      val t = \"hi\"<br />      val u = true<br />      val r = 2<br />      val e = 2.0",
    "val f = 1.1f<br />      val i = 7<br />      val x = 21L<br />      val t = \"hi\"<br />      val u = true<br />      val r = 2<br />      val e = 2.0<br />      val o = 128",
    "val f = 1.1f<br />      val i = 7<br />      val x = 21L<br />      val t = \"hi\"<br />      val u = true<br />      val r = 2<br />      val e = 2.0<br />      val o = 128<br />      val b = \"fixtures are easy!\""
  )

  val dir = new File("build/generated/org/scalatest/fun")
  dir.mkdirs()
  for (i <- 1 to 9) {
    val bw = new BufferedWriter(new FileWriter("build/generated/org/scalatest/fun/FunSuite" + i + ".scala"))
    try {
      val st = new org.antlr.stringtemplate.StringTemplate(template)
      st.setAttribute("moreThanOne", i != 1);
      st.setAttribute("moreThanFour", i > 4);
      st.setAttribute("num", i);
      st.setAttribute("typeParams", typeParams(i - 1));
      st.setAttribute("exampleParams", exampleParams(i - 1));
      st.setAttribute("exampleArgs", exampleArgs(i - 1));
      st.setAttribute("argDefinitions", argDefinitions(i - 1));
      st.setAttribute("underscores", underscores(i - 1));
      bw.write(st.toString)
    }
    finally {
      bw.close()
    }
  }
}

/*
$if (moreThanFour)$
 * <pre>
 * class MySuite extends FunSuite$num$[
 *   $exampleParams$
 * ] {
$else$
*/
