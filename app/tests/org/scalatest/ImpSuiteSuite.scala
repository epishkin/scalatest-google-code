package org.scalatest

import org.scalatest.fun.FunSuite

class ImpSuiteSuite extends FunSuite {

  class TheSuper extends Suite {
    var runTestWasCalled = false
    var executeWasCalled = false
    override def runTest(testName: String, reporter: Reporter, stopper: Stopper, properties: Map[String, Any]) {
      runTestWasCalled = true
      super.runTest(testName, reporter, stopper, properties)
    }
    override def execute(testName: Option[String], reporter: Reporter, stopper: Stopper, includes: Set[String], excludes: Set[String],
                         properties: Map[String, Any], distributor: Option[Distributor]) {
      executeWasCalled = true
      super.execute(testName, reporter, stopper, includes, excludes, properties, distributor)
    }
  }
  
  class MySuite extends TheSuper with ImpSuite {
    var beforeCalledBeforeRunTest = false
    var afterCalledAfterRunTest = false
    override def before() {
      if (!runTestWasCalled)
        beforeCalledBeforeRunTest = true
    }
    def testSomething() = ()
    override def after() {
      if (runTestWasCalled)
        afterCalledAfterRunTest = true
    }
  }

  specify("super's runTest must be called") {
    val a = new MySuite
    a.execute()
    assert(a.runTestWasCalled)
  }
  
  specify("super's execute must be called") {
    val a = new MySuite
    a.execute()
    assert(a.executeWasCalled)
  }

  specify("before gets called before runTest") {
    val a = new MySuite
    a.execute()
    assert(a.beforeCalledBeforeRunTest)
  }
  
  specify("after gets called after runTest") {
    val a = new MySuite
    a.execute()
    assert(a.afterCalledAfterRunTest)
  }
}
