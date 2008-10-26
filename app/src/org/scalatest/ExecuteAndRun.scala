package org.scalatest

trait ExecuteAndRun {
  def execute(
    testName: Option[String],
    reporter: Reporter,
    stopper: Stopper,
    groupsToInclude: Set[String],
    groupsToExclude: Set[String],
    goodies: Map[String, Any],
    distributor: Option[Distributor]
  )
  protected def runTest(
    testName: String,
    reporter: Reporter,
    stopper: Stopper,
    goodies: Map[String, Any]
  )
}
