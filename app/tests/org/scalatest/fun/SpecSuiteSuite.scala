package org.scalatest.fun

class SpecSuiteSuite extends FunSuite {
  specify("an example must get invoked by execute") {
    class MySuite extends SpecSuite {
      var exampleWasInvoked = false
      it should "get invoked" in {
        exampleWasInvoked = true
      }
    }
    val a = new MySuite
    a.execute()
    assert(a.exampleWasInvoked)
  }
  
  specify("two examples must get invoked by execute") {
    class MySuite extends SpecSuite {
      var exampleWasInvoked = false
      var example2WasInvoked = false
      it should "get invoked" in {
        exampleWasInvoked = true
      }
      it should "also get invoked" in {
        example2WasInvoked = true
      }
    }
    val a = new MySuite
    a.execute()
    assert(a.exampleWasInvoked)
    assert(a.example2WasInvoked)
  }

  specify("three examples must get invoked by execute") {
    class MySuite extends SpecSuite {
      var exampleWasInvoked = false
      var example2WasInvoked = false
      var example3WasInvoked = false
      it should "get invoked" in {
        exampleWasInvoked = true
      }
      it should "also get invoked" in {
        example2WasInvoked = true
      }
      it should "also also get invoked" in {
        example3WasInvoked = true
      }
    }
    val a = new MySuite
    a.execute()
    assert(a.exampleWasInvoked)
    assert(a.example2WasInvoked)
    assert(a.example3WasInvoked)
  }
}

class TryingASpecSuite extends SpecSuite {

  share("a non-empty stack") {
    it should "return the top when sent #peek" in {
      println("and how")
    }
  }

  describe("Stack") {

    before each {
      println("do the setup thing")
    }

    it should "work right the first time" in {
      println("and how")
    }

    it should behave like "a non-empty stack"
  }
}
