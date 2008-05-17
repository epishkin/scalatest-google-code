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
    var beforeSuiteCalledBeforeExecute = false
    var afterSuiteCalledAfterExecute = false
    override def beforeSuite() {
      if (!executeWasCalled)
        beforeSuiteCalledBeforeExecute = true
    }
    override def before() {
      if (!runTestWasCalled)
        beforeCalledBeforeRunTest = true
    }
    def testSomething() = ()
    override def after() {
      if (runTestWasCalled)
        afterCalledAfterRunTest = true
    }
    override def afterSuite() {
      if (executeWasCalled)
        afterSuiteCalledAfterExecute = true
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

  specify("beforeSuite gets called before execute") {
    val a = new MySuite
    a.execute()
    assert(a.beforeSuiteCalledBeforeExecute)
  }
  
  specify("afterSuite gets called after execute") {
    val a = new MySuite
    a.execute()
    assert(a.afterSuiteCalledAfterExecute)
  }
  
  specify("If any invocation of before completes abruptly with an exception, runTest " +
    "will complete abruptly with the same exception.") {
    
    class MySuite extends ImpSuite {
      override def before() { throw new NumberFormatException } 
    }
    intercept(classOf[NumberFormatException]) {
      val a = new MySuite
      a.runTest("july", new Reporter {}, new Stopper {}, Map())
    }
  }
  
  specify("If any call to super.runTest completes abruptly with an exception, runTest " +
    "will complete abruptly with the same exception, however, before doing so, it will invoke after") {
    trait FunkySuite extends Suite {
      override def runTest(testName: String, reporter: Reporter, stopper: Stopper, properties: Map[String, Any]) {
        throw new NumberFormatException
      }
    }
    class MySuite extends FunkySuite with ImpSuite {
      var afterCalled = false
      override def after() {
        afterCalled = true
      }
    }
    val a = new MySuite
    intercept(classOf[NumberFormatException]) {
      a.runTest("july", new Reporter {}, new Stopper {}, Map())
    }
    assert(a.afterCalled)
  }
  
  specify("If any call to super.runTest completes abruptly with an exception, runTest " +
    "will complete abruptly with the same exception, however, before doing so, it will invoke after") {
    trait FunkySuite extends Suite {
      override def runTest(testName: String, reporter: Reporter, stopper: Stopper, properties: Map[String, Any]) {
        throw new NumberFormatException
      }
    }
    class MySuite extends FunkySuite with ImpSuite {
      var afterCalled = false
      override def after() {
        afterCalled = true
      }
    }
    val a = new MySuite
    intercept(classOf[NumberFormatException]) {
      a.runTest("july", new Reporter {}, new Stopper {}, Map())
    }
    assert(a.afterCalled)
  }
}
