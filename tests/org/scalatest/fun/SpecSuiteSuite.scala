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
 
  specify("test names should include an enclosing describe string, separated by a space") {
    class MySuite extends SpecSuite {
      describe("A Stack") {
        it should "allow me to pop" in {}
        it should "allow me to push" in {}
      }
    }
    val a = new MySuite
    assert(a.testNames.size === 2)
    assert(a.testNames.elements.toList(0) === "A Stack should allow me to pop")
    assert(a.testNames.elements.toList(1) === "A Stack should allow me to push")
  }

  specify("test names should properly nest descriptions in test names") {
    class MySuite extends SpecSuite {
      describe("A Stack") {
        describe("(when not empty)") {
          it should "allow me to pop" in {}
        }
        describe("(when not full)") {
          it should "allow me to push" in {}
        }
      }
    }
    val a = new MySuite
    assert(a.testNames.size === 2)
    assert(a.testNames.elements.toList(0) === "A Stack (when not empty) should allow me to pop")
    assert(a.testNames.elements.toList(1) === "A Stack (when not full) should allow me to push")
  }
  
  specify("should be able to mix in ImpSuite without any problems") {
    class MySuite extends SpecSuite with ImpSuite {
      describe("A Stack") {
        before each {
          // set up fixture
        }
        describe("(when not empty)") {
          it should "allow me to pop" in {}
        }
        describe("(when not full)") {
          it should "allow me to push" in {}
        }
      }
    }
    val a = new MySuite
    a.execute()
  }
  
  specify("a shared example invoked with 'it should behave like' should get invoked") {
    class MySuite extends SpecSuite with ImpSuite {
      var sharedExampleInvoked = false
      share("shared example") {
        it should "be invoked" in {
          sharedExampleInvoked = true
        }
      }
      describe("A Stack") {
        before each {
          // set up fixture
        }
        describe("(when not empty)") {
          it should "allow me to pop" in {}
          it should behave like "shared example"
        }
        describe("(when not full)") {
          it should "allow me to push" in {}
        }
      }
    }
    val a = new MySuite
    a.execute()
    assert(a.sharedExampleInvoked)
  }
  
  specify("should throw an exception if they attempt to invoke a non-existent shared behavior") {
    class MySuite extends SpecSuite {
      it should behave like "well-mannered children"
    }
    intercept(classOf[NoSuchElementException]) {
      new MySuite
    }
  }
  
  
  specify("should throw an exception if they attempt to invoke a shared behavior with a typo") {
    class MySuite extends SpecSuite {
      share("will-mannered children") {}
      it should behave like "well-mannered children"
    }
    intercept(classOf[NoSuchElementException]) {
      new MySuite
    }
  }

  specify("should throw an exception if they attempt to invoke a shared behavior that's defined later") {
    class MySuite1 extends SpecSuite {
      share("nice people") {}
      it should behave like "nice people" // this should work
    }
    class MySuite2 extends SpecSuite {
      it should behave like "well-mannered children" // this should throw an exception
      share("well-mannered children") {}
    }
    new MySuite1
    intercept(classOf[NoSuchElementException]) {
      new MySuite2
    }
  }
  
  specify("Should find and invoke shared behavior that's inside a describe and invoked inside a nested describe") {
    class MySuite extends SpecSuite with ImpSuite {
      var sharedExampleInvoked = false
      describe("A Stack") {
        share("shared example") {
          it should "be invoked" in {
            sharedExampleInvoked = true
          }
        }
        before each {
          // set up fixture
        }
        describe("(when not empty)") {
          it should "allow me to pop" in {}
          it should behave like "shared example"
        }
        describe("(when not full)") {
          it should "allow me to push" in {}
        }
      }
    }
    val a = new MySuite
    a.execute()
    assert(a.sharedExampleInvoked)
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
