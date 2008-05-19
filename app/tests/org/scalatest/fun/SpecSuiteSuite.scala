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
  
  specify("expectedTestCount is the number of examples if no shares") {
    class MySuite extends SpecSuite {
      it should "one" in {}
      it should "two" in {}
      describe("behavior") {
        it should "three" in {}  
        it should "four" in {}
      }
      it should "five" in {}
    }
    val a = new MySuite
    assert(a.expectedTestCount(Set(), Set()) === 5)
  }
  
  specify("expectedTestCount should not include tests in shares if never called") {
    class MySuite extends SpecSuite {
      share("this") {
        it should "six" in {}
        it should "seven" in {}
      }
      it should "one" in {}
      it should "two" in {}
      describe("behavior") {
        it should "three" in {}  
        it should "four" in {}
      }
      it should "five" in {}
    }
    val a = new MySuite
    assert(a.expectedTestCount(Set(), Set()) === 5)
  }

  specify("expectedTestCount should  include tests in a share that is called") {
    class MySuite extends SpecSuite {
      share("this") {
        it should "six" in {}
        it should "seven" in {}
      }
      it should "one" in {}
      it should "two" in {}
      describe("behavior") {
        it should "three" in {} 
        it should behave like "this"
        it should "four" in {}
      }
      it should "five" in {}
    }
    val a = new MySuite
    assert(a.expectedTestCount(Set(), Set()) === 7)
  }

  specify("expectedTestCount should include tests in a share that is called twice") {
    class MySuite extends SpecSuite {
      share("this") {
        it should "six" in {}
        it should "seven" in {}
      }
      it should "one" in {}
      it should "two" in {}
      describe("behavior") {
        it should "three" in {} 
        it should behave like "this"
        it should "four" in {}
      }
      it should "five" in {}
      it should behave like "this"
    }
    val a = new MySuite
    assert(a.expectedTestCount(Set(), Set()) === 9)
  }

  specify("expectedTestCount should work when shares are nested") {
    class MySuite extends SpecSuite {
      share("this") {
        it should "six" in {}
        it should "seven" in {}
        share("that") {
          it should "eight" in {}
          it should "nine" in {}
          it should "ten" in {}
        }
        it should behave like "that"
      }
      it should "one" in {}
      it should "two" in {}
      describe("behavior") {
        it should "three" in {} 
        it should behave like "this"
        it should "four" in {}
      }
      it should "five" in {}
      it should behave like "this"
    }
    val a = new MySuite
    assert(a.expectedTestCount(Set(), Set()) === 15)
  }
  
  specify("Before each, after each, before all, and after all should all nest nicely") {
    class MySuite extends SpecSuite {
      before all {}
      share("this") {
        before each {}
        it should "six" in {}
        after each {}
        it should "seven" in {}
        share("that") {
          it should "eight" in {}
          it should "nine" in {}
          it should "ten" in {}
          after each {}
          before each {}
        }
        it should behave like "that"
      }
      it should "one" in {}
      before each{}
      it should "two" in {}
      describe("behavior") {
        before each {}
        it should "three" in {} 
        it should behave like "this"
        it should "four" in {}
      }
      it should "five" in {}
      it should behave like "this"
      after each {}
      after all {}
    }
    new MySuite
  }
  
  specify("a before each should run before an example") {
    class MySuite extends SpecSuite {
      var exampleRan = false
      var beforeEachRanBeforeExample = false
      before each {
        if (!exampleRan)
          beforeEachRanBeforeExample = true
      }
      it should "run after example" in {
        exampleRan = true
      }
    }
    val a = new MySuite
    a.execute()
    assert(a.beforeEachRanBeforeExample)
  }
  
  specify("an 'after each' should run after an example") {
    class MySuite extends SpecSuite {
      var exampleRan = false
      var afterEachRanAfterExample = false
      after each {
        if (exampleRan)
          afterEachRanAfterExample = true
      }
      it should "run after example" in {
        exampleRan = true
      }
    }
    val a = new MySuite
    a.execute()
    assert(a.afterEachRanAfterExample)
  }

  specify("If a test function throws an exception, after each should get invoked anyway") {
    class MySuite extends SpecSuite {
      var afterEachRanAfterExample = false
      after each {
          afterEachRanAfterExample = true
      }
      it should "run after example" in {
        throw new RuntimeException
      }
    }
    val a = new MySuite
    a.execute()
    assert(a.afterEachRanAfterExample)
  }
}