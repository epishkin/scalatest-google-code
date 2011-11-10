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
      if (isAccessibleSuite(suiteClass)) {

        // Why are we getting rid of empty strings? Were empty strings coming in from sbt? -bv 11/09/2011
        val (propertiesArgsList, includesArgsList, excludesArgsList, repoArgsList) = parsePropsAndTags(args.filter(!_.equals("")))
        
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
        val report:Reporter = new SbtReporter(eventHandler, Some(Runner.getDispatchReporter(reporterConfigs, None, None, testLoader)))

        val tracker = new Tracker
        val suiteStartTime = System.currentTimeMillis

        val suite = suiteClass.newInstance

        val formatter = formatterForSuiteStarting(suite)

        report(SuiteStarting(tracker.nextOrdinal(), suite.suiteName, suite.suiteID, Some(suiteClass.getName), formatter, None))

        try {
          suite.run(None, report, new Stopper {}, filter, configMap, None, tracker)

          val formatter = formatterForSuiteCompleted(suite)

          val duration = System.currentTimeMillis - suiteStartTime
          report(SuiteCompleted(tracker.nextOrdinal(), suite.suiteName, suite.suiteID, Some(suiteClass.getName), Some(duration), formatter, None))
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
            report(SuiteAborted(tracker.nextOrdinal(), rawString, suite.suiteName, suite.suiteID, Some(suiteClass.getName), Some(e), Some(duration), formatter, None))
          }
        }
      }
      else throw new IllegalArgumentException("Class is not an accessible org.scalatest.Suite: " + testClassName)
    }

    private val emptyClassArray = new Array[java.lang.Class[T] forSome {type T}](0)

    // TODO: Chee Seng: At some point, please reuse the identical method in SuiteDiscoveryHelper instead
    private def isAccessibleSuite(clazz: java.lang.Class[_]): Boolean = {
      import java.lang.reflect.Modifier

      try {
        classOf[Suite].isAssignableFrom(clazz) &&
                Modifier.isPublic(clazz.getModifiers) &&
                !Modifier.isAbstract(clazz.getModifiers) &&
                Modifier.isPublic(clazz.getConstructor(emptyClassArray: _*).getModifiers)
      } catch {
        case nsme: NoSuchMethodException => false
        case se: SecurityException => false
      }
    }
    
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
    
    private[scalatest] def parseUntilFound(value:String, endsWith:String, it:Iterator[String]):String = {
      if(it.hasNext) {
        val next = it.next()
        if(next.endsWith(endsWith))
          value + next
        else
          parseUntilFound(value + next, endsWith, it)
      }
      else
        throw new IllegalArgumentException("Unable to find '" + endsWith + "'")
    }
    
    private[scalatest] def parseParams(rawParamsStr:String, it:Iterator[String], validParamSet:Set[String], expected:String):Map[String, String] = {
      
      if(rawParamsStr.length() > 0) {
        if(!rawParamsStr.startsWith("("))
          throw new IllegalArgumentException("Invalid configuration, example valid configuration: " + expected)
      
        val paramsStr = 
         if(rawParamsStr.endsWith(")"))
           rawParamsStr
         else 
           parseUntilFound(rawParamsStr, ")", it)
      
        val configsArr:Array[String] = paramsStr.substring(1, paramsStr.length() - 1).split(",")
        val tuples = for(configStr <- configsArr) yield {
          val keyValueArr = configStr.trim().split("=")
          if(keyValueArr.length == 2) {
            // Value config param
            val key:String = keyValueArr(0).trim()
            if(!validParamSet.contains(key))
              throw new IllegalArgumentException("Invalid configuration: " + key)
            val rawValue = keyValueArr(1).trim()
            val value:String = 
              if(rawValue.startsWith("\"") && rawValue.endsWith("\"") && rawValue.length() > 1) 
                rawValue.substring(1, rawValue.length() - 1)
              else
                rawValue
            (key -> value)
          }
          else
            throw new IllegalArgumentException("Invalid configuration: " + configStr)
        }
        Map[String, String]() ++ tuples
      }
      else
        Map[String, String]()
    }
    
    private[scalatest] val validConfigMap = Map(
                                             "dropteststarting" -> "N", 
                                             "doptestsucceeded" -> "C", 
                                             "droptestignored" -> "X", 
                                             "droptestpending" -> "E", 
                                             "dropsuitestarting" -> "H", 
                                             "dropsuitecompleted" -> "L", 
                                             "dropinfoprovided" -> "O", 
                                             "nocolor" -> "W", 
                                             "shortstacks" -> "S", 
                                             "fullstacks" -> "F", 
                                             "durations" -> "D"
                                           )
    
    private[scalatest] def translateConfigs(rawConfigs:String):String = {
      val configArr = rawConfigs.split(" ")
      val translatedArr = configArr.map {config => 
            val translatedOpt:Option[String] = validConfigMap.get(config)
            translatedOpt match {
              case Some(translated) => translated
              case None => throw new IllegalArgumentException("Invalid config value: " + config)
            }
          }
      translatedArr.mkString
    }
    
    private[scalatest] def getTranslatedConfig(paramsMap:Map[String, String]):String = {
      val configOpt:Option[String] = paramsMap.get("config")
	  configOpt match {
	    case Some(configStr) => translateConfigs(configStr)
	    case None => ""
	  }
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
        else if (s.startsWith("-g")) {
	      repoArgs += s
	    }
	    else if (s.startsWith("-o")) {
	      println("-o is deprecated, use stdout instead.")
	      repoArgs += s
	    }
	    else if (s.startsWith("stdout")) {
	      val paramsMap:Map[String, String] = parseParams(s.substring("stdout".length()), it, Set("config"), "stdout")
	      repoArgs += "-o" + getTranslatedConfig(paramsMap:Map[String, String])
	    }
	    else if (s.startsWith("-e")) {
	      println("-e is deprecated, use stderr instead.")
	      repoArgs += s
	    }
        else if (s.startsWith("stderr")) {
	      val paramsMap:Map[String, String] = parseParams(s.substring("stderr".length()), it, Set("config"), "stderr")
	      repoArgs += "-e" + getTranslatedConfig(paramsMap:Map[String, String])
	    }
	    else if (s.startsWith("-f")) {
	      println("-f is deprecated, use file(directory=\"xxx\") instead.")
	      repoArgs += s
	      if (it.hasNext)
	        repoArgs += it.next
	    }
	    else if (s.startsWith("file")) {
	      val paramsMap:Map[String, String] = parseParams(s.substring("file".length()), it, Set("filename", "config"), "junitxml(directory=\"xxx\")")
	      repoArgs += "-f" + getTranslatedConfig(paramsMap:Map[String, String])
	      val filenameOpt:Option[String] = paramsMap.get("filename")
	      filenameOpt match {
	        case Some(filename) => repoArgs += filename
	        case None => throw new IllegalArgumentException("file requires filename to be specified, example: file(filename=\"xxx\")")
	      }
	    }
	    else if (s.startsWith("-u")) {
	        println("-u is deprecated, use junitxml(directory=\"xxx\") instead.")
	        repoArgs += s
	        if (it.hasNext)
	          repoArgs += it.next
	    }
	    else if(s.startsWith("junitxml")) {
	        repoArgs += "-u"
	        val paramsMap:Map[String, String] = parseParams(s.substring("junitxml".length()), it, Set("directory"), "junitxml(directory=\"xxx\")")
	        val directoryOpt:Option[String] = paramsMap.get("directory")
	        directoryOpt match {
	          case Some(dir) => repoArgs += dir
	          case None => throw new IllegalArgumentException("junitxml requires directory to be specified, example: junitxml(directory=\"xxx\")")
	        }
	    }
	    else if (s.startsWith("-d")) {
	        repoArgs += s
	        if (it.hasNext)
	          repoArgs += it.next
	    }
	    else if (s.startsWith("-a")) {
	        repoArgs += s
	        if (it.hasNext)
	          repoArgs += it.next
	    }
	    else if (s.startsWith("-x")) {
	        repoArgs += s
	        if (it.hasNext)
	          repoArgs += it.next
	    }
	    else if (s.startsWith("-h")) {
	        repoArgs += s
	        if (it.hasNext)
	          repoArgs += it.next
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
