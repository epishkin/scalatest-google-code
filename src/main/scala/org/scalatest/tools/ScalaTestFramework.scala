package org.scalatest.tools

import org.scalatools.testing._
import org.scalatest.tools.Runner.parsePropertiesArgsIntoMap
import org.scalatest.tools.Runner.parseCompoundArgIntoSet
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
 * To use ScalaTest from within sbt, simply add a line like this to your project file (for sbt 0.1.0 or higher):
 * </p>
 *
 * <pre class="stExamples">
 * libraryDependencies += "org.scalatest" % "scalatest_2.9.0" % "1.6.1" % "test"
 * </pre>
 *
 * <p>
 * The above line of code will work for any version of Scala 2.9 (for example, it works for Scala 2.9.0-1).
 * </p>
 *
 * <pre class="stExamples">
 * libraryDependencies += "org.scalatest" % "scalatest_2.8.1" % "1.5.1" % "test"
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
   * Returns an array containing one <code>org.scalatools.testing.TestFingerprint</code> object, whose superclass name is <code>org.scalatest.Suite</code>
   * and <code>isModule</code> value is false.
   */
  def tests =
    Array(
      new org.scalatools.testing.TestFingerprint {
        def superClassName = "org.scalatest.Suite"
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
  private[tools] class ScalaTestRunner(val testLoader: ClassLoader, val loggers: Array[Logger]) extends org.scalatools.testing.Runner {

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

Commands that should work:

-Ddbname=testdb -Dserver=192.168.1.188
Can't do a runpath
Can add more reporters. -g seems odd, but could be done, -o seems odd. Maybe it is a no-op. -e could work. -r for sure. -u for sure.
Ask Mark about -o. If there's some way to turn off his output, then that could mean -o. Or maybe -o is the default, which I think
it should be for runner anyway, and then if you say -g you don't get -o. Meaning I don't send the strings to log. yes, -o maybe
means log in the sbt case.

Reporters can be configured.

Tags to include and exclude: -n "CheckinTests FunctionalTests" -l "SlowTests NetworkTests"


     */
    def run(testClassName: String, fingerprint: TestFingerprint, eventHandler: EventHandler, args: Array[String]) {
      val suiteClass = Class.forName(testClassName, true, testLoader).asSubclass(classOf[Suite])
       //println("sbt args: " + args.toList)
      if (SuiteDiscoveryHelper.isAccessibleSuite(suiteClass)) {

        // Why are we getting rid of empty strings? Were empty strings coming in from sbt? -bv 11/09/2011
        val translator = new SbtFriendlyParamsTranslator();
        val (propertiesArgsList, includesArgsList, excludesArgsList, repoArgsList, concurrentList, memberOnlyList, wildcardList, 
            suiteList, junitList, testngList) = translator.parsePropsAndTags(args.filter(!_.equals("")))
        
        val configMap: Map[String, String] = parsePropertiesArgsIntoMap(propertiesArgsList)
        val tagsToInclude: Set[String] = parseCompoundArgIntoSet(includesArgsList, "-n")
        val tagsToExclude: Set[String] = parseCompoundArgIntoSet(excludesArgsList, "-l")
        val filter = org.scalatest.Filter(if (tagsToInclude.isEmpty) None else Some(tagsToInclude), tagsToExclude)
        
        // If no reporters specified, just give them a default stdout reporter
        val fullReporterConfigurations: ReporterConfigurations = Runner.parseReporterArgsIntoConfigurations(if(repoArgsList.isEmpty) "-o" :: Nil else repoArgsList)
          val reporterConfigs: ReporterConfigurations =
            fullReporterConfigurations.graphicReporterConfiguration match {
              case None => fullReporterConfigurations
              case Some(grs) => {
                new ReporterConfigurations(
                  None,
                  fullReporterConfigurations.fileReporterConfigurationList,
                  fullReporterConfigurations.junitXmlReporterConfigurationList,
                  fullReporterConfigurations.dashboardReporterConfigurationList,
                  fullReporterConfigurations.xmlReporterConfigurationList,
                  fullReporterConfigurations.standardOutReporterConfiguration,
                  fullReporterConfigurations.standardErrReporterConfiguration,
                  fullReporterConfigurations.htmlReporterConfigurationList,
                  fullReporterConfigurations.customReporterConfigurationList
                )
             }
            }
        
        // TODO: Chee Seng, when you add support for the graphic reporter, I think it makes sense to hold up the build tool until
        // the graphic reporter is exited. This is what we do in Runner for the ant task I think.
        // Actually, I wonder if don't want some kind of private[tools] run method in Runner that takes command line arguments
        // and a classloader, and this gets called by the public main and run methods. We can talk about that over the phone.
        // After discussion -> TODO this in the custom task.
        val report:Reporter = new SbtReporter(eventHandler, Some(Runner.getDispatchReporter(reporterConfigs, None, None, testLoader)))

        val tracker = new Tracker
        val suiteStartTime = System.currentTimeMillis

        val suite = suiteClass.newInstance

        val formatter = formatterForSuiteStarting(suite)

        report(SuiteStarting(tracker.nextOrdinal(), suite.suiteName, suite.suiteID, Some(suiteClass.getName), suite.decodedSuiteName, formatter, None))

        try {
          suite.run(None, report, new Stopper {}, filter, configMap, None, tracker)

          val formatter = formatterForSuiteCompleted(suite)

          val duration = System.currentTimeMillis - suiteStartTime
          report(SuiteCompleted(tracker.nextOrdinal(), suite.suiteName, suite.suiteID, Some(suiteClass.getName), suite.decodedSuiteName, Some(duration), formatter, None))
        }
        catch {       
          case e: Exception => {

            // TODO: Could not get this from Resources. Got:
            // java.util.MissingResourceException: Can't find bundle for base name org.scalatest.ScalaTestBundle, locale en_US
            // TODO Chee Seng, I wonder why we couldn't access resources, and if that's still true. I'd rather get this stuff
            // from the resource file so we can later localize.
            val rawString = "Exception encountered when attempting to run a suite with class name: " + suiteClass.getName
            val formatter = formatterForSuiteAborted(suite, rawString)

            val duration = System.currentTimeMillis - suiteStartTime
            report(SuiteAborted(tracker.nextOrdinal(), rawString, suite.suiteName, suite.suiteID, Some(suiteClass.getName), suite.decodedSuiteName, Some(e), Some(duration), formatter, None))
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
          case _ => 
        }
      }
    }
  }
}

private[scalatest] class SbtFriendlyParamsTranslator extends FriendlyParamsTranslator {
  override private[scalatest] def validateSupportedPropsAndTags(s:String) {
    if(s.startsWith("-g") || s.startsWith("graphic") || 
       s.startsWith("-f") || s.startsWith("file") || 
       s.startsWith("-u") || s.startsWith("junitxml") || 
       s.startsWith("-d") || s.startsWith("-a") || s.startsWith("dashboard") || 
       s.startsWith("-x") || s.startsWith("xml") || 
       s.startsWith("-h") || s.startsWith("html") || 
       s.startsWith("-r") || s.startsWith("reporterclass") || 
       s == "-c" || s == "concurrent" || 
       s == "-m" || s.startsWith("memberonly") || 
       s == "-w" || s.startsWith("wildcard") || 
       s == "-s" || s.startsWith("suite") || 
       s == "-j" || s.startsWith("junit") || 
       s == "-t" || s.startsWith("testng"))
      throw new IllegalArgumentException("Argument '" + s + "' is not supported using test/test-only, use scalatest task instead.")
    
  }
}
