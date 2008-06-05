package org.scalatest

private[scalatest] abstract class Behavior extends Spec {
  def execute(testName: Option[String], reporter: Reporter, stopper: Stopper, includes: Set[String], excludes: Set[String],
              properties: Map[String, Any], distributor: Option[Distributor], prefix: String) {
    super.execute(testName, reporter, stopper, includes, excludes, properties, distributor)
  }
}
