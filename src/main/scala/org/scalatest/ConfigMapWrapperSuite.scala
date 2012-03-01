package org.scalatest

final class ConfigMapWrapperSuite(clazz: Class[_ <: Suite]) extends Suite {
  override def run(testName: Option[String], reporter: Reporter, stopper: Stopper, filter: Filter,
      configMap: Map[String, Any], distributor: Option[Distributor], tracker: Tracker) {
    val constructor = clazz.getConstructor(classOf[Map[_, _]])
    val suite = constructor.newInstance(configMap)
    suite.run(testName, reporter, stopper, filter, configMap, distributor, tracker)
  }
}