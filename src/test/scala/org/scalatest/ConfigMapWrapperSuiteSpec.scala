package org.scalatest

class ConfigMapWrapperSuiteSpec extends FunSuite with SharedHelpers {

  // Need a test that ensures the passed config map gets in there.
  test("configMap should get passed into the wrapped Suite") {
    SavesConfigMapSuite.resetConfigMap()
    val wrapped = new ConfigMapWrapperSuite(classOf[SavesConfigMapSuite])
    val configMap = Map("salt" -> "pepper", "eggs" -> "bacon")
    wrapped.run(None, SilentReporter, new Stopper {}, Filter(), configMap, None, new Tracker)
    assert(SavesConfigMapSuite.savedConfigMap === Some(configMap))
  }
}