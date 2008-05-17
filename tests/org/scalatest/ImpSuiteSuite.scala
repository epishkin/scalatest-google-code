package org.scalatest

import org.scalatest.fun.FunSuite
import scala.collection.mutable.ListBuffer

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
  
  // test exceptions with runTest
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
  
  specify("If both super.runTest and after complete abruptly with an exception, runTest " + 
    "will complete abruptly with the exception thrown by super.runTest.") {
    trait FunkySuite extends Suite {
      override def runTest(testName: String, reporter: Reporter, stopper: Stopper, properties: Map[String, Any]) {
        throw new NumberFormatException
      }
    }
    class MySuite extends FunkySuite with ImpSuite {
      var afterCalled = false
      override def after() {
        afterCalled = true
        throw new IllegalArgumentException
      }
    }
    val a = new MySuite
    intercept(classOf[NumberFormatException]) {
      a.runTest("july", new Reporter {}, new Stopper {}, Map())
    }
    assert(a.afterCalled)
  }
  
  specify("If super.runTest returns normally, but after completes abruptly with an " +
    "exception, runTest will complete abruptly with the same exception.") {
       
    class MySuite extends ImpSuite {
      override def after() { throw new NumberFormatException }
      def testJuly() = ()
    }
    intercept(classOf[NumberFormatException]) {
      val a = new MySuite
      a.runTest("testJuly", new Reporter {}, new Stopper {}, Map())
    }
  }
 
  // test exceptions with execute
  specify("If any invocation of beforeSuite completes abruptly with an exception, execute " +
    "will complete abruptly with the same exception.") {
    
    class MySuite extends ImpSuite {
      override def beforeSuite() { throw new NumberFormatException }
      def testJuly() = ()
    }
    intercept(classOf[NumberFormatException]) {
      val a = new MySuite
      a.execute(None, new Reporter {}, new Stopper {}, Set(), Set(), Map(), None)
    }
  }
 
  specify("If any call to super.execute completes abruptly with an exception, execute " +
    "will complete abruptly with the same exception, however, before doing so, it will invoke afterSuite") {
    trait FunkySuite extends Suite {
      override def execute(testName: Option[String], reporter: Reporter, stopper: Stopper, includes: Set[String], excludes: Set[String],
                           properties: Map[String, Any], distributor: Option[Distributor]) {
        throw new NumberFormatException
      }
    }
    class MySuite extends FunkySuite with ImpSuite {
      var afterSuiteCalled = false
      override def afterSuite() {
        afterSuiteCalled = true
      }
    }
    val a = new MySuite
    intercept(classOf[NumberFormatException]) {
      a.execute(None, new Reporter {}, new Stopper {}, Set(), Set(), Map(), None)
    }
    assert(a.afterSuiteCalled)
  }
   
  specify("If both super.execute and afterSuite complete abruptly with an exception, execute " + 
    "will complete abruptly with the exception thrown by super.execute.") {
    trait FunkySuite extends Suite {
      override def execute(testName: Option[String], reporter: Reporter, stopper: Stopper, includes: Set[String], excludes: Set[String],
                           properties: Map[String, Any], distributor: Option[Distributor]) {
        throw new NumberFormatException
      }
    }
    class MySuite extends FunkySuite with ImpSuite {
      var afterSuiteCalled = false
      override def afterSuite() {
        afterSuiteCalled = true
        throw new IllegalArgumentException
      }
    }
    val a = new MySuite
    intercept(classOf[NumberFormatException]) {
      a.execute(None, new Reporter {}, new Stopper {}, Set(), Set(), Map(), None)
    }
    assert(a.afterSuiteCalled)
  }
  
  specify("If super.execute returns normally, but afterSuite completes abruptly with an " +
    "exception, execute will complete abruptly with the same exception.") {
       
    class MySuite extends ImpSuite {
      override def afterSuite() { throw new NumberFormatException }
      def testJuly() = ()
    }
    intercept(classOf[NumberFormatException]) {
      val a = new MySuite
      a.execute(None, new Reporter {}, new Stopper {}, Set(), Set(), Map(), None)
    }
  }
}

class ImpSuiteExtendingSuite extends ImpSuite {

  var sb: StringBuilder = _
  val lb = new ListBuffer[String]

  override def before() {
    sb = new StringBuilder("ScalaTest is ")
    lb.clear()
  }

  def testEasy() {
    sb.append("easy!")
    assert(sb.toString === "ScalaTest is easy!")
    assert(lb.isEmpty)
    lb += "sweet"
  }

  def testFun() {
    sb.append("fun!")
    assert(sb.toString === "ScalaTest is fun!")
    assert(lb.isEmpty)
  }
}

class ImpSuiteExtendingFunSuite extends FunSuite with ImpSuite {

  var sb: StringBuilder = _
  val lb = new ListBuffer[String]

  override def before() {
    sb = new StringBuilder("ScalaTest is ")
    lb.clear()
  }

  test("easy") {
    sb.append("easy!")
    assert(sb.toString === "ScalaTest is easy!")
    assert(lb.isEmpty)
    lb += "sweet"
  }

  test("fun") {
    sb.append("fun!")
    assert(sb.toString === "ScalaTest is fun!")
    assert(lb.isEmpty)
  }
}


