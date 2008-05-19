package org.scalatest.imp

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
    var beforeEachCalledBeforeRunTest = false
    var afterEachCalledAfterRunTest = false
    var beforeAllCalledBeforeExecute = false
    var afterAllCalledAfterExecute = false
    override def beforeAll() {
      if (!executeWasCalled)
        beforeAllCalledBeforeExecute = true
    }
    override def beforeEach() {
      if (!runTestWasCalled)
        beforeEachCalledBeforeRunTest = true
    }
    def testSomething() = ()
    override def afterEach() {
      if (runTestWasCalled)
        afterEachCalledAfterRunTest = true
    }
    override def afterAll() {
      if (executeWasCalled)
        afterAllCalledAfterExecute = true
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

  specify("beforeEach gets called before runTest") {
    val a = new MySuite
    a.execute()
    assert(a.beforeEachCalledBeforeRunTest)
  }
  
  specify("afterEach gets called after runTest") {
    val a = new MySuite
    a.execute()
    assert(a.afterEachCalledAfterRunTest)
  }

  specify("beforeAll gets called before execute") {
    val a = new MySuite
    a.execute()
    assert(a.beforeAllCalledBeforeExecute)
  }
  
  specify("afterAll gets called after execute") {
    val a = new MySuite
    a.execute()
    assert(a.afterAllCalledAfterExecute)
  }
  
  // test exceptions with runTest
  specify("If any invocation of beforeEach completes abruptly with an exception, runTest " +
    "will complete abruptly with the same exception.") {
    
    class MySuite extends ImpSuite {
      override def beforeEach() { throw new NumberFormatException } 
    }
    intercept(classOf[NumberFormatException]) {
      val a = new MySuite
      a.runTest("july", new Reporter {}, new Stopper {}, Map())
    }
  }
  
  specify("If any call to super.runTest completes abruptly with an exception, runTest " +
    "will complete abruptly with the same exception, however, before doing so, it will invoke afterEach") {
    trait FunkySuite extends Suite {
      override def runTest(testName: String, reporter: Reporter, stopper: Stopper, properties: Map[String, Any]) {
        throw new NumberFormatException
      }
    }
    class MySuite extends FunkySuite with ImpSuite {
      var afterEachCalled = false
      override def afterEach() {
        afterEachCalled = true
      }
    }
    val a = new MySuite
    intercept(classOf[NumberFormatException]) {
      a.runTest("july", new Reporter {}, new Stopper {}, Map())
    }
    assert(a.afterEachCalled)
  }
  
  specify("If both super.runTest and afterEach complete abruptly with an exception, runTest " + 
    "will complete abruptly with the exception thrown by super.runTest.") {
    trait FunkySuite extends Suite {
      override def runTest(testName: String, reporter: Reporter, stopper: Stopper, properties: Map[String, Any]) {
        throw new NumberFormatException
      }
    }
    class MySuite extends FunkySuite with ImpSuite {
      var afterEachCalled = false
      override def afterEach() {
        afterEachCalled = true
        throw new IllegalArgumentException
      }
    }
    val a = new MySuite
    intercept(classOf[NumberFormatException]) {
      a.runTest("july", new Reporter {}, new Stopper {}, Map())
    }
    assert(a.afterEachCalled)
  }
  
  specify("If super.runTest returns normally, but afterEach completes abruptly with an " +
    "exception, runTest will complete abruptly with the same exception.") {
       
    class MySuite extends ImpSuite {
      override def afterEach() { throw new NumberFormatException }
      def testJuly() = ()
    }
    intercept(classOf[NumberFormatException]) {
      val a = new MySuite
      a.runTest("testJuly", new Reporter {}, new Stopper {}, Map())
    }
  }
 
  // test exceptions with execute
  specify("If any invocation of beforeAll completes abruptly with an exception, execute " +
    "will complete abruptly with the same exception.") {
    
    class MySuite extends ImpSuite {
      override def beforeAll() { throw new NumberFormatException }
      def testJuly() = ()
    }
    intercept(classOf[NumberFormatException]) {
      val a = new MySuite
      a.execute(None, new Reporter {}, new Stopper {}, Set(), Set(), Map(), None)
    }
  }
 
  specify("If any call to super.execute completes abruptly with an exception, execute " +
    "will complete abruptly with the same exception, however, before doing so, it will invoke afterAll") {
    trait FunkySuite extends Suite {
      override def execute(testName: Option[String], reporter: Reporter, stopper: Stopper, includes: Set[String], excludes: Set[String],
                           properties: Map[String, Any], distributor: Option[Distributor]) {
        throw new NumberFormatException
      }
    }
    class MySuite extends FunkySuite with ImpSuite {
      var afterAllCalled = false
      override def afterAll() {
        afterAllCalled = true
      }
    }
    val a = new MySuite
    intercept(classOf[NumberFormatException]) {
      a.execute(None, new Reporter {}, new Stopper {}, Set(), Set(), Map(), None)
    }
    assert(a.afterAllCalled)
  }
   
  specify("If both super.execute and afterAll complete abruptly with an exception, execute " + 
    "will complete abruptly with the exception thrown by super.execute.") {
    trait FunkySuite extends Suite {
      override def execute(testName: Option[String], reporter: Reporter, stopper: Stopper, includes: Set[String], excludes: Set[String],
                           properties: Map[String, Any], distributor: Option[Distributor]) {
        throw new NumberFormatException
      }
    }
    class MySuite extends FunkySuite with ImpSuite {
      var afterAllCalled = false
      override def afterAll() {
        afterAllCalled = true
        throw new IllegalArgumentException
      }
    }
    val a = new MySuite
    intercept(classOf[NumberFormatException]) {
      a.execute(None, new Reporter {}, new Stopper {}, Set(), Set(), Map(), None)
    }
    assert(a.afterAllCalled)
  }
  
  specify("If super.execute returns normally, but afterAll completes abruptly with an " +
    "exception, execute will complete abruptly with the same exception.") {
       
    class MySuite extends ImpSuite {
      override def afterAll() { throw new NumberFormatException }
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

  override def beforeEach() {
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

  override def beforeEach() {
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


