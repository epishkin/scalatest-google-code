package org.scalatest

import NodeFamily._

/**
 * A trait that specifies and tests behavior that can be shared by different subjects.
 *
 * <p>
 * In general, you'll need to pass in any fixture objects needed by the examples. One
 * way to do this is to define a method that takes the needed fixture objects, and returns
 * an <code>Examples</code> that uses them. For instance:
 * </p>
 *
 * <pre>
 * def nonFullStack(stack: Stack[Int]) = new Examples {
 *
 *   it("should not be full") {
 *     assert(!stack.full)
 *   }
 *
 *   it("should add to the top on push") {
 *     val size = stack.size
 *     stack.push(7)
 *     assert(stack.size === size + 1)
 *     assert(stack.peek === 7)
 *   }
 * }
 * </pre>
 *
 * <p>
 * An <code>Examples</code> is only a container for examples, and does not inherit from <code>Suite</code>.
 * As a result, you can't mix in <code>BeforeAndAfter</code> into an <code>Examples</code>. To do something
 * before and/or after each example, you'll need to use one of the more functional approaches described
 * in the documentation for <code>Spec</code>, such as <code>createFixture</code> or <code>withFixture</code>
 * methods.
 * </p>
 *
 * <p>
 * <strong>Test groups</strong>
 * </p>
 *
 * <p>
 * An <code>Examples</code>' tests may be classified into named <em>groups</em>.
 * When executing a <code>Spec</code> that includes an <code>Examples</code>, groups of tests can
 * optionally be included and/or excluded. To place <code>Examples</code> tests into
 * groups, you pass objects that extend abstract class <code>org.scalatest.Group</code> to the methods
 * that register tests, <code>it</code> and <code>ignore</code>. Class <code>Group</code> takes one parameter,
 * a string name.  If you have
 * created Java annotation interfaces for use as group names in direct subclasses of <code>org.scalatest.Suite</code>,
 * then you will probably want to use group names on your <code>Examples</code>s that match. To do so, simply
 * pass the fully qualified names of the Java interfaces to the <code>Group</code> constructor. For example, if you've
 * defined Java annotation interfaces with fully qualified names, <code>com.mycompany.groups.SlowTest</code> and <code>com.mycompany.groups.DBTest</code>, then you could
 * create matching groups for <code>Spec</code>s like this:
 * </p>
 * <pre>
 * import org.scalatest.Group
 *
 * object SlowTest extends Group("com.mycompany.groups.SlowTest")
 * object DBTest extends Group("com.mycompany.groups.DBTest")
 * </pre>
 * <p>
 * Given these definitions, you could place <code>Examples</code> tests into groups like this:
 * </p>
 * <pre>
 * def nonFullStack(stack: Stack[Int]) = new Examples {
 *
 *   it("should not be full", SlowTest) {
 *     assert(!stack.full)
 *   }
 *
 *   it("should add to the top on push", SlowTest, DBTest) {
 *     val size = stack.size
 *     stack.push(7)
 *     assert(stack.size === size + 1)
 *     assert(stack.peek === 7)
 *   }
 * }
 * </pre>
 *
 * <p>
 * This code places both tests into the <code>com.mycompany.groups.SlowTest</code> group,
 * and test <code>"should add to the top on push"</code> into the <code>com.mycompany.groups.DBTest</code> group.
 * </p>
 *
 * <p>
 * <strong>Ignored tests</strong>
 * </p>
 *
 * <p>
 * To support the common use case of &#8220;temporarily&#8221; disabling a test, with the
 * good intention of resurrecting the test at a later time, <code>Examples</code> provides registration
 * methods that start with <code>ignore</code> instead of <code>it</code>. For example, to temporarily
 * disable the test with the name <code>"shoud not be full"</code>, just change &#8220;<code>it</code>&#8221; into &#8220;<code>ignore</code>,&#8221; like this:
 * </p>
 *
 * <pre>
 * def nonFullStack(stack: Stack[Int]) = new Examples {
 *
 *   ignore("should not be full") {
 *     assert(!stack.full)
 *   }
 *
 *   it("should add to the top on push") {
 *     val size = stack.size
 *     stack.push(7)
 *     assert(stack.size === size + 1)
 *     assert(stack.peek === 7)
 *   }
 * }
 * </pre>
 *
 * <p>
 * <strong>A longer example</strong>
 * </p>
 *
 * <p>
 * To get a better idea of how to use <code>Examples</code>, here's a more complete, well, example:
 * </p>
 *
 * <pre>
 * import org.scalatest.Examples
 * import org.scalatest.Spec
 * import scala.collection.mutable.ListBuffer
 *
 * class Stack[T] {
 *   val MAX = 10
 *   private var buf = new ListBuffer[T]
 *   def push(o: T) {
 *     if (!full)
 *       o +: buf
 *     else
 *       throw new IllegalStateException("can't push onto a full stack")
 *   }
 *   def pop(): T = {
 *     if (!empty)
 *       buf.remove(0)
 *     else
 *       throw new IllegalStateException("can't pop an empty stack")
 *   }
 *   def peek: T = {
 *     if (!empty)
 *       buf(0)
 *     else
 *       throw new IllegalStateException("can't pop an empty stack")
 *   }
 *   def full: Boolean = buf.size == MAX
 *   def empty: Boolean = buf.size == 0
 *   def size = buf.size
 * }
 *
 * trait SharedExamples {
 *
 *   def nonEmptyStack(lastItemAdded: Int)(stack: Stack[Int]) = new Examples {
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
 *   def nonFullStack(stack: Stack[Int]) = new Examples {
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
 *
 * class StackItSpec extends Spec with SharedExamples {
 *
 *   // Fixtures
 *   def emptyStack = new Stack[Int]
 *   def fullStack = {
 *     val stack = new Stack[Int]
 *     for (i <- 0 until stack.MAX)
 *       stack.push(i)
 *     stack
 *   }
 *   def stackWithOneItem = {
 *     val stack = new Stack[Int]
 *     stack.push(9)
 *     stack
 *   }
 *   def stackWithOneItemLessThanCapacity = {
 *     val stack = new Stack[Int]
 *     for (i <- 1 to 9)
 *       stack.push(i)
 *     stack
 *   }
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
 *       includeExamples(nonEmptyStack(lastValuePushed)(stackWithOneItem))
 *       includeExamples(nonFullStack(stackWithOneItem))
 *     }
 *
 *     describe("(with one item less than capacity)") {
 *       includeExamples(nonEmptyStack(lastValuePushed)(stackWithOneItemLessThanCapacity))
 *       includeExamples(nonFullStack(stackWithOneItemLessThanCapacity))
 *     }
 *
 *     describe("(full)") {
 *
 *       it("should be full") {
 *         assert(fullStack.full)
 *       }
 *
 *       includeExamples(nonEmptyStack(lastValuePushed)(fullStack))
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
 * Were you to run this in the Scala interpreter, here's what you'd see:
 * </p>
 *
 * <pre>
 * scala> (new StackItSpec).execute()
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
 * @author Bill Venners
 */
trait Examples extends Assertions {

  private val IgnoreGroupName = "org.scalatest.Ignore"

  // All shared examples, in reverse order of registration
  private var sharedExamplesList = List[SharedExample]()

  // This map contains specText keys mapped to group name sets, not testName keys, because
  // the testName isn't known until this Examples is included in a Spec.
  private var sharedGroupsMap: Map[String, Set[String]] = Map()

  private[scalatest] def examples(newParent: Branch): List[Example] = {
    
    def transform(sharedExample: SharedExample): Example = {
      val testName = getTestName(sharedExample.specText, newParent)
      Example(newParent, testName, sharedExample.specText, -1, sharedExample.f)
    }
    sharedExamplesList.map(transform)
  }

  private[scalatest] def groups(newParent: Branch): Map[String, Set[String]] = {

    def transform(sharedGroupElement: (String, Set[String])): (String, Set[String]) = {
      val (specText, groupsSet) = sharedGroupElement
      val testName = getTestName(specText, newParent)
      (testName, groupsSet)
    }
    Map() ++ sharedGroupsMap.map(transform)
  }

  /**
   * Register a test with the given spec text, optional groups, and test function value that takes no arguments.
   * An invocation of this method is called an &#8220;example.&#8221;
   *
   * This method will register the example for later importing into a <code>Spec</code>. The passed
   * spec text must not have been registered previously on this <code>Examples</code> instance.
   *
   * @param specText the specification text, which will be combined with the descText of any surrounding describers
   * to form the test name
   * @param testFun the test function
   * @throws IllegalArgumentException if an example with the same spec text has been registered previously
   * @throws NullPointerException if <code>specText</code>is <code>null</code>
   */
  def it(specText: String, testGroups: Group*)(testFun: => Unit) {
    if (specText == null)
      throw new NullPointerException("specText was null")
    if (sharedExamplesList.exists(_.specText == specText)) {
      val duplicateName = sharedExamplesList.find(_.specText == specText).getOrElse("")
      throw new IllegalArgumentException("Duplicate spec text: " + duplicateName)
    }
    sharedExamplesList ::= SharedExample(specText, testFun _)
    val groupNames = Set[String]() ++ testGroups.map(_.name)
    if (!groupNames.isEmpty)
      sharedGroupsMap += (specText -> groupNames)
  }

  /**
   * Register a test to ignore, which has the given spec text, optional groups, and test function value that takes no arguments.
   * This method will register the test for later ignoring via an invocation of one of the <code>execute</code>
   * methods. This method exists to make it easy to ignore an existing test method by changing the call to <code>it</code>
   * to <code>ignore</code> without deleting or commenting out the actual test code. The test will not be executed, but a
   * report will be sent that indicates the test was ignored. The name of the test will be a concatenation of the text of all surrounding describers,
   * from outside in, and the passed spec text, with one space placed between each item. (See the documenation
   * for <code>testNames</code> for an example.) The resulting test name must not have been registered previously on
   * this <code>Spec</code> instance.
   *
   * @param specText the specification text, which will be combined with the descText of any surrounding describers
   * to form the test name
   * @param testGroups the optional list of groups to which this test belongs
   * @param testFun the test function
   * @throws IllegalArgumentException if a test with the same name has been registered previously
   * @throws NullPointerException if <code>specText</code> or any passed test group is <code>null</code>
   */
  def ignore(specText: String, testGroups: Group*)(testFun: => Unit) {
    if (specText == null)
      throw new NullPointerException("specText was null")
    if (sharedExamplesList.exists(_.specText == specText)) {
      val duplicateName = sharedExamplesList.find(_.specText == specText).getOrElse("")
      throw new IllegalArgumentException("Duplicate spec text: " + duplicateName)
    }
    sharedExamplesList ::= SharedExample(specText, testFun _)
    val groupNames = Set[String]() ++ testGroups.map(_.name)
    sharedGroupsMap += (specText -> (groupNames + IgnoreGroupName))
  }
}
