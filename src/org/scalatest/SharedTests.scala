/*
 * Copyright 2001-2008 Artima, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.scalatest

/**
 * Trait that enables the same tests to be run on different fixture objects. In other words, it enables tests to be "shared"
 * by different fixture objects.
 *
 * <p>
 * To use the <code>SharedTests</code> trait, you first place shared tests in <em>behavior functions</em>. These behavior functions will be
 * invoked during the construction phase of any suite that uses them, so that the tests they contain will be registered as tests in that suite.
 * The <code>SharedTests</code>, therefore, can only be used in suites in which tests are represented by function values registered
 * during suite object construction, such as ScalaTest's <code>FunSuite</code> and <code>Spec</code> classes. By contrast, trait
 * <code>SharedTests</code>  can't be mixed into suites such as ScalaTest's <code>Suite</code>, <code>JUnitSuite</code>, or
 * <code>TestNGSuite</code>, in which tests are represented by methods. Any attempt to mix <code>SharedTests</code> into any such
 * suite will not compile, because they don't conform to <code>SharedTest</code>'s self type, <code>TestRegistration</code>.
 * </p>
 *
 * <p>
 * For example, given this stack class:
 * </p>
 *
 * <pre>
 * import scala.collection.mutable.ListBuffer
 * 
 * class Stack[T] {
 *
 *   val MAX = 10
 *   private var buf = new ListBuffer[T]
 *
 *   def push(o: T) {
 *     if (!full)
 *       o +: buf
 *     else
 *       throw new IllegalStateException("can't push onto a full stack")
 *   }
 *
 *   def pop(): T = {
 *     if (!empty)
 *       buf.remove(0)
 *     else
 *       throw new IllegalStateException("can't pop an empty stack")
 *   }
 *
 *   def peek: T = {
 *     if (!empty)
 *       buf(0)
 *     else
 *       throw new IllegalStateException("can't pop an empty stack")
 *   }
 *
 *   def full: Boolean = buf.size == MAX
 *   def empty: Boolean = buf.size == 0
 *   def size = buf.size
 * }
 * </pre>
 *
 * <p>
 * You may want to test the <code>Stack</code> class in different states: empty, full, with one item, with one item less than capacity,
 * <em>etc</em>. You may find you have several tests that make sense any time the stack is non-empty. Thus you'd ideally want to run
 * those same tests for three stack fixture objects: a full stack, a stack with a one item, and a stack with one item less than
 * capacity. With <code>SharedTests</code>, you can factor these tests out into a behavior function, into which you pass the
 * stack fixture to use when running the tests. So in your test suite for stack, you'd invoke the
 * behavior function three times, passing in each of the three stack fixtures so that the shared tests are run for all three fixtures. You
 * can define a behavior function that encapsulates these shared tests inside the suite you use them. If they are shared
 * between different suites, however, you could also define them in a separate trait that is mixed into each suite that uses them.
 * </p>
 *
 * <p>
 * For example, here the <code>nonEmptyStack</code> behavior function (here a behavior <em>method</em>) is defined in a trait along with another
 * method containing shared tests for non-full stacks:
 * </p>
 * 
 * <pre>
 * trait StackBehaviors { this: Spec =>
 * 
 *   def nonEmptyStack(lastItemAdded: Int)(stack: Stack[Int]) {
 * 
 *     it("should be non-empty") {
 *       assert(!stack.empty)
 *     }  
 * 
 *     it("should return the top item on peek") {
 *       assert(stack.peek === lastItemAdded)
 *     }
 *   
 *     it("should not remove the top item on peek") {
 *       val size = stack.size
 *       assert(stack.peek === lastItemAdded)
 *       assert(stack.size === size)
 *     }
 *   
 *     it("should remove the top item on pop") {
 *       val size = stack.size
 *       assert(stack.pop === lastItemAdded)
 *       assert(stack.size === size - 1)
 *     }
 *   }
 *   
 *   def nonFullStack(stack: Stack[Int]) {
 *       
 *     it("should not be full") {
 *       assert(!stack.full)
 *     }
 *       
 *     it("should add to the top on push") {
 *       val size = stack.size
 *       stack.push(7)
 *       assert(stack.size === size + 1)
 *       assert(stack.peek === 7)
 *     }
 *   }
 * }
 * </pre>
 *
 *
 * <p>
 * In a behavior function, the fixture object must be passed in its own parameter list. If the shared tests need nothing more than
 * the fixture object, then the fixture object's parameter list is the only parameter list, as in the <code>nonFullStack</code>
 * method from the previous example:
 * </p>
 *
 * <pre>
 * def nonFullStack(stack: Stack[Int])
 * </pre>
 *
 * <p>
 * However, if the shared tests need other information in addition to the fixture object, that information must be
 * passed in a separate parameter list. The behavior function must in that case be curried, with the parameter list
 * for the fixture object coming last, as in the <code>nonEmptyStack</code> method from the previous example:
 * </p>
 *
 * <pre>
 * def nonEmptyStack(lastItemAdded: Int)(stack: Stack[Int])
 * </pre>
 *
 * <p>
 * Given these behavior functions, you could invoke them directly, but <code>SharedTests</code> offers a DSL for the purpose,
 * which looks like this:
 * </p>
 *
 * <pre>
 * ensure (stackWithOneItem) behaves like (nonEmptyStack(lastValuePushed))
 * ensure (stackWithOneItem) behaves like (nonFullStack)
 * </pre>
 *
 * <p>
 * If you don't like the parentheses, you can alternatively invoke them like this:
 * <p>
 *
 * <pre>
 * ensure that stackWithOneItem behaves like a nonEmptyStack(lastValuePushed)
 * ensure that stackWithOneItem behaves like a nonFullStack
 * </pre>
 *
 * <p>
 * If you prefer to use an imperative style to change fixtures, for example by mixing in <code>BeforeAndAfter</code> and
 * reassigning a <code>stack</code> <code>var</code> in <code>beforeEach</code>, you could write your behavior functions
 * in the context of that <code>var</code>, which means you wouldn't need to pass in the stack fixture because it would be
 * in scope already inside the behavior function. In that case, you can
 * use <code>it</code> in place of the fixture object, like this:
 * </p>
 *
 * <pre>
 * ensure it behaves like nonEmptyStack // assuming lastValuePushed is also in scope inside nonEmptyStack
 * ensure it behaves like nonFullStack
 * </pre>
 *
 * <p>
 * The recommended style, however, is the functional, pass-all-the-needed-values-in style. Here's an example:
 * </p>
 *
 * <pre>
 * class SharedTestExampleSpec extends Spec with SharedTests with StackBehaviors {
 * 
 *   // Stack fixture creation methods
 *   def emptyStack = new Stack[Int]
 * 
 *   def fullStack = {
 *     val stack = new Stack[Int]
 *     for (i <- 0 until stack.MAX)
 *       stack.push(i)
 *     stack
 *   }
 * 
 *   def stackWithOneItem = {
 *     val stack = new Stack[Int]
 *     stack.push(9)
 *     stack
 *   }
 * 
 *   def stackWithOneItemLessThanCapacity = {
 *     val stack = new Stack[Int]
 *     for (i <- 1 to 9)
 *       stack.push(i)
 *     stack
 *   }
 * 
 *   val lastValuePushed = 9
 * 
 *   describe("A Stack") {
 * 
 *     describe("(when empty)") {
 *       
 *       it("should be empty") {
 *         assert(emptyStack.empty)
 *       }
 * 
 *       it("should complain on peek") {
 *         intercept[IllegalStateException] {
 *           emptyStack.peek
 *         }
 *       }
 * 
 *       it("should complain on pop") {
 *         intercept[IllegalStateException] {
 *           emptyStack.pop
 *         }
 *       }
 *     }
 * 
 *     describe("(with one item)") {
 *       ensure (stackWithOneItem) behaves like (nonEmptyStack(lastValuePushed))
 *       ensure (stackWithOneItem) behaves like (nonFullStack)
 *     }
 *     
 *     describe("(with one item less than capacity)") {
 *       ensure (stackWithOneItemLessThanCapacity) behaves like (nonEmptyStack(lastValuePushed))
 *       ensure (stackWithOneItemLessThanCapacity) behaves like (nonFullStack)
 *     }
 * 
 *     describe("(full)") {
 *       
 *       it("should be full") {
 *         assert(fullStack.full)
 *       }
 * 
 *       nonEmptyStack(lastValuePushed)(fullStack)
 * 
 *       it("should complain on a push") {
 *         intercept[IllegalStateException] {
 *           fullStack.push(10)
 *         }
 *       }
 *     }
 *   }
 * }
 * </pre>
 *
 * <p>
 * If you load these classes into the Scala interpreter (with scalatest's JAR file on the class path), and execute it,
 * you'll see:
 * </p>
 *
 * <pre>
 * scala> (new StackSpec).execute()
 * A Stack (when empty) 
 * - should be empty
 * - should complain on peek
 * - should complain on pop
 * A Stack (with one item) 
 * - should be non-empty
 * - should return the top item on peek
 * - should not remove the top item on peek
 * - should remove the top item on pop
 * - should not be full
 * - should add to the top on push
 * A Stack (with one item less than capacity) 
 * - should be non-empty
 * - should return the top item on peek
 * - should not remove the top item on peek
 * - should remove the top item on pop
 * - should not be full
 * - should add to the top on push
 * A Stack (full) 
 * - should be full
 * - should be non-empty
 * - should return the top item on peek
 * - should not remove the top item on peek
 * - should remove the top item on pop
 * - should complain on a push
 * </pre>
 * 
 * <p>
 * <strong>Obtaining unique test names</strong>
 * </p>
 *
 * <p>
 * One thing to keep in mind when using shared tests is that in ScalaTest, each test in a suite must have a unique name.
 * If you register the same tests repeatedly in the same suite, one problem you may encounter is an exception at runtime
 * complaining that multiple tests are being registered with the same test name. The way to solve this problem is to surround
 * each invocation of a behavior function with a clause that will prepend a string to each test name. In <code>Spec</code>, for
 * example, this is done with <code>describe</code> clauses. For example, the following code in a <code>Spec</code> would register
 * a test with the name <code>"A Stack (when empty) should be empty"</code>:
 * </p>
 *
 * <pre>
 *   describe("A Stack") {
 * 
 *     describe("(when empty)") {
 *       
 *       it("should be empty") {
 *         assert(emptyStack.empty)
 *       }
 *       // ...
 * </pre>
 *
 * <p>
 * If the <code>"should be empty"</code> tests were factored out into a behavior function, it could be called repeatedly so long
 * as each invocation of the behavior function is inside a different set of <code>describe</code> clauses. You can achieve the same effect in <code>FunSuite</code>
 * by nesting invocations of the behavior function inside <code>testsFor</code> clauses, in <code>FeatureSpec</code> inside
 * <code>scenariosFor</code> clauses, <em>etc</em>.
 * </p>
 */
trait SharedTests { this: TestRegistration =>

  class ResultOfLikeApplication[T](val function: (T) => Unit)

  def like[T](function: (T) => Unit): ResultOfLikeApplication[T] = new ResultOfLikeApplication(function)

  class ResultOfEnsureWordApplication[T](left: T) {
    def behaves(resultOfLikeApplication: ResultOfLikeApplication[T]) {
      resultOfLikeApplication.function(left)
    }
  }

  class ResultOfEnsureItBehaves {
    def like(function: () => Unit) {
      function()
    }
  }

  class BehavesWord

  class EnsureWord {

    // ensure (stackWithOneItem) behaves like (nonEmptyStack(lastValuePushed))
    def apply[T](left: T): ResultOfEnsureWordApplication[T] = new ResultOfEnsureWordApplication(left)

    // ensure it behaves like (nonEmptyStack(lastValuePushed))
    def it(behaves: BehavesWord): ResultOfEnsureItBehaves = new ResultOfEnsureItBehaves
  }

  val ensure = new EnsureWord
  val behaves = new BehavesWord
}

/*
The way to do this in FunSuite is to use toString on the thing:

using fixture "A stack with one item" {
  ensure (stackWithOneItem) behaves like (nonFullStack)
}

testsFor("A stack with one item") {
  ensure (stackWithOneItem) behaves like (nonFullStack)
}

testsFor("A stack with one item") {
  ensure that stackWithOneItem behaves like a nonFullStack
}

Man, I guess I need to add testsFor to FunSuite. Bummer.

Ack, what about scenarios?

scenariosFor("something something") {
}

Dang, they are all the same: the same data structure, but different syntax.
*/

