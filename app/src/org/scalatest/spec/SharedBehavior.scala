package org.scalatest.spec

trait SharedBehavior {

  /*private*/ case class Example(exampleName: String, f: () => Unit)

  /*private*/ var examples: List[Example] = Nil

  class Inifier(exampleName: String) {
    def in(f: => Unit) {
      examples ::= Example(exampleName, f _)
    }
  }
  
  class Itifier {
    def should(exampleName: String) = new Inifier(exampleName)
  }

  protected def it = new Itifier
  
  def execute(reporter: Reporter, stopper: Stopper) {
    examples.reverse.foreach { runExample(_, reporter) }
  }
  
  def expectedExampleCount: Int = 0
  
  /*private*/ def runExample(example: Example, reporter: Reporter) {

    if (example == null || reporter == null)
      throw new NullPointerException

    val wrappedReporter = reporter // wrapReporterIfNecessary(reporter)

    val report = new Report(example.exampleName /*getTestNameForReport(example.exampleName)*/, "")

    wrappedReporter.testStarting(report)

    try {

      example.f()

      val report = new Report(example.exampleName/*getTestNameForReport(example.exampleName)*/, "")

      wrappedReporter.testSucceeded(report)
    }
    catch { 
      case e: Exception => {
        handleFailedTest(e, false, example.exampleName, None, wrappedReporter)
      }
      case ae: AssertionError => {
        handleFailedTest(ae, false, example.exampleName, None, wrappedReporter)
      }
    }
  }

  /*private*/ def handleFailedTest(t: Throwable, hasPublicNoArgConstructor: Boolean, testName: String,
      rerunnable: Option[Rerunnable], reporter: Reporter) {

    val msg =
      if (t.getMessage != null) // [bv: this could be factored out into a helper method]
        t.getMessage
      else
        t.toString

    val report = new Report(testName/*getTestNameForReport(testName)*/, msg, Some(t), None)

    reporter.testFailed(report)
  }
}
