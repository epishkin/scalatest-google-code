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
package org.scalatest.fixture

import collection.immutable.TreeSet
import java.lang.reflect.{InvocationTargetException, Method, Modifier}
import org.scalatest.Suite.checkForPublicNoArgConstructor
import org.scalatest.Suite.TestMethodPrefix
import org.scalatest.Suite.IgnoreAnnotation
import FixtureSuite.FixtureAndInformerInParens
import FixtureSuite.FixtureInParens
import FixtureSuite.testMethodTakesInformer
import FixtureSuite.simpleNameForTest
import FixtureSuite.argsArrayForTestName
import org.scalatest.events._

/**
 * <code>Suite</code> that passes a fixture object into each test.
 *
 * <p>
 * This trait behaves similarly to trait <code>org.scalatest.Suite</code>, except that each test takes a fixture object. The type of the
 * fixture object passed is defined by the abstract <code>Fixture</code> type, which is declared as a member of this trait.
 * This trait also declares the abstract method <code>withFixture</code>. The <code>withFixture</code> method
 * takes a <code>TestFunction</code>, which is a nested trait defined as a member of this trait.
 * <code>TestFunction</code> has an <code>apply</code> method that takes a <code>Fixture</code>.
 * This <code>apply</code> method is responsible for running a test.
 * This trait's <code>runTest</code> method delegates the actual running of each test to <code>withFixture</code>, passing
 * in the test code to run via the <code>TestFunction</code> argument. The <code>withFixture</code> method (abstract in this trait) is responsible
 * for creating the fixture and passing it to the test function.
 * </p>
 * 
 * <p>
 * Subclasses of this trait must, therefore, do three things differently from a plain old <code>org.scalatest.Suite</code>:
 * </p>
 * 
 * <ol>
 * <li>define the type of the fixture object by specifying type <code>Fixture</code></li>
 * <li>define the <code>withFixture</code> method</li>
 * <li>write test methods that take a <code>Fixture</code></li>
 * </ol>
 *
 * <p>
 * Here's an example:
 * </p>
 *
 * <pre>
 * import org.scalatest.fixture.FixtureSuite
 * import java.io.FileReader
 * import java.io.FileWriter
 * import java.io.File
 * 
 * class MySuite extends FixtureSuite {
 *
 *   // 1. define type Fixture
 *   type Fixture = FileReader
 *
 *   // 2. define the withFixture method
 *   def withFixture(testFunction: TestFunction) {
 *
 *     val FileName = "TempFile.txt"
 *
 *     // Set up the temp file needed by the test
 *     val writer = new FileWriter(FileName)
 *     try {
 *       writer.write("Hello, test!")
 *     }
 *     finally {
 *       writer.close()
 *     }
 *
 *     // Create the reader needed by the test
 *     val reader = new FileReader(FileName)
 *  
 *     try {
 *       // Run the test using the temp file
 *       testFunction(reader)
 *     }
 *     finally {
 *       // Close and delete the temp file
 *       reader.close()
 *       val file = new File(FileName)
 *       file.delete()
 *     }
 *   }
 * 
 *   // 3. write test methods that take a Fixture
 *   def testReadingFromTheTempFile(reader: FileReader) {
 *     var builder = new StringBuilder
 *     var c = reader.read()
 *     while (c != -1) {
 *       builder.append(c.toChar)
 *       c = reader.read()
 *     }
 *     assert(builder.toString === "Hello, test!")
 *   }
 * 
 *   def testFirstCharOfTheTempFile(reader: FileReader) {
 *     assert(reader.read() === 'H')
 *   }
 * }
 * </pre>
 *
 * <p>
 * If the fixture you want to pass into each test consists of multiple objects, you will need to combine
 * them into one object to use this trait. One good approach to passing multiple fixture objects is
 * to encapsulate them in a tuple. Here's an example that takes the tuple approach:
 * </p>
 *
 * <pre>
 * import org.scalatest.fixture.FixtureSuite
 * import scala.collection.mutable.ListBuffer
 *
 * class MySuite extends FixtureSuite {
 *
 *   type Fixture = (StringBuilder, ListBuffer[String])
 *
 *   def withFixture(testFunction: TestFunction) {
 *
 *     // Create needed mutable objects
 *     val stringBuilder = new StringBuilder("ScalaTest is ")
 *     val listBuffer = new ListBuffer[String]
 *
 *     // Invoke the test function, passing in the mutable objects
 *     testFunction(stringBuilder, listBuffer)
 *   }
 *
 *   def testEasy(fixture: Fixture) {
 *     val (builder, buffer) = fixture
 *     builder.append("easy!")
 *     assert(builder.toString === "ScalaTest is easy!")
 *     assert(buffer.isEmpty)
 *     buffer += "sweet"
 *   }
 *
 *   def testFun(fixture: Fixture) {
 *     val (builder, buffer) = fixture
 *     builder.append("fun!")
 *     assert(builder.toString === "ScalaTest is fun!")
 *     assert(buffer.isEmpty)
 *   }
 * }
 * </pre>
 *
 * <p>
 * When using a tuple to pass multiple fixture objects, it is usually helpful to give names to each
 * individual object in the tuple with a pattern-match assignment, as is done at the beginning
 * of each test method here with:
 * </p>
 *
 * <pre>
 * val (builder, buffer) = fixture
 * </pre>
 *
 * <p>
 * Another good approach to passing multiple fixture objects is
 * to encapsulate them in a case class. Here's an example that takes the case class approach:
 * </p>
 *
 * <pre>
 * import org.scalatest.fixture.FixtureSuite
 * import scala.collection.mutable.ListBuffer
 *
 * class MySuite extends FixtureSuite {
 *
 *   case class FixtureHolder(builder: StringBuilder, buffer: ListBuffer[String])
 *
 *   type Fixture = FixtureHolder
 *
 *   def withFixture(testFunction: TestFunction) {
 *
 *     // Create needed mutable objects
 *     val stringBuilder = new StringBuilder("ScalaTest is ")
 *     val listBuffer = new ListBuffer[String]
 *
 *     // Invoke the test function, passing in the mutable objects
 *     testFunction(FixtureHolder(stringBuilder, listBuffer))
 *   }
 *
 *   def testEasy(fixture: Fixture) {
 *     import fixture._
 *     builder.append("easy!")
 *     assert(builder.toString === "ScalaTest is easy!")
 *     assert(buffer.isEmpty)
 *     buffer += "sweet"
 *   }
 *
 *   def testFun(fixture: Fixture) {
 *     fixture.builder.append("fun!")
 *     assert(fixture.builder.toString === "ScalaTest is fun!")
 *     assert(fixture.buffer.isEmpty)
 *   }
 * }
 * </pre>
 *
 * <p>
 * When using a case class to pass multiple fixture objects, it can be helpful to make the names of each
 * individual object available as a single identifier with an import statement. This is the approach
 * taken by the <code>testEasy</code> method in the previous example. Because it imports the members
 * of the fixture object, the test method code can just use them as unqualified identifiers:
 * </p>
 *
 * <pre>
 * def testEasy(fixture: Fixture) {
 *   import fixture._
 *   builder.append("easy!")
 *   assert(builder.toString === "ScalaTest is easy!")
 *   assert(buffer.isEmpty)
 *   buffer += "sweet"
 * }
 * </pre>
 *
 * <p>
 * Alternatively, you may sometimes prefer to qualify each use of a fixture object with the name
 * of the fixture parameter. This approach, taken by the <code>testFun</code> method in the previous
 * example, makes it more obvious which variables in your test method
 * are part of the passed-in fixture:
 * </p>
 *
 * <pre>
 * def testFun(fixture: Fixture) {
 *   fixture.builder.append("fun!")
 *   assert(fixture.builder.toString === "ScalaTest is fun!")
 *   assert(fixture.buffer.isEmpty)
 * }
 * </pre>
 *
 * <p>
 * <strong>Configuring fixtures and tests</strong>
 * </p>
 * 
 * <p>
 * Sometimes you may want to write tests that are configurable. For example, you may want to write
 * a suite of tests that each take an open temp file as a fixture, but whose file name is specified
 * externally so that the file name can be can be changed from run to run. To accomplish this
 * the <code>TestFunction</code> trait has a <code>configMap</code>
 * method, which will return a <code>Map[String, Any]</code> from which configuration information may be obtained.
 * The <code>runTest</code> method of this trait will pass a <code>TestFunction</code> to <code>withFixture</code>
 * whose <code>configMap</code> method returns the <code>configMap</code> passed to <code>runTest</code>.
 * Here's an example in which the name of a temp file is taken from the passed <code>configMap</code>:
 * </p>
 *
 * <pre>
 * import org.scalatest.fixture.FixtureSuite
 * import java.io.FileReader
 * import java.io.FileWriter
 * import java.io.File
 * 
 * class MySuite extends FixtureSuite {
 *
 *   type Fixture = FileReader
 *
 *   def withFixture(testFunction: TestFunction) {
 *
 *     require(
 *       testFunction.configMap.contains("TempFileName"),
 *       "This suite requires a TempFileName to be passed in the configMap"
 *     )
 *
 *     // Grab the file name from the configMap
 *     val FileName = testFunction.configMap("TempFileName")
 *
 *     // Set up the temp file needed by the test
 *     val writer = new FileWriter(FileName)
 *     try {
 *       writer.write("Hello, test!")
 *     }
 *     finally {
 *       writer.close()
 *     }
 *
 *     // Create the reader needed by the test
 *     val reader = new FileReader(FileName)
 *  
 *     try {
 *       // Run the test using the temp file
 *       testFunction(reader)
 *     }
 *     finally {
 *       // Close and delete the temp file
 *       reader.close()
 *       val file = new File(FileName)
 *       file.delete()
 *     }
 *   }
 * 
 *   def testReadingFromTheTempFile(reader: FileReader) {
 *     var builder = new StringBuilder
 *     var c = reader.read()
 *     while (c != -1) {
 *       builder.append(c.toChar)
 *       c = reader.read()
 *     }
 *     assert(builder.toString === "Hello, test!")
 *   }
 * 
 *   def testFirstCharOfTheTempFile(reader: FileReader) {
 *     assert(reader.read() === 'H')
 *   }
 * }
 * </pre>
 *
 * <p>
 * If you want to pass into each test the entire <code>configMap</code> that was passed to <code>runTest</code>, you 
 * can mix in trait <code>ConfigMapFixture</code>. See the <a href="ConfigMapFixture.html">documentation
 * for <code>ConfigMapFixture</code></a> for the details, but here's a quick
 * example of how it looks:
 * </p>
 *
 * <pre>
 *  import org.scalatest.fixture.FixtureSuite
 *  import org.scalatest.fixture.ConfigMapFixture
 *
 *  class MySuite extends FixtureSuite with ConfigMapFixture {
 *
 *    def testHello(configMap: Map[String, Any]) {
 *      // Use the configMap passed to runTest in the test
 *      assert(configMap.contains("hello")
 *    }
 *
 *    def testWorld(configMap: Map[String, Any]) {
 *      assert(configMap.contains("world")
 *    }
 *  }
 * </pre>
 *
 * @author Bill Venners
 */
trait FixtureSuite extends org.scalatest.Suite { thisSuite =>

  /**
   * The type of the fixture needed by the current suite.
   */
  protected type Fixture

  /**
   * Object that encapsulates a test function and config map.
   *
   * <p>
   * The <code>FixtureSuite</code> trait's implementation of <code>runTest</code> passes instances of this trait
   * to <code>FixtureSuite</code>'s <code>withFixture</code> method.  For more detail and examples, see the
   * <a href="FixtureSuite.html">documentation for trait <code>FixtureSuite</code></a>.
   * </p>
   */
  protected trait TestFunction extends ((Fixture) => Unit) {

    /**
     * Run the test, using the passed <code>Fixture</code>.
     */
    def apply(fixture: Fixture)

    /**
     * Return a <code>Map[String, Any]</code> containing objects that can be used
     * to configure the fixture and test.
     */
    def configMap: Map[String, Any]
  }

  /**
   * Run the passed test function with a fixture created by this method.
   *
   * <p>
   * This method should create the fixture object needed by the tests of the
   * current suite, invoke the test function (passing in the fixture object),
   * and if needed, perform any clean up needed after the test completes.
   * For more detail and examples, see the <a href="FixtureSuite.html">main documentation for this trait</a>.
   * </p>
   *
   * @param testFunction the <code>TestFunction</code> to invoke, passing in a fixture
   */
  protected def withFixture(testFunction: TestFunction)

  private[fixture] class TestFunAndConfigMap(fun: (Fixture) => Unit, val configMap: Map[String, Any])
    extends TestFunction {
    
    def apply(fixture: Fixture) {
      fun(fixture)
    }
  }

  // Need to override this one becaue it call getMethodForTestName
  override def tags: Map[String, Set[String]] = {

    def getTags(testName: String) =
/* AFTER THE DEPRECATION CYCLE FOR GROUPS TO TAGS (0.9.8), REPLACE THE FOLLOWING FOR LOOP WITH THIS COMMENTED OUT ONE
   THAT MAKES SURE ANNOTATIONS ARE TAGGED WITH TagAnnotation.
      for {
        a <- getMethodForTestName(testName).getDeclaredAnnotations
        annotationClass = a.annotationType
        if annotationClass.isAnnotationPresent(classOf[TagAnnotation])
      } yield annotationClass.getName
*/
      for (a <- getMethodForTestName(testName).getDeclaredAnnotations)
        yield a.annotationType.getName

    val elements =
      for (testName <- testNames; if !getTags(testName).isEmpty)
        yield testName -> (Set() ++ getTags(testName))

    Map() ++ elements
  }

  override def testNames: Set[String] = {

    def takesTwoParamsOfTypesObjectAndInformer(m: Method) = {
      val paramTypes = m.getParameterTypes
      val hasTwoParams = paramTypes.length == 2
      hasTwoParams &&
          classOf[Object].isAssignableFrom(paramTypes(0)) &&
          classOf[Informer].isAssignableFrom(paramTypes(1))
    }

    def takesOneParamOfTypeObject(m: Method) = {
      val paramTypes = m.getParameterTypes
      val hasOneParam = paramTypes.length == 1
      hasOneParam && classOf[Object].isAssignableFrom(paramTypes(0))
    }

    def isTestMethod(m: Method) = {

      val isInstanceMethod = !Modifier.isStatic(m.getModifiers())

      // name must have at least 4 chars (minimum is "test")
      val simpleName = m.getName
      val firstFour = if (simpleName.length >= 4) simpleName.substring(0, 4) else "" 

      // Don't need to check for testNames in this case, because will discover both
      // testNames(Object) and testNames(Object, Informer). Reason is if I didn't discover these
      // it would likely just be silently ignored, and that might waste users' time
      isInstanceMethod && (firstFour == "test") && (takesOneParamOfTypeObject(m) ||
              takesTwoParamsOfTypesObjectAndInformer(m))
    }

    val testNameArray =
      for (m <- getClass.getMethods; if isTestMethod(m)) yield
        if (takesOneParamOfTypeObject(m))
          m.getName + FixtureInParens
        else
          m.getName + FixtureAndInformerInParens

    TreeSet[String]() ++ testNameArray
  }

  protected override def runTest(testName: String, reporter: Reporter, stopper: Stopper, configMap: Map[String, Any], tracker: Tracker) {

    if (testName == null || reporter == null || stopper == null || configMap == null || tracker == null)
      throw new NullPointerException

    val stopRequested = stopper
    val report = wrapReporterIfNecessary(reporter)
    val method = getMethodForTestName(testName)

    // Create a Rerunner if the Suite has a no-arg constructor
    val hasPublicNoArgConstructor = checkForPublicNoArgConstructor(getClass)

    val rerunnable =
      if (hasPublicNoArgConstructor)
        Some(new TestRerunner(getClass.getName, testName))
      else
        None

    val testStartTime = System.currentTimeMillis

    report(TestStarting(tracker.nextOrdinal(), thisSuite.suiteName, Some(thisSuite.getClass.getName), testName, None, rerunnable))


    try {
      val testFun: Fixture => Unit = {
        (fixture: Fixture) => {
          val anyRefFixture: AnyRef = fixture.asInstanceOf[AnyRef]
          val args: Array[Object] =
            if (testMethodTakesInformer(testName)) {
              val informer =
                new Informer {
                  def apply(message: String) {
                    if (message == null)
                      throw new NullPointerException
                    report(InfoProvided(tracker.nextOrdinal(), message, Some(NameInfo(thisSuite.suiteName, Some(thisSuite.getClass.getName), Some(testName)))))
                  }
                }
              Array(anyRefFixture, informer)
            }
            else Array(anyRefFixture)

          method.invoke(this, args: _*)
        }
      }
      withFixture(new TestFunAndConfigMap(testFun, configMap))

      val duration = System.currentTimeMillis - testStartTime
      report(TestSucceeded(tracker.nextOrdinal(), thisSuite.suiteName, Some(thisSuite.getClass.getName), testName, Some(duration), None, rerunnable))
    }
    catch { 
      case ite: InvocationTargetException =>
        val t = ite.getTargetException
        t match {
          case _: TestPendingException =>
            report(TestPending(tracker.nextOrdinal(), thisSuite.suiteName, Some(thisSuite.getClass.getName), testName))
          case _ =>
            val duration = System.currentTimeMillis - testStartTime
            handleFailedTest(t, hasPublicNoArgConstructor, testName, rerunnable, report, tracker, duration)
        }

      case e: Exception => {
        val duration = System.currentTimeMillis - testStartTime
        handleFailedTest(e, hasPublicNoArgConstructor, testName, rerunnable, report, tracker, duration)
      }
      case ae: AssertionError => {
        val duration = System.currentTimeMillis - testStartTime
        handleFailedTest(ae, hasPublicNoArgConstructor, testName, rerunnable, report, tracker, duration)
      }
    }
  }

  // TODO: This is identical with the one in Suite. Factor it out to an object somewhere.
  private def handleFailedTest(throwable: Throwable, hasPublicNoArgConstructor: Boolean, testName: String,
      rerunnable: Option[Rerunner], report: Reporter, tracker: Tracker, duration: Long) {

    val message =
      if (throwable.getMessage != null) // [bv: this could be factored out into a helper method]
        throwable.getMessage
      else
        throwable.toString

    report(TestFailed(tracker.nextOrdinal(), message, thisSuite.suiteName, Some(thisSuite.getClass.getName), testName, Some(throwable), Some(duration), None, rerunnable))
  }

  // TODO: This is also identical with the one in Suite, but it probably will change when I start looking
  // for an argument of type Fixture, which will probably have to be Object.
  private def getMethodForTestName(testName: String) = {
    val candidateMethods = getClass.getMethods.filter(_.getName == simpleNameForTest(testName))
    val found =
      if (testMethodTakesInformer(testName))
        candidateMethods.find(
          candidateMethod => {
            val paramTypes = candidateMethod.getParameterTypes
            paramTypes.length == 2 && paramTypes(1) == classOf[Informer]
          }
        )
      else
        candidateMethods.find(_.getParameterTypes.length == 1)
     found match {
       case Some(method) => method
       case None =>
         throw new IllegalArgumentException(Resources("testNotFound", testName))
     }
  }
}

private object FixtureSuite {

  val FixtureAndInformerInParens = "(Fixture, Informer)"
  val FixtureInParens = "(Fixture)"

  private def testMethodTakesInformer(testName: String) = testName.endsWith(FixtureAndInformerInParens)

  private def simpleNameForTest(testName: String) =
    if (testName.endsWith(FixtureAndInformerInParens))
      testName.substring(0, testName.length - FixtureAndInformerInParens.length)
    else if (testName.endsWith(FixtureInParens))
      testName.substring(0, testName.length - FixtureInParens.length)
    else
      throw new IllegalArgumentException(Resources("needFixtureInTestName", testName))

  private def argsArrayForTestName(testName: String): Array[Class[_]] =
    if (testMethodTakesInformer(testName))
      Array(classOf[Object], classOf[Informer])
    else
      Array(classOf[Informer])
}
