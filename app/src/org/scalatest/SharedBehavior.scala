package org.scalatest

import NodeFamily._

trait SharedBehavior {

  private val trunk = new Trunk
  /*private case class Example(exampleName: String, f: () => Unit) */

  private var examples: List[Example] = Nil

  class Inifier(exampleName: String) {
    def in(f: => Unit) {
      examples ::= Example(trunk, exampleName, f _)
    }
  }
  
  class Itifier {
    def should(exampleName: String) = new Inifier(exampleName)
  }

  protected def it = new Itifier
  
  def execute(reporter: Reporter, stopper: Stopper, prefix: String, runExample: (Example, Reporter) => Unit) {
    val examplesToExec: List[Example] =
      if (prefix.trim.isEmpty)
        examples
      else {
        val desc = Description(trunk, prefix)
        examples.map(ex => Example(desc, ex.exampleName, ex.f))
      }
    examplesToExec.reverse.foreach(runExample(_, reporter))
  }
  
  def expectedExampleCount: Int = examples.size
  
  /*private def runExample(example: Example, reporter: Reporter, r: String => String) {

    if (example == null || reporter == null)
      throw new NullPointerException

    val wrappedReporter = reporter // wrapReporterIfNecessary(reporter)

    val report = new Report(getTestNameForReport(example.exampleName), "")

    wrappedReporter.testStarting(report)

    try {

      example.f()

      val report = new Report(getTestNameForReport(example.exampleName), "")

      wrappedReporter.testSucceeded(report)
    }
    catch { 
      case e: Exception => {
        handleFailedTest(e, false, example.exampleName, None, wrappedReporter, getTestNameForReport)
      }
      case ae: AssertionError => {
        handleFailedTest(ae, false, example.exampleName, None, wrappedReporter, getTestNameForReport)
      }
    }
  }

  private def handleFailedTest(t: Throwable, hasPublicNoArgConstructor: Boolean, testName: String,
      rerunnable: Option[Rerunnable], reporter: Reporter, getTestNameForReport: String => String) {

    val msg =
      if (t.getMessage != null) // [bv: this could be factored out into a helper method]
        t.getMessage
      else
        t.toString

    val report = new Report(getTestNameForReport(testName), msg, Some(t), None)

    reporter.testFailed(report)
  }
  */
}
