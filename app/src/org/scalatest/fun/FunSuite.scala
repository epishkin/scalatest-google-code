package org.scalatest.fun

class FunSuite extends Suite {

  private val ReporterInParens = " (Reporter)"
  private val IgnoreGroupName = "org.scalatest.Ignore"

  private trait Test
  private case class PlainOldTest(msg: String, f: () => Unit) extends Test
  private case class ReporterTest(msg: String, f: (Reporter) => Unit) extends Test

  private var testsMap: Map[String, Test] = Map()
  private var groupsMap: Map[String, Set[String]] = Map()

  protected def test(msg: String)(f: => Unit) {
    assume(!testsMap.keySet.contains(msg))
    testsMap += (msg -> PlainOldTest(msg, f _))
  }

  protected def testWithReporter(msg: String)(f: (Reporter) => Unit) {
    assume(!testsMap.keySet.contains(msg))
    testsMap += (msg -> ReporterTest(msg, f))
  }

  protected def ignore(msg: String)(f: => Unit) {
    test(msg)(f _) 
    groupsMap += (msg -> Set(IgnoreGroupName))
  }

  protected def ignoreWithReporter(msg: String)(f: (Reporter) => Unit) {
    testWithReporter(msg)(f) 
    groupsMap += (msg + ReporterInParens -> Set(IgnoreGroupName))
  }

  protected def specify(msg: String)(f: => Unit) {
    test(msg)(f _) 
    // testsMap += (msg -> PlainOldTest(msg, f _))
  }

  protected def specifyWithReporter(msg: String)(f: (Reporter) => Unit) {
    testWithReporter(msg)(f) 
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
