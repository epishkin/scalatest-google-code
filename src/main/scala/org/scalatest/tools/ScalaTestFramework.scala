package org.scalatest.tools

import org.scalatools.testing._
import org.scalatest.tools.Runner.parsePropertiesArgsIntoMap
import org.scalatest.tools.Runner.parseCompoundArgIntoSet
import SuiteDiscoveryHelper._
import StringReporter.colorizeLinesIndividually
import org.scalatest.Suite.formatterForSuiteStarting
import org.scalatest.Suite.formatterForSuiteCompleted
import org.scalatest.Suite.formatterForSuiteAborted
import org.scalatest.events.SuiteStarting
import org.scalatest.events.SuiteCompleted
import org.scalatest.events.SuiteAborted

/**
 * Class that makes ScalaTest tests visible to sbt.
 *
 * <p>
 * To use ScalaTest from within sbt, simply add a line like this to your project file, replacing 1.5 with whatever version you desire:
 * </p>
 *
 * <pre class="stExamples">
 * val scalatest = "org.scalatest" % "scalatest_2.8.1" % "1.5"
 * </pre>
 *
 * <p>
 * You can configure the output shown when running with sbt in four ways: 1) turn off color, 2) show
 * short stack traces, 3) full stack traces, and 4) show durations for everything. To do that
 * you need to add test options, like this:
 * </p>
 *
 * <pre class="stExamples">
 * override def testOptions = super.testOptions ++
 *   Seq(TestArgument(TestFrameworks.ScalaTest, "-oD"))
 * </pre>
 *
 * <p>
 * After the -o, place any combination of:
 * </p>
 *
 * <ul>
 * <li>D - show durations</li>
 * <li>S - show short stack traces</li>
 * <li>F - show full stack traces</li>
 * <li>W - without color</li>
 * </ul>
 *
 * <p>
 * For example, "-oDF" would show full stack traces and durations (the amount
 * of time spent in each test).
 * </p>
 *
 * @author Bill Venners
 * @author Josh Cough
 */
class ScalaTestFramework extends Framework {

  /**
   * Returns <code>"ScalaTest"</code>, the human readable name for this test framework.
   */
  def name = "ScalaTest"

  /**
   * Returns an array containing fingerprint for ScalaTest's test, which are classes  
   * whose superclass name is <code>org.scalatest.Suite</code>
   * or is annotated with <code>org.scalatest.WrapWith</code>.
   */
  def tests =
    Array(
      new org.scalatools.testing.TestFingerprint {
        def superClassName = "org.scalatest.Suite"
        def isModule = false
      },
      new org.scalatools.testing.AnnotatedFingerprint {
        def annotationName = "org.scalatest.WrapWith"
        def isModule = false
      }
    )

  /**
   * Returns an <code>org.scalatools.testing.Runner</code> that will load test classes via the passed <code>testLoader</code>
   * and direct output from running the tests to the passed array of <code>Logger</code>s.
   */
  def testRunner(testLoader: ClassLoader, loggers: Array[Logger]) = {
    new ScalaTestRunner(testLoader, loggers)
  }

  /**The test runner for ScalaTest suites. It is compiled in a second step after the rest of sbt.*/
  private[tools] class ScalaTestRunner(val testLoader: ClassLoader, val loggers: Array[Logger]) extends org.scalatools.testing.Runner2 {

    import org.scalatest._

    /* 
      test-only FredSuite -- -A -B -C -d  all things to right of == come in as a separate string in the array
 the other way is to set up the options and when I say test it always comes in that way

 new wqay, if one framework

testOptions in Test += Tests.Arguments("-d", "-g")

so each of those would come in as one separate string in the aray

testOptions in Test += Tests.Arguments(TestFrameworks.ScalaTest, "-d", "-g")

Remember:

maybe add a distributor like thing to run
maybe add some event things like pending, ignored as well skipped
maybe a call back for the summary

st look at wiki on xsbt

tasks & commands. commands have full control over everything.
tasks are more integrated, don't need to know as much.
write a sbt plugin to deploy the task.

     */
    def run(testClassName: String, fingerprint: Fingerprint, eventHandler: EventHandler, args: Array[String]) {
      val testClass = Class.forName(testClassName, true, testLoader)
      // println("sbt args: " + args.toList)
      if (isAccessibleSuite(testClass) || isRunnable(testClass)) {

        val (propertiesArgsList, includesArgsList,
        excludesArgsList, repoArgsList) = parsePropsAndTags(args.filter(!_.equals("")))
        val configMap: Map[String, String] = parsePropertiesArgsIntoMap(propertiesArgsList)
        val tagsToInclude: Set[String] = parseCompoundArgIntoSet(includesArgsList, "-n")
        val tagsToExclude: Set[String] = parseCompoundArgIntoSet(excludesArgsList, "-l")
        val filter = org.scalatest.Filter(if (tagsToInclude.isEmpty) None else Some(tagsToInclude), tagsToExclude)
        
        // If no reporters specified, just give them a default stdout reporter
        val fullReporterConfigurations: ReporterConfigurations = Runner.parseReporterArgsIntoConfigurations(if(repoArgsList.isEmpty) checkSbtLogNoFormat("-o") :: Nil else repoArgsList)

        // For 1.6.3, this will never return a Some(grs). Could just as well throw an exception if Some(grs).
        val reporterConfigs: ReporterConfigurations =
          fullReporterConfigurations.graphicReporterConfiguration match {
            case None => fullReporterConfigurations
            case Some(grs) => {
              new ReporterConfigurations(
                None,
                fullReporterConfigurations.fileReporterConfigurationList,
                fullReporterConfigurations.xmlReporterConfigurationList,
                fullReporterConfigurations.standardOutReporterConfiguration,
                fullReporterConfigurations.standardErrReporterConfiguration,
                fullReporterConfigurations.htmlReporterConfigurationList,
                fullReporterConfigurations.customReporterConfigurationList
              )
           }
          }
        
        val report: SbtReporter = new SbtReporter(eventHandler, Some(Runner.getDispatchReporter(reporterConfigs, None, None, testLoader)))

        val tracker = new Tracker
        val suiteStartTime = System.currentTimeMillis

        val wrapWithAnnotation = testClass.getAnnotation(classOf[WrapWith])
        val suite = 
        if (wrapWithAnnotation == null)
          testClass.newInstance.asInstanceOf[Suite]
        else {
          val suiteClazz = wrapWithAnnotation.value
          val constructorList = suiteClazz.getDeclaredConstructors()
          val constructor = constructorList.find { c => 
              val types = c.getParameterTypes
              types.length == 1 && types(0).isAssignableFrom(testClass)
            }
            constructor.get.newInstance(testClass.newInstance.asInstanceOf[Object]).asInstanceOf[Suite]
        }

        val formatter = formatterForSuiteStarting(suite)

        report(SuiteStarting(tracker.nextOrdinal(), suite.suiteName, Some(testClass.getName), formatter, None))

        try {
          suite.run(None, report, new Stopper {}, filter, configMap, None, tracker)

          val formatter = formatterForSuiteCompleted(suite)

          val duration = System.currentTimeMillis - suiteStartTime
          report(SuiteCompleted(tracker.nextOrdinal(), suite.suiteName, Some(testClass.getName), Some(duration), formatter, None))
        }
        catch {       
          case e: Exception => {

            // TODO: Could not get this from Resources. Got:
            // java.util.MissingResourceException: Can't find bundle for base name org.scalatest.ScalaTestBundle, locale en_US
            val rawString = "Exception encountered when attempting to run a suite with class name: " + testClass.getName
            val formatter = formatterForSuiteAborted(suite, rawString)

            val duration = System.currentTimeMillis - suiteStartTime
            report(SuiteAborted(tracker.nextOrdinal(), rawString, suite.suiteName, Some(testClass.getName), Some(e), Some(duration), formatter, None))
          }
        }
      }
      else throw new IllegalArgumentException("Class is not an accessible org.scalatest.Suite: " + testClassName)
    }

    private val emptyClassArray = new Array[java.lang.Class[T] forSome {type T}](0)
    
    private class SbtReporter(eventHandler: EventHandler, report: Option[Reporter]) extends Reporter {
      
      import org.scalatest.events._
      
      def fireEvent(tn: String, r: Result, e: Option[Throwable]) = {
        eventHandler.handle(
          new org.scalatools.testing.Event {
            def testName = tn
            def description = tn
            def result = r
            def error = e getOrElse null
          }
        )
      }
      
      override def apply(event: Event) {
        report match {
          case Some(report) => report(event)
          case None =>
        }
        event match {
          // the results of running an actual test
          case t: TestPending => fireEvent(t.testName, Result.Skipped, None)
          case t: TestFailed => fireEvent(t.testName, Result.Failure, t.throwable)
          case t: TestSucceeded => fireEvent(t.testName, Result.Success, None)
          case t: TestIgnored => fireEvent(t.testName, Result.Skipped, None)
          case t: SuiteAborted => fireEvent("!!! Suite Aborted !!!", Result.Failure, t.throwable)
          case _ => 
        }
      }
    }
    
    private def checkSbtLogNoFormat(s: String) = {
      if (System.getProperty("sbt.log.noformat") == "true" && !s.contains("W"))
        s + "W"
      else
        s  
    }

    private[scalatest] def parsePropsAndTags(args: Array[String]) = {

      import collection.mutable.ListBuffer

      val props = new ListBuffer[String]()
      val includes = new ListBuffer[String]()
      val excludes = new ListBuffer[String]()
      var repoArgs = new ListBuffer[String]()

      val it = args.iterator
      while (it.hasNext) {

        val s = it.next

        if (s.startsWith("-D")) {
          props += s
        }
        else if (s.startsWith("-n")) {
          includes += s
          if (it.hasNext)
            includes += it.next
        }
        else if (s.startsWith("-l")) {
          excludes += s
          if (it.hasNext)
            excludes += it.next
        }
        else if (s.startsWith("-o")) {
          repoArgs += checkSbtLogNoFormat(s)
        }
        //      else if (s.startsWith("-t")) {
        //
        //        testNGXMLFiles += s
        //        if (it.hasNext)
        //          testNGXMLFiles += it.next
        //      }
        else {
          throw new IllegalArgumentException("Unrecognized argument: " + s)
        }
      }
      (props.toList, includes.toList, excludes.toList, repoArgs.toList)
    }
  }
}
