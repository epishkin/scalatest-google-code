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

  specify("two examples should be invoked in order") {
    class MySuite extends SpecSuite {
      var example1WasInvoked = false
      var example2WasInvokedAfterExample1 = false
      it should "get invoked" in {
        example1WasInvoked = true
      }
      it should "also get invoked" in {
        if (example1WasInvoked)
          example2WasInvokedAfterExample1 = true
      }
    }
    val a = new MySuite
    a.execute()
    assert(a.example1WasInvoked)
    assert(a.example2WasInvokedAfterExample1)
  }

  specify("three examples should be invoked in order") {
    class MySuite extends SpecSuite {
      var example1WasInvoked = false
      var example2WasInvokedAfterExample1 = false
      var example3WasInvokedAfterExample2 = false
      it should "get invoked" in {
        example1WasInvoked = true
      }
      it should "also get invoked" in {
        if (example1WasInvoked)
          example2WasInvokedAfterExample1 = true
      }
      it should "also also get invoked" in {
        if (example2WasInvokedAfterExample1)
          example3WasInvokedAfterExample2 = true
      }
    }
    val a = new MySuite
    a.execute()
    assert(a.example1WasInvoked)
    assert(a.example2WasInvokedAfterExample1)
    assert(a.example3WasInvokedAfterExample2)
  }

  specify("three examples should be invoked in order even when two are surrounded by a describe") {
    class MySuite extends SpecSuite {
      var example1WasInvoked = false
      var example2WasInvokedAfterExample1 = false
      var example3WasInvokedAfterExample2 = false
      it should "get invoked" in {
        example1WasInvoked = true
      }
      describe("Stack") {
        it should "also get invoked" in {
          if (example1WasInvoked)
            example2WasInvokedAfterExample1 = true
        }
        it should "also also get invoked" in {
          if (example2WasInvokedAfterExample1)
            example3WasInvokedAfterExample2 = true
        }
      }
    }
    val a = new MySuite
    a.execute()
    assert(a.example1WasInvoked)
    assert(a.example2WasInvokedAfterExample1)
    assert(a.example3WasInvokedAfterExample2)
  }

  specify("an example should show up in testNames") {
    class MySuite extends SpecSuite {
      var exampleWasInvoked = false
      it should "get invoked" in {
        exampleWasInvoked = true
      }
    }
    val a = new MySuite
    assert(a.testNames.size === 1)
    assert(a.testNames.contains("it should get invoked"))
  }
   
  specify("two examples should show up in testNames") {
    class MySuite extends SpecSuite {
      var example1WasInvoked = false
      var example2WasInvokedAfterExample1 = false
      it should "get invoked" in {
        example1WasInvoked = true
      }
      it should "also get invoked" in {
        if (example1WasInvoked)
          example2WasInvokedAfterExample1 = true
      }
    }
    val a = new MySuite
    a.execute()
    assert(a.testNames.size === 2)
    assert(a.testNames.contains("it should get invoked"))
    assert(a.testNames.contains("it should also get invoked"))
  }
   
  specify("two examples should show up in order of appearance in testNames") {
    class MySuite extends SpecSuite {
      var example1WasInvoked = false
      var example2WasInvokedAfterExample1 = false
      it should "get invoked" in {
        example1WasInvoked = true
      }
      it should "also get invoked" in {
        if (example1WasInvoked)
          example2WasInvokedAfterExample1 = true
      }
    }
    val a = new MySuite
    a.execute()
    assert(a.testNames.size === 2)
    assert(a.testNames.elements.toList(0) === "it should get invoked")
    assert(a.testNames.elements.toList(1) === "it should also get invoked")
  }
 /* 
  specify("test names should include an enclosing describe string, separated by a space") {
    class MySuite extends SpecSuite {
      describe("A Stack") {
        it should "allow me to pop" in {}
        it should "allow me to push" in {}
      }
      val a = new MySuite
    assert(a.testNames.size === 2)
    assert(a.testNames.elements.toList(0) === "A Stack should allow me to pop")
    assert(a.testNames.elements.toList(1) === "A Stack should allow me to push")
    }  
  }
*/
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
