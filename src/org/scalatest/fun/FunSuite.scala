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
package org.scalatest.fun

trait Group

abstract class FunSuite extends Suite {

  private val ReporterInParens = " (Reporter)"
  private val IgnoreGroupName = "org.scalatest.Ignore"

  private trait Test
  private case class PlainOldTest(msg: String, f: () => Unit) extends Test
  private case class ReporterTest(msg: String, f: (Reporter) => Unit) extends Test

  private var testsMap: Map[String, Test] = Map()
  private var groupsMap: Map[String, Set[String]] = Map()

  protected def test(msg: String, groupClasses: Group*)(f: => Unit) {
    assume(!testsMap.keySet.contains(msg))
    testsMap += (msg -> PlainOldTest(msg, f _))
    val groupNames = Set[String]() ++ groupClasses.map(_.getClass.getName)
    if (!groupNames.isEmpty)
      groupsMap += (msg -> groupNames)
  }

  protected def testWithReporter(msg: String, groupClasses: Group*)(f: (Reporter) => Unit) {
    assume(!testsMap.keySet.contains(msg))
    testsMap += (msg -> ReporterTest(msg, f))
    val groupNames = Set[String]() ++ groupClasses.map(_.getClass.getName)
    if (!groupNames.isEmpty)
      groupsMap += (msg -> groupNames)
  }

  protected def ignore(msg: String, groupClasses: Group*)(f: => Unit) {
    test(msg)(f _) // Call test without passing the groups
    val groupNames = Set[String]() ++ groupClasses.map(_.getClass.getName)
    groupsMap += (msg -> (groupNames + IgnoreGroupName))
  }

  protected def ignoreWithReporter(msg: String, groupClasses: Group*)(f: (Reporter) => Unit) {
    testWithReporter(msg)(f) // Call testWithReporter without passing the groups
    val groupNames = Set[String]() ++ groupClasses.map(_.getClass.getName)
    groupsMap += (msg -> (groupNames + IgnoreGroupName))
    println("testsMap: " + testsMap)
    println("groupsMap: " + groupsMap)
  }

  protected def specify(msg: String, groupClasses: Group*)(f: => Unit) {
    test(msg, groupClasses: _*)(f _) 
  }

  protected def specifyWithReporter(msg: String, groupClasses: Group*)(f: (Reporter) => Unit) {
    testWithReporter(msg, groupClasses: _*)(f) 
  }

  override def testNames: Set[String] = {
    Set() ++ testsMap.keySet
  }

  protected override def runTest(testName: String, reporter: Reporter, stopper: Stopper, properties: Map[String, Any]) {

    if (testName == null || reporter == null || stopper == null || properties == null)
      throw new NullPointerException

    val wrappedReporter = reporter

    val report = new Report(testName, this.getClass.getName)

    wrappedReporter.testStarting(report)

    try {

      testsMap(testName) match {
        case PlainOldTest(msg, f) => f()
        case ReporterTest(msg, f) => f(reporter)
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

  override def groups: Map[String, Set[String]] = groupsMap
}
