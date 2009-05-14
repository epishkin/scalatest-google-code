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
package org.scalatest.tools

import scala.collection.mutable.ListBuffer
import java.lang.reflect.Constructor
import java.lang.reflect.Modifier
import java.net.URL
import java.net.MalformedURLException
import java.net.URLClassLoader
import java.io.File
import java.io.IOException
import javax.swing.SwingUtilities
import java.util.concurrent.ArrayBlockingQueue
import org.scalatest.testng.TestNGWrapperSuite
import java.util.concurrent.Semaphore
import org.scalatest.events._

/**
 * <p>
 * Application that runs a suite of tests.
 * The application accepts command line arguments that specify optional <em>user-defined properties</em>, an optional 
 * <em>runpath</em>, zero to many <code>Reporter</code>s, optional lists of test groups to include and/or exclude, zero to many
 * <code>Suite</code> class names, zero to many "members-only" <code>Suite</code> paths, zero to many "wildcard" <code>Suite</code> paths,
 * and zero to many TestNG XML config file paths.
 * All of these arguments are described in more detail below. Here's a summary:
 * </p>
 *
 * <p>
 * <code>scala [-classpath scalatest-&lt;version&gt;.jar:...] org.scalatest.tools.Runner [-D&lt;key&gt;=&lt;value&gt; [...]] [-p &lt;runpath&gt;] [reporter [...]] [-n &lt;includes&gt;] [-x &lt;excludes&gt;] [-c] [-s &lt;suite class name&gt; [...]] [-m &lt;members-only suite path&gt; [...]] [-w &lt;wildcard suite path&gt; [...]] [-t &lt;TestNG config file path&gt; [...]]</code>
 * </p>
 *
 * <p>
 * The simplest way to start <code>Runner</code> is to specify the directory containing your compiled tests as the sole element of the runpath, for example:
 * </p>
 *
 * <p>
 * <code>scala -classpath scalatest-&lt;version&gt;.jar org.scalatest.tools.Runner -p compiled_tests</code>
 * </p>
 *
 * <p>
 * Given the previous command, <code>Runner</code> will discover and execute all <code>Suite</code>s in the <code>compiled_tests</code> directory and its subdirectories,
 * and show results in graphical user interface (GUI).
 * </p>
 *
 * <p>
 * <strong>Specifying user-defined properties</strong>
 * </p>
 *
 * <p>
 * A user-defined property consists of a key and a value. The key may not begin with
 * &quot;org.scalatest.&quot;. User-defined properties may be specified on the command line.
 * Each property is denoted with a "-D", followed immediately by the key string, an &quot;=&quot;, and the value string.
 * For example:
 * </p>
 *
 * <p>
 * <code>-Ddbname=testdb -Dserver=192.168.1.188</code>
 * </p>
 *
 * <p>
 * <strong>Specifying a runpath</strong>
 * </p>
 *
 * <p>
 * A runpath is the list of filenames, directory paths, and/or URLs that <code>Runner</code>
 * uses to load classes for the running test. If runpath is specified, <code>Runner</code> creates
 * a custom class loader to load classes available on the runpath.
 * The graphical user interface reloads the test classes anew for each run
 * by creating and using a new instance of the custom class loader for each run.
 * The classes that comprise the test may also be made available on
 * the classpath, in which case no runpath need be specified.
 * </p>
 *
 * <p>
 * The runpath is specified with the <b>-p</b> option. The <b>-p</b> must be followed by a space,
 * a double quote (<code>"</code>), a white-space-separated list of
 * paths and URLs, and a double quote. If specifying only one element in the runpath, you can leave off
 * the double quotes, which only serve to combine a white-space separated list of strings into one
 * command line argument. Here's an example:
 * </p>
 *
 * <p>
 * <code>-p "serviceuitest-1.1beta4.jar myjini http://myhost:9998/myfile.jar"</code>
 * </p>
 *
 * <p>
 * <strong>Specifying reporters</strong>
 * </p>
 *
 * <p>
 * Reporters can be specified  on the command line in any of the following
 * ways:
 * </p>
 *
 * <ul>
 * <li> <code><b>-g[configs...]</b></code> - causes display of a graphical user interface that allows
 *    tests to be run and results to be investigated
 * <li> <code><b>-f[configs...] &lt;filename&gt;</b></code> - causes test results to be written to
 *     the named file
 * <li> <code><b>-o[configs...]</b></code> - causes test results to be written to
 *     the standard output
 * <li> <code><b>-e[configs...]</b></code> - causes test results to be written to
 *     the standard error
 * <li> <code><b>-r[configs...] &lt;reporterclass&gt;</b></code> - causes test results to be reported to
 *     an instance of the specified fully qualified <code>Reporter</code> class name
 * </ul>
 *
 * <p>
 * The <code><b>[configs...]</b></code> parameter, which is used to configure reporters, is described in the next section.
 * </p>
 *
 * <p>
 * The <code><b>-r</b></code> option causes the reporter specified in
 * <code><b>&lt;reporterclass&gt;</b></code> to be
 * instantiated.
 * Each reporter class specified with a <b>-r</b> option must be public, implement
 * <code>org.scalatest.Reporter</code>, and have a public no-arg constructor.
 * Reporter classes must be specified with fully qualified names. 
 * The specified reporter classes may be
 * deployed on the classpath. If a runpath is specified with the
 * <code>-p</code> option, the specified reporter classes may also be loaded from the runpath.
 * All specified reporter classes will be loaded and instantiated via their no-arg constructor.
 * </p>
 *
 * <p>
 * For example, to run a suite named <code>MySuite</code> from the <code>mydir</code> directory
 * using two reporters, the graphical reporter and a file reporter
 * writing to a file named <code>"test.out"</code>, you would type:
 * </p>
 *
 * <p>
 * <code>java -jar scalatest.jar -p mydir <b>-g -f test.out</b> -s MySuite</code>
 * </p>
 *
 * <p>
 * The <code><b>-g</b></code>, <code><b>-o</b></code>, or <code><b>-e</b></code> options can
 * appear at most once each in any single command line.
 * Multiple appearances of <code><b>-f</b></code> and <code><b>-r</b></code> result in multiple reporters
 * unless the specified <code><b>&lt;filename&gt;</b></code> or <code><b>&lt;reporterclass&gt;</b></code> is
 * repeated. If any of <code><b>-g</b></code>, <code><b>-o</b></code>, <code><b>-e</b></code>,
 * <code><b>&lt;filename&gt;</b></code> or <code><b>&lt;reporterclass&gt;</b></code> are repeated on
 * the command line, the <code>Runner</code> will print an error message and not run the tests.
 * </p>
 *
 * <p>
 * <code>Runner</code> adds the reporters specified on the command line to a <em>dispatch reporter</em>,
 * which will dispatch each method invocation to each contained reporter. <code>Runner</code> will pass
 * the dispatch reporter to executed suites. As a result, every
 * specified reporter will receive every report generated by the running suite of tests.
 * If no reporters are specified, a graphical
 * runner will be displayed that provides a graphical report of
 * executed suites.
 * </p>
 *
 * <p>
 * <strong>Configuring Reporters</strong>
 * </p>
 *
 * <p>
 * Each reporter specification on the command line can include configuration characters. Configuration
 * characters
 * are specified immediately following the <code><b>-g</b></code>, <code><b>-o</b></code>,
 * <code><b>-e</b></code>, <code><b>-f</b></code>, or <code><b>-r</b></code>. Valid configuration
 * characters are:
 * </p>
 *
 * <ul>
 * <li> <code><b>Y</b></code> - present <code>runStarting</code> method invocations
 * <li> <code><b>Z</b></code> - present <code>testStarting</code> method invocations
 * <li> <code><b>T</b></code> - present <code>testSucceeded</code> method invocations
 * <li> <code><b>F</b></code> - present <code>testFailed</code> method invocations
 * <li> <code><b>G</b></code> - present <code>testIgnored</code> method invocations
 * <li> <code><b>U</b></code> - present <code>suiteStarting</code> method invocations
 * <li> <code><b>P</b></code> - present <code>suiteCompleted</code> method invocations
 * <li> <code><b>B</b></code> - present <code>suiteAborted</code> method invocations
 * <li> <code><b>I</b></code> - present <code>infoProvided</code> method invocations
 * <li> <code><b>S</b></code> - present <code>runStopped</code> method invocations
 * <li> <code><b>A</b></code> - present <code>runAborted</code> method invocations
 * <li> <code><b>R</b></code> - present <code>runCompleted</code> method invocations
 * </ul>
 *
 * <p>
 * Each reporter class has a default configuration. If no configuration
 * is specified on the command line for a particular reporter, that
 * reporter uses its default configuration. If a configuration is specified, <code>Runner</code> will present
 * to the configured reporter only those report types mentioned in the configuration characters. If the command
 * line includes argument <code>-oFAB</code>, for example, only <code>testFailed</code>, 
 * <code>runAborted</code>, and <code>suiteAborted</code> events will be reported to the standard output reporter.
 * </p>
 *
 * <p>
 * For example, to run a suite using two reporters, the graphical reporter (using its default
 * configuration) and a standard error reporter configured to print only test failures, run aborts, and
 * suite aborts, you would type:
 * </p>
 *
 * <p>
 * <code>scala -classpath scalatest-&lt;version&gt;.jar -p mydir <strong>-g -eFAB</strong> -s MySuite</code>
 * </p>
 *
 * <p>
 * Note that no white space is allowed between the reporter option and the initial configuration
 * parameters. So <code>"-e FAB"</code> will not work,
 * <code>"-eFAB"</code> will work.
 * </p>
 *
 * <p>
 * <strong>Specifying includes and excludes</strong>
 * </p>
 *
 * <p>
 * You can specify named groups of tests to include or exclude from a run. To specify includes,
 * use <code>-n</code> followed by a white-space-separated list of group names to include, surrounded by
 * double quotes. (The double quotes are not needed if specifying just one group.)  Similarly, to specify excludes, use <code>-x</code> followed by a white-space-separated
 * list of group names to exclude, surrounded by double quotes. (As before, the double quotes are not needed if specifying just one group.) If includes is not specified, then all tests
 * except those mentioned in the excludes group (and in the <code>Ignore</code> group), will be executed.
 * (In other words, an empty includes list is like a wildcard, indicating all tests be included.)
 * If includes is specified, then only those tests in groups mentioned in the argument following <code>-n</code>
 * and not mentioned in the excludes group, will be executed. For more information on test groups, see
 * the <a href="Suite.html">documentation for <code>Suite</code></a>. Here are some examples:
 * </p>
 *
 * <p>
 * <ul>
 * <li><code>-n CheckinTests</code></li>
 * <li><code>-n FunctionalTests -x SlowTests</code></li>
 * <li><code>-n "CheckinTests FunctionalTests"-x "SlowTests NetworkTests"</code></li>
 * </ul>
 * </p>
 *
 * <p>
 * <strong>Executing <code>Suite</code>s concurrently</strong>
 * </p>
 *
 * <p>
 * With the proliferation of multi-core architectures, and the often parallelizable nature of tests, it is useful to be able to run
 * tests concurrently. If you include <code>-c</code> on the command line, <code>Runner</code> will pass a <code>Distributor</code> to 
 * the <code>Suite</code>s you specify with <code>-s</code>. <code>Runner</code> will set up a thread pool to execute any <code>Suite</code>s
 * passed to the <code>Distributor</code>'s <code>put</code> method concurrently. Trait <code>Suite</code>'s implementation of
 * <code>runNestedSuites</code> will place any nested <code>Suite</code>s into this <code>Distributor</code>. Thus, if you have a <code>Suite</code>
 * of tests that must be executed sequentially, you should override <code>runNestedSuites</code> as described in the <a href="Distributor.html">documentation for <code>Distributor</code></a>.
 * </p>
 *
 * <p>
 * <strong>Specifying <code>Suite</code>s</strong>
 * </p>
 *
 * <p>
 * Suites are specified on the command line with a <b>-s</b> followed by the fully qualified
 * name of a <code>Suite</code> subclass, as in:
 * </p>
 *
 * <p>
 * <code>-s com.artima.serviceuitest.ServiceUITestkit</code>
 * </p>
 *
 * <p>
 * Each specified suite class must be public, a subclass of
 * <code>org.scalatest.Suite</code>, and contain a public no-arg constructor.
 * <code>Suite</code> classes must be specified with fully qualified names. 
 * The specified <code>Suite</code> classes may be
 * loaded from the classpath. If a runpath is specified with the
 * <code>-p</code> option, specified <code>Suite</code> classes may also be loaded from the runpath.
 * All specified <code>Suite</code> classes will be loaded and instantiated via their no-arg constructor.
 * </p>
 *
 * <p>
 * The runner will invoke <code>execute</code> on each instantiated <code>org.scalatest.Suite</code>,
 * passing in the dispatch reporter to each <code>execute</code> method.
 * </p>
 *
 * <p>
 * <code>Runner</code> is intended to be used from the command line. It is included in <code>org.scalatest</code>
 * package as a convenience for the user. If this package is incorporated into tools, such as IDEs, which take
 * over the role of runner, object <code>org.scalatest.tools.Runner</code> may be excluded from that implementation of the package.
 * All other public types declared in package <code>org.scalatest.tools.Runner</code> should be included in any such usage, however,
 * so client software can count on them being available.
 * </p>
 *
 * <p>
 * <strong>Specifying "members-only" and "wildcard" <code>Suite</code> paths</strong>
 * </p>
 *
 * <p>
 * If you specify <code>Suite</code> path names with <code>-m</code> or <code>-w</code>, <code>Runner</code> will automatically
 * discover and execute accessible <code>Suite</code>s in the runpath that are either a member of (in the case of <code>-m</code>)
 * or enclosed by (in the case of <code>-w</code>) the specified path. As used in this context, a <em>path</em> is a portion of a fully qualified name.
 * For example, the fully qualifed name <code>com.example.webapp.MySuite</code> contains paths <code>com</code>, <code>com.example</code>, and <code>com.example.webapp</code>.
 * The fully qualifed name <code>com.example.webapp.MyObject.NestedSuite</code> contains paths <code>com</code>, <code>com.example</code>,
 * <code>com.example.webapp</code>, and <code>com.example.webapp.MyObject</code>.
 * An <em>accessible <code>Suite</code></em> is a public class that extends <code>org.scalatest.Suite</code>
 * and defines a public no-arg constructor. Note that <code>Suite</code>s defined inside classes and traits do not have no-arg constructors,
 * and therefore won't be discovered. <code>Suite</code>s defined inside singleton objects, however, do get a no-arg constructor by default, thus
 * they can be discovered.
 * </p>
 *
 * <p>
 * For example, if you specify <code>-m com.example.webapp</code>
 * on the command line, and you've placed <code>com.example.webapp.RedSuite</code> and <code>com.example.webapp.BlueSuite</code>
 * on the runpath, then <code>Runner</code> will instantiate and execute both of those <code>Suite</code>s. The difference
 * between <code>-m</code> and <code>-w</code> is that for <code>-m</code>, only <code>Suite</code>s that are direct members of the named path
 * will be discovered. For <code>-w</code>, any <code>Suite</code>s whose fully qualified
 * name begins with the specified path will be discovered. Thus, if <code>com.example.webapp.controllers.GreenSuite</code>
 * exists on the runpath, invoking <code>Runner</code> with <code>-w com.example.webapp</code> will cause <code>GreenSuite</code>
 * to be discovered, because its fully qualifed name begins with <code>"com.example.webapp"</code>. But if you invoke <code>Runner</code>
 * with <code>-m com.example.webapp</code>, <code>GreenSuite</code> will <em>not</em> be discovered because it is directly
 * a member of <code>com.example.webapp.controllers</code>, not <code>com.example.webapp</code>.
 * </p>
 *
 * <p>
 * If you specify no <code>-s</code>, <code>-m</code>, or <code>-w</code> arguments on the command line to <code>Runner</code>, it will discover and execute all accessible <code>Suite</code>s
 * in the runpath.
 * </p>
 *
 * <p>
 * <strong>Specifying TestNG XML config file paths</strong>
 * </p>
 *
 * <p>
 * If you specify one or more file paths with <code>-t</code>, <code>Runner</code> will create a <code>org.scalatest.testng.TestNGWrapperSuite</code>,
 * passing in a <code>List</code> of the specified paths. When executed, the <code>TestNGWrapperSuite</code> will create one <code>TestNG</code> instance
 * and pass each specified file path to it for running. If you include <code>-t</code> arguments, you must include TestNG's jar file on the class path or runpath.
 * The <code>-t</code> argument will enable you to run existing <code>TestNG</code> tests, including tests written in Java, as part of a ScalaTest run.
 * You need not use <code>-t</code> to run suites written in Scala that extend <code>TestNGSuite</code>. You can simply run such suites with 
 * <code>-s</code>, <code>-m</code>, or </code>-w</code> parameters.
 * </p>
 *
 * @author Bill Venners
 * @author George Berger
 * @author Josh Cough
 */
object Runner {

  private val RUNNER_JFRAME_START_X: Int = 150
  private val RUNNER_JFRAME_START_Y: Int = 100

  //
  // We always include a PassFailReporter on runs in order to determine
  // whether or not all tests passed.
  //
  // The thread that calls Runner.run() will either start a GUI, if a graphic
  // reporter was requested, or just run the tests itself. If a GUI is started,
  // an event handler thread will get going, and it will start a RunnerThread,
  // which will actually do the running. The GUI can repeatedly start RunnerThreads
  // and RerunnerThreads, until the GUI is closed. If -c is specified, that means
  // the tests should be run concurrently, which in turn means a Distributor will
  // be passed to the execute method of the Suites, which will in turn populate
  // it with its nested suites instead of executing them directly in the same
  // thread. The Distributor works in conjunction with a pool of threads that
  // will take suites out of the distributor queue and execute them. The DispatchReporter
  // will serialize all reports via an actor, which because that actor uses receive
  // not react, will have its own thread. So the DispatchReporter actor's thread will
  // be the one that actually invokes testFailed, runAborted, etc., on this PassFailReporter.
  // The thread that invoked Runner.run(), will be the one that calls allTestsPassed.
  //
  // The thread that invoked Runner.run() will be the one to instantiate the PassFailReporter
  // and in its primary constructor acquire the single semaphore permit. This permit will
  // only be released by the DispatchReporter's actor thread when a runAborted, runStopped,
  // or runCompleted is invoked. allTestsPassed will block until it can reacquire the lone
  // semaphore permit. Thus, a PassFailReporter can just be used for one run, then it is
  // spent. A new PassFailReporter is therefore created each time the Runner.run() method is invoked.
  //
  private class PassFailReporter extends Reporter {

    @volatile private var failedAbortedOrStopped = false
    private val runDoneSemaphore = new Semaphore(1)
    runDoneSemaphore.acquire()

    override def apply(event: Event) {
      super.apply(event)
    }

    override def testFailed(report: Report) {
      failedAbortedOrStopped = true
    }
    override def runAborted(report: Report) {
      failedAbortedOrStopped = true
      runDoneSemaphore.release()
    }
    override def suiteAborted(report: Report) {
      failedAbortedOrStopped = true
    }
    override def runStopped() {
      failedAbortedOrStopped = true
      runDoneSemaphore.release()
    }
    override def runCompleted() {
      runDoneSemaphore.release()
    }

    def allTestsPassed = {
      runDoneSemaphore.acquire()
      !failedAbortedOrStopped
    }
  }

  // TODO: I don't think I'm enforcing that properties can't start with "org.scalatest"
  // TODO: I don't think I'm handling rejecting multiple -f/-r with the same arg. -f fred.txt -f fred.txt should
  // fail, as should -r MyReporter -r MyReporter. I'm failing on -o -o, -g -g, and -e -e, but the error messages
  // could indeed be nicer.
  /**
   * Runs a suite of tests, with optional GUI. See the main documentation for this singleton object for the details.
   */
  def main(args: Array[String]) {
    runOptionallyWithPassFailReporter(args, false)
  }

  /**
   * Runs a suite of tests, with optional GUI. See the main documentation for this singleton object for the details.
   * The difference between this method and <code>main</code> is simply that this method will block until the run
   * has completed, aborted, or been stopped, and return <code>true</code> if all tests executed and passed. In other
   * words, if any test fails, or if any suite aborts, or if the run aborts or is stopped, this method will
   * return <code>false</code>. This value is used, for example, by the ScalaTest ant task to determine whether
   * to continue the build if <code>haltOnFailure</code> is set to <code>true</code>.
   *
   * @return true if all tests were executed and passed.
   */
  def run(args: Array[String]): Boolean = {
    runOptionallyWithPassFailReporter(args, true)
  }

  private def runOptionallyWithPassFailReporter(args: Array[String], runWithPassFailReporter: Boolean): Boolean = {

    checkArgsForValidity(args) match {
      case Some(s) => {
        println(s)
        exit(1)
      }
      case None =>
    }

    val (
      runpathArgsList,
      reporterArgsList,
      suiteArgsList,
      propertiesArgsList,
      includesArgsList,
      excludesArgsList,
      concurrentList,
      membersOnlyArgsList,
      wildcardArgsList,
      testNGArgsList
    ) = parseArgs(args)

    val fullReporterSpecs: ReporterSpecs =
      if (reporterArgsList.isEmpty)
        // If no reporters specified, just give them a graphic reporter
        new ReporterSpecs(Some(GraphicReporterSpec(ReporterOpts.Set32(0))), Nil, None, None, Nil)
      else
        parseReporterArgsIntoSpecs(reporterArgsList)

    val suitesList: List[String] = parseSuiteArgsIntoNameStrings(suiteArgsList, "-s")
    val runpathList: List[String] = parseRunpathArgIntoList(runpathArgsList)
    val propertiesMap: Map[String, String] = parsePropertiesArgsIntoMap(propertiesArgsList)
    val includes: Set[String] = parseCompoundArgIntoSet(includesArgsList, "-n")
    val excludes: Set[String] = parseCompoundArgIntoSet(excludesArgsList, "-x")
    val concurrent: Boolean = !concurrentList.isEmpty
    val membersOnlyList: List[String] = parseSuiteArgsIntoNameStrings(membersOnlyArgsList, "-m")
    val wildcardList: List[String] = parseSuiteArgsIntoNameStrings(wildcardArgsList, "-w")
    val testNGList: List[String] = parseSuiteArgsIntoNameStrings(testNGArgsList, "-t")

    // Not yet supported
    val recipeName: Option[String] = None

    // If there's a graphic reporter, we need to leave it out of
    // reporterSpecs, because we want to pass all reporterSpecs except
    // the graphic reporter's to the RunnerJFrame (because RunnerJFrame *is*
    // the graphic reporter).
    val reporterSpecs: ReporterSpecs =
      fullReporterSpecs.graphicReporterSpec match {
        case None => fullReporterSpecs
        case Some(grs) => {
          new ReporterSpecs(
            None,
            fullReporterSpecs.fileReporterSpecList,
            fullReporterSpecs.standardOutReporterSpec,
            fullReporterSpecs.standardErrReporterSpec,
            fullReporterSpecs.customReporterSpecList
          )
        }
      }

    val passFailReporter = if (runWithPassFailReporter) Some(new PassFailReporter) else None

    fullReporterSpecs.graphicReporterSpec match {
      case Some(GraphicReporterSpec(configSet)) => {
        val graphicConfigSet = if (configSet.isEmpty) ReporterOpts.allOptions else configSet
        val abq = new ArrayBlockingQueue[RunnerJFrame](1)
        usingEventDispatchThread {
          val rjf = new RunnerJFrame(recipeName, graphicConfigSet, reporterSpecs, suitesList, runpathList,
            includes, excludes, propertiesMap, concurrent, membersOnlyList, wildcardList, testNGList, passFailReporter)
          rjf.setLocation(RUNNER_JFRAME_START_X, RUNNER_JFRAME_START_Y)
          rjf.setVisible(true)
          rjf.prepUIForRunning()
          rjf.runFromGUI()
          abq.put(rjf)
        }
        // To get the Ant task to work, the main thread needs to block until
        // The GUI window exits.
        val rjf = abq.take()
        rjf.blockUntilWindowClosed()
      }
      case None => { // Run the test without a GUI
        withClassLoaderAndDispatchReporter(runpathList, reporterSpecs, None, passFailReporter) {
          (loader, dispatchReporter) => {
            doRunRunRunADoRunRun(dispatchReporter, suitesList, new Stopper {}, includes, excludesWithIgnore(excludes),
                propertiesMap, concurrent, membersOnlyList, wildcardList, testNGList, runpathList, loader, new RunDoneListener {}, 1) 
          }
        }
      }
    }
    
    passFailReporter match {
      case Some(pfr) => pfr.allTestsPassed
      case None => false
    }
  }

  // Returns an Option[String]. Some is an error message. None means no error.
  private[scalatest] def checkArgsForValidity(args: Array[String]) = {

    val lb = new ListBuffer[String]
    val it = args.elements
    while (it.hasNext) {
      val s = it.next
      // Style advice
      // If it is multiple else ifs, then make it symetrical. If one needs an open curly brace, put it on all
      // If an if just has another if, a compound statement, go ahead and put the open curly brace's around the outer one
      if (s.startsWith("-p") || s.startsWith("-f") || s.startsWith("-r") || s.startsWith("-n") || s.startsWith("-x") || s.startsWith("-s") || s.startsWith("-m") || s.startsWith("-w") || s.startsWith("-t")) {
        if (it.hasNext)
          it.next
      }
      else if (!s.startsWith("-D") && !s.startsWith("-g") && !s.startsWith("-o") && !s.startsWith("-e") && !s.startsWith("-c")) {
        lb += s
      }
    }
    val argsList = lb.toList
    if (argsList.length != 0)
      Some("Unrecognized argument" + (if (argsList.isEmpty) ": " else "s: ") + argsList.mkString("", ", ", "."))
    else
      None
  }

  private[scalatest] def parseArgs(args: Array[String]) = {

    val runpath = new ListBuffer[String]()
    val reporters = new ListBuffer[String]()
    val suites = new ListBuffer[String]()
    val props = new ListBuffer[String]()
    val includes = new ListBuffer[String]()
    val excludes = new ListBuffer[String]()
    val concurrent = new ListBuffer[String]()
    val membersOnly = new ListBuffer[String]()
    val wildcard = new ListBuffer[String]()
    val testNGXMLFiles = new ListBuffer[String]()

    val it = args.elements
    while (it.hasNext) {

      val s = it.next

      if (s.startsWith("-D")) {
         props += s
      }
      else if (s.startsWith("-p")) {
        runpath += s
        if (it.hasNext)
          runpath += it.next
      }
      else if (s.startsWith("-g")) {
        reporters += s
      }
      else if (s.startsWith("-o")) {
        reporters += s
      }
      else if (s.startsWith("-e")) {
        reporters += s
      }
      else if (s.startsWith("-f")) {
        reporters += s
        if (it.hasNext)
          reporters += it.next
      }
      else if (s.startsWith("-n")) {
        includes += s
        if (it.hasNext)
          includes += it.next
      }
      else if (s.startsWith("-x")) {
        excludes += s
        if (it.hasNext)
          excludes += it.next
      }
      else if (s.startsWith("-r")) {

        reporters += s
        if (it.hasNext)
          reporters += it.next
      }
      else if (s.startsWith("-s")) {

        suites += s
        if (it.hasNext)
          suites += it.next
      }
      else if (s.startsWith("-m")) {

        membersOnly += s
        if (it.hasNext)
          membersOnly += it.next
      }
      else if (s.startsWith("-w")) {

        wildcard += s
        if (it.hasNext)
          wildcard += it.next
      }
      else if (s.startsWith("-c")) {

        concurrent += s
      }
      else if (s.startsWith("-t")) {

        testNGXMLFiles += s
        if (it.hasNext)
          testNGXMLFiles += it.next
      }
      else {
        throw new IllegalArgumentException("Unrecognized argument: " + s)
      }
    }

    (
      runpath.toList,
      reporters.toList,
      suites.toList,
      props.toList,
      includes.toList,
      excludes.toList,
      concurrent.toList,
      membersOnly.toList,
      wildcard.toList,
      testNGXMLFiles.toList
    )
  }

  /**
   * Returns a possibly empty ConfigSet containing configuration
   * objects specified in the passed reporterArg. Configuration
   * options are specified immediately following
   * the reporter option, as in:
   *
   * -oFA
   *
   * If no configuration options are specified, this method returns an
   * empty ConfigSet. This method never returns null.
   */
  private[scalatest] def parseConfigSet(reporterArg: String): ReporterOpts.Set32 = {

    if (reporterArg == null)
      throw new NullPointerException("reporterArg was null")

    if (reporterArg.length < 2)
      throw new IllegalArgumentException("reporterArg < 2")

    // The reporterArg passed includes the initial -, as in "-oFI",
    // so the first config param will be at index 2
    val configString = reporterArg.substring(2)
    val it = configString.elements
    val allConfigs = "YZTFUPBISARG" // G for test ignored
    var mask = 0
    while (it.hasNext) 
      it.next match {
        case 'Y' => mask = mask | ReporterOpts.PresentRunStarting.mask32
        case 'Z' => mask = mask | ReporterOpts.PresentTestStarting.mask32
        case 'T' => mask = mask | ReporterOpts.PresentTestSucceeded.mask32
        case 'F' => mask = mask | ReporterOpts.PresentTestFailed.mask32
        case 'U' => mask = mask | ReporterOpts.PresentSuiteStarting.mask32
        case 'P' => mask = mask | ReporterOpts.PresentSuiteCompleted.mask32
        case 'B' => mask = mask | ReporterOpts.PresentSuiteAborted.mask32
        case 'I' => mask = mask | ReporterOpts.PresentInfoProvided.mask32
        case 'S' => mask = mask | ReporterOpts.PresentRunStopped.mask32
        case 'A' => mask = mask | ReporterOpts.PresentRunAborted.mask32
        case 'R' => mask = mask | ReporterOpts.PresentRunCompleted.mask32
        case 'G' => mask = mask | ReporterOpts.PresentTestIgnored.mask32
        case c: Char => {

          // this should be moved to the checker, and just throw an exception here with a debug message. Or allow a MatchError.
          val msg1 = Resources("invalidConfigOption", String.valueOf(c)) + '\n'
          val msg2 =  Resources("probarg", reporterArg) + '\n'

          throw new IllegalArgumentException(msg1 + msg2)
        }
      }
    ReporterOpts.Set32(mask)
  }

  private[scalatest] def parseReporterArgsIntoSpecs(args: List[String]) = {

    if (args == null)
      throw new NullPointerException("args was null")

    if (args.exists(_ == null))
      throw new NullPointerException("an arg String was null")

    if (args.exists(_.length < 2)) // TODO: check and print out a user friendly message for this
      throw new IllegalArgumentException("an arg String was less than 2 in length: " + args)

    for (dashX <- List("-g", "-o", "-e")) {
      if (args.toList.count(_.substring(0, 2) == dashX) > 1) // TODO: also check and print a user friendly message for this
        throw new IllegalArgumentException("Only one " + dashX + " allowed")
    }

    // TODO: also check and print a user friendly message for this
    // again here, i had to skip some things, so I had to use an iterator.
    val it = args.elements
    while (it.hasNext) it.next.substring(0, 2) match {
      case "-g" => 
      case "-o" => 
      case "-e" => 
      case "-f" => if (it.hasNext)
                     it.next // scroll past the filename
                   else
                     throw new IllegalArgumentException("-f needs to be followed by a file name arg: ")
      case "-r" => if (it.hasNext)
                    it.next // scroll past the reporter class
                   else
                     throw new IllegalArgumentException("-r needs to be followed by a reporter class name arg: ")
      case arg: String => throw new IllegalArgumentException("An arg started with an invalid character string: " + arg)
    }

    val graphicReporterSpecOption = args.find(arg => arg.substring(0, 2) == "-g") match {
      case Some(dashGString) => Some(new GraphicReporterSpec(parseConfigSet(dashGString)))
      case None => None
    }

    def buildFileReporterSpecList(args: List[String]) = {
      val it = args.elements
      val lb = new ListBuffer[FileReporterSpec]
      while (it.hasNext) {
        val arg = it.next
        arg.substring(0,2) match {
          case "-f" => lb += new FileReporterSpec(parseConfigSet(arg), it.next)
          case _ => 
        }
      }
      lb.toList
    }
    val fileReporterSpecList = buildFileReporterSpecList(args)

    val standardOutReporterSpecOption = args.find(arg => arg.substring(0, 2) == "-o") match {
      case Some(dashOString) => Some(new StandardOutReporterSpec(parseConfigSet(dashOString)))
      case None => None
    }

    val standardErrReporterSpecOption = args.find(arg => arg.substring(0, 2) == "-e") match {
      case Some(dashEString) => Some(new StandardErrReporterSpec(parseConfigSet(dashEString)))
      case None => None
    }

    def buildCustomReporterSpecList(args: List[String]) = {
      val it = args.elements
      val lb = new ListBuffer[CustomReporterSpec]
      while (it.hasNext) {
        val arg = it.next
        arg.substring(0,2) match {
          case "-r" => lb += new CustomReporterSpec(parseConfigSet(arg), it.next)
          case _ => 
        }
      }
      lb.toList
    }
    val customReporterSpecList = buildCustomReporterSpecList(args)

    // Here instead of one loop, i go through the loop several times.
    new ReporterSpecs(
      graphicReporterSpecOption,
      fileReporterSpecList,
      standardOutReporterSpecOption,
      standardErrReporterSpecOption,
      customReporterSpecList
    )
  }

  // Used to parse -s, -m, and -w args, one of which will be passed as a String as dashArg
  private[scalatest] def parseSuiteArgsIntoNameStrings(args: List[String], dashArg: String) = {

    if (args == null)
      throw new NullPointerException("args was null")

    if (args.exists(_ == null))
      throw new NullPointerException("an arg String was null")

    if (dashArg != "-s" && dashArg != "-w" && dashArg != "-m" && dashArg != "-t")
      throw new NullPointerException("dashArg invalid: " + dashArg)

    val lb = new ListBuffer[String]
    val it = args.elements
    while (it.hasNext) {
      val dashS = it.next
      if (dashS != dashArg)
        throw new IllegalArgumentException("Every other element, starting with the first, must be -s")
      if (it.hasNext) {
        val suiteName = it.next
        if (!suiteName.startsWith("-"))
          lb += suiteName
        else
          throw new IllegalArgumentException("Expecting a Suite class name to follow -s, but got: " + suiteName)
      }
      else
        throw new IllegalArgumentException("Last element must be a Suite class name, not a -s.")
    }
    lb.toList
  }

  private[scalatest] def parseCompoundArgIntoSet(args: List[String], expectedDashArg: String): Set[String] = 
      Set() ++ parseCompoundArgIntoList(args, expectedDashArg)

  private[scalatest] def parseRunpathArgIntoList(args: List[String]): List[String] = parseCompoundArgIntoList(args, "-p")

  private[scalatest] def parseCompoundArgIntoList(args: List[String], expectedDashArg: String): List[String] = {

    if (args == null)
      throw new NullPointerException("args was null")

    if (args.exists(_ == null))
      throw new NullPointerException("an arg String was null")

    if (args.length == 0) {
      List()
    }
    else if (args.length == 2) {
      val dashArg = args(0)
      val runpathArg = args(1)

      if (dashArg != expectedDashArg)
        throw new IllegalArgumentException("First arg must be " + expectedDashArg + ", but was: " + dashArg)

      if (runpathArg.trim.isEmpty)
        throw new IllegalArgumentException("The runpath string must actually include some non-whitespace characters.")

      val tokens = runpathArg.split("\\s")

      tokens.toList
    }
    else {
      throw new IllegalArgumentException("Runpath must be either zero or two args: " + args)
    }
  }

  private[scalatest] def parsePropertiesArgsIntoMap(args: List[String]) = {

    if (args == null)
      throw new NullPointerException("args was null")

    if (args.exists(_ == null))
      throw new NullPointerException("an arg String was null")

    if (args.exists(_.indexOf('=') == -1))
      throw new IllegalArgumentException("A -D arg does not contain an equals sign.")

    if (args.exists(!_.startsWith("-D")))
      throw new IllegalArgumentException("A spice arg does not start with -D.")

    if (args.exists(_.indexOf('=') == 2))
      throw new IllegalArgumentException("A spice arg does not have a key to the left of the equals sign.")

    if (args.exists(arg => arg.indexOf('=') == arg.length - 1))
      throw new IllegalArgumentException("A spice arg does not have a value to the right of the equals sign.")

    val tuples = for (arg <- args) yield {
      val keyValue = arg.substring(2) // Cut off the -D at the beginning
      val equalsPos = keyValue.indexOf('=')
      val key = keyValue.substring(0, equalsPos)
      val value = keyValue.substring(equalsPos + 1)
      (key, value)
    }

    scala.collection.immutable.Map() ++ tuples
  }

  // For debugging.
/*
  private[scalatest] def printOpts(opt: ReporterOpts.Set32) {
    if (opt.contains(ReporterOpts.PresentRunStarting))
      println("PresentRunStarting")
    if (opt.contains(ReporterOpts.PresentTestStarting))
      println("PresentTestStarting")
    if (opt.contains(ReporterOpts.PresentTestSucceeded))
      println("PresentTestSucceeded")
    if (opt.contains(ReporterOpts.PresentTestFailed))
      println("PresentTestFailed")
    if (opt.contains(ReporterOpts.PresentTestIgnored))
      println("PresentTestIgnored")
    if (opt.contains(ReporterOpts.PresentSuiteStarting))
      println("PresentSuiteStarting")
    if (opt.contains(ReporterOpts.PresentSuiteCompleted))
      println("PresentSuiteCompleted")
    if (opt.contains(ReporterOpts.PresentSuiteAborted))
      println("PresentSuiteAborted")
    if (opt.contains(ReporterOpts.PresentInfoProvided))
      println("PresentInfoProvided")
    if (opt.contains(ReporterOpts.PresentRunStopped))
      println("PresentRunStopped")
    if (opt.contains(ReporterOpts.PresentRunCompleted))
      println("PresentRunCompleted")
    if (opt.contains(ReporterOpts.PresentRunAborted))
      println("PresentRunAborted")
  }
*/

  private[scalatest] def getDispatchReporter(reporterSpecs: ReporterSpecs, graphicReporter: Option[Reporter], passFailReporter: Option[Reporter], loader: ClassLoader) = {
    def getReporterFromSpec(spec: ReporterSpec): Reporter = spec match {
      case StandardOutReporterSpec(configSet) => {
        if (configSet.isEmpty)
          new StandardOutReporter
        else
          new FilterReporter(new StandardOutReporter, configSet)
      }
      case StandardErrReporterSpec(configSet) => {
        if (configSet.isEmpty)
          new StandardErrReporter
        else
          new FilterReporter(new StandardErrReporter, configSet)
      }
      case FileReporterSpec(configSet, fileName) => {
        if (configSet.isEmpty)
          new FileReporter(fileName)
        else
          new FilterReporter(new FileReporter(fileName), configSet)
      }
      case CustomReporterSpec(configSet, reporterClassName) => {
        val customReporter = getCustomReporter(reporterClassName, loader, "-r... " + reporterClassName)
        if (configSet.isEmpty)
          customReporter
        else
          new FilterReporter(customReporter, configSet)
      }
      case GraphicReporterSpec(configSet) => throw new RuntimeException("Should never happen.")
    }

    val reporterSeq =
      (for (spec <- reporterSpecs)
        yield getReporterFromSpec(spec))

    val almostFullReporterList: List[Reporter] =
      graphicReporter match {
        case None => reporterSeq.toList
        case Some(gRep) => gRep :: reporterSeq.toList
      }
      
    val fullReporterList: List[Reporter] =
      passFailReporter match {
        case Some(pfr) => pfr :: almostFullReporterList
        case None => almostFullReporterList
      }

    new DispatchReporter(fullReporterList)
  }

  private def getCustomReporter(reporterClassName: String, loader: ClassLoader, argString: String): Reporter = {
    try {
      val reporterClass: java.lang.Class[_] = loader.loadClass(reporterClassName) 
      reporterClass.newInstance.asInstanceOf[Reporter]
    }    // Could probably catch ClassCastException too
    catch {
      case e: ClassNotFoundException => {

        val msg1 = Resources("cantLoadReporterClass", reporterClassName)
        val msg2 = Resources("probarg", argString)
        val msg = msg1 + "\n" + msg2
    
        val iae = new IllegalArgumentException(msg)
        iae.initCause(e)
        throw iae
      }
      case e: InstantiationException => {

        val msg1 = Resources("cantInstantiateReporter", reporterClassName)
        val msg2 = Resources("probarg", argString)
        val msg = msg1 + "\n" + msg2
    
        val iae = new IllegalArgumentException(msg)
        iae.initCause(e)
        throw iae
      }
      case e: IllegalAccessException => {

        val msg1 = Resources("cantInstantiateReporter", reporterClassName)
        val msg2 = Resources("probarg", argString)
        val msg = msg1 + "\n" + msg2
    
        val iae = new IllegalArgumentException(msg)
        iae.initCause(e)
        throw iae
      }
    }
  }

  private[scalatest] def doRunRunRunADoRunRun(
    dispatchReporter: DispatchReporter,
    suitesList: List[String],
    stopper: Stopper,
    includes: Set[String],
    excludes: Set[String],
    propertiesMap: Map[String, String],
    concurrent: Boolean,
    membersOnlyList: List[String],
    wildcardList: List[String],
    testNGList: List[String],
    runpath: List[String],
    loader: ClassLoader,
    doneListener: RunDoneListener,
    runStamp: Int
  ) = {

    // TODO: add more, and to RunnerThread too
    if (dispatchReporter == null)
      throw new NullPointerException
    if (suitesList == null)
      throw new NullPointerException
    if (stopper == null)
      throw new NullPointerException
    if (includes == null)
      throw new NullPointerException
    if (excludes == null)
      throw new NullPointerException
    if (propertiesMap == null)
      throw new NullPointerException
    if (membersOnlyList == null)
      throw new NullPointerException
    if (wildcardList == null)
      throw new NullPointerException
    if (runpath == null)
      throw new NullPointerException
    if (loader == null)
      throw new NullPointerException
    if (doneListener == null)
      throw new NullPointerException

    try {
      val loadProblemsExist =
        try {
          val unassignableList = suitesList.filter(className => !classOf[Suite].isAssignableFrom(loader.loadClass(className)))
          if (!unassignableList.isEmpty) {
            val names = for (className <- unassignableList) yield " " + className
            val report = new Report("org.scalatest.tools.Runner", Resources("nonSuite") + names, None, None, None)
            dispatchReporter.runAborted(report)
            true
          }
          else {
            false
          }
        }
        catch {
          case e: ClassNotFoundException => {
            val report = new Report("org.scalatest.tools.Runner", Resources("cannotLoadSuite", e.getMessage), Some(e), None)
            dispatchReporter.runAborted(report)
            true
          }
        }
  
      if (!loadProblemsExist) {
        try {
          val namedSuiteInstances: List[Suite] =
            for (suiteClassName <- suitesList)
              yield {
                val clazz = loader.loadClass(suiteClassName)
                clazz.newInstance.asInstanceOf[Suite]
              }

          val testNGWrapperSuiteList: List[TestNGWrapperSuite] =
            if (!testNGList.isEmpty)
              List(new TestNGWrapperSuite(testNGList))
            else
              Nil

          val (membersOnlySuiteInstances, wildcardSuiteInstances) = {

            val membersOnlyAndBeginsWithListsAreEmpty = membersOnlyList.isEmpty && wildcardList.isEmpty // They didn't specify any -m's or -w's on the command line


            // TODO: rename the 'BeginsWith' variables to 'Wildcard' to match the terminology that
            // we ended up with on the outside
            // TODO: Should SuiteDiscoverHelper be a singleton object?
            if (membersOnlyAndBeginsWithListsAreEmpty && !suitesList.isEmpty) {
              (Nil, Nil) // No DiscoverySuites in this case. Just run Suites named with -s
            }
            else {
              val accessibleSuites = (new SuiteDiscoveryHelper).discoverSuiteNames(runpath, loader)

              if (membersOnlyAndBeginsWithListsAreEmpty && suitesList.isEmpty) {
                // In this case, they didn't specify any -w, -m, or -s on the command line, so the default
                // is to run any accessible Suites discovered on the runpath
                (Nil, List(new DiscoverySuite("", accessibleSuites, true, loader)))
              }
              else {
                val membersOnlyInstances =
                  for (membersOnlyName <- membersOnlyList)
                    yield new DiscoverySuite(membersOnlyName, accessibleSuites, false, loader)

                val wildcardInstances =
                  for (wildcardName <- wildcardList)
                    yield new DiscoverySuite(wildcardName, accessibleSuites, true, loader)

                (membersOnlyInstances, wildcardInstances)
              }
            }
          }

          val suiteInstances: List[Suite] = namedSuiteInstances ::: membersOnlySuiteInstances ::: wildcardSuiteInstances ::: testNGWrapperSuiteList

          val testCountList =
            for (suite <- suiteInstances)
              yield suite.expectedTestCount(includes, excludes)
  
          def sumInts(list: List[Int]): Int =
            list match {
              case Nil => 0
              case x :: xs => x + sumInts(xs)
            }

          val expectedTestCount = sumInts(testCountList)

          val ordinal = new Ordinal(runStamp)
          // dispatchReporter.runStarting(expectedTestCount) TODO DELETE
          dispatchReporter.apply(RunStarting(ordinal, expectedTestCount))

          if (concurrent) {
            val distributor = new ConcurrentDistributor(dispatchReporter, stopper, includes, excludesWithIgnore(excludes), propertiesMap)
            for (suite <- suiteInstances)
              distributor.put(suite)
            distributor.waitUntilDone()
          }
          else {
            for (suite <- suiteInstances) {
              val suiteRunner = new SuiteRunner(suite, dispatchReporter, stopper, includes, excludesWithIgnore(excludes),
                  propertiesMap, None)
              suiteRunner.run()
            }
          }


          if (stopper.stopRequested)
            dispatchReporter.runStopped()
          else
            dispatchReporter.runCompleted()
        }
        catch {
          case ex: InstantiationException => {
            val report =
                new Report("org.scalatest.tools.Runner", Resources("cannotInstantiateSuite", ex.getMessage), Some(ex), None)
            dispatchReporter.runAborted(report)
          }
          case ex: IllegalAccessException => {
            val report
                = new Report("org.scalatest.tools.Runner", Resources("cannotInstantiateSuite", ex.getMessage), Some(ex), None)
            dispatchReporter.runAborted(report)
          }
          case ex: NoClassDefFoundError => {
            val report = new Report("org.scalatest.tools.Runner", Resources("cannotLoadClass", ex.getMessage), Some(ex), None)
            dispatchReporter.runAborted(report)
          }
          case ex: Throwable => {
            val report = new Report("org.scalatest.tools.Runner", Resources.bigProblems(ex), Some(ex), None)
            dispatchReporter.runAborted(report)
          }
        }
      }
    }
    finally {
      dispatchReporter.dispose()
      doneListener.done()
    }
  }

  private[scalatest] def excludesWithIgnore(excludes: Set[String]) = excludes + "org.scalatest.Ignore"

  private[scalatest] def withClassLoaderAndDispatchReporter(runpathList: List[String], reporterSpecs: ReporterSpecs,
      graphicReporter: Option[Reporter], passFailReporter: Option[Reporter])(f: (ClassLoader, DispatchReporter) => Unit): Unit = {

    val loader: ClassLoader = getRunpathClassLoader(runpathList)
    try {
      Thread.currentThread.setContextClassLoader(loader)
      try {
        val dispatchReporter = getDispatchReporter(reporterSpecs, graphicReporter, passFailReporter, loader)
        try {
          f(loader, dispatchReporter)
        }
        finally {
          dispatchReporter.dispose()
        }
      }
      catch {
        // getDispatchReporter may complete abruptly with an exception, if there is an problem trying to load
        // or instantiate a custom reporter class.
        case ex: Throwable => {
          System.err.println(Resources("bigProblemsMaybeCustomReporter"))
          ex.printStackTrace(System.err)
        }
      }
    }
    finally {
      // eventually call close on the RunpathClassLoader
    }
  }

  private[scalatest] def getRunpathClassLoader(runpathList: List[String]): ClassLoader = {

    if (runpathList == null)
      throw new NullPointerException
    if (runpathList.isEmpty) {
      classOf[Suite].getClassLoader // Could this be null technically?
    }
    else {
      val urlsList: List[URL] =
        for (raw <- runpathList) yield {
          try {
            new URL(raw)
          }
          catch {
            case murle: MalformedURLException => {
  
              // Assume they tried to just pass in a file name
              val file: File = new File(raw)
  
              // file.toURL may throw MalformedURLException too, but for now
              // just let that propagate up.
              file.toURL() // If a dir, comes back terminated by a slash
            }
          }
        }
  
      // Here is where the Jini preferred class loader stuff went.
  
      // Tell the URLConnections to not use caching, so that repeated runs and reruns actually work
      // on the latest binaries.
      for (url <- urlsList) {
        try {
          url.openConnection.setDefaultUseCaches(false)
        }
        catch {
          case e: IOException => // just ignore these
        }
      }
  
      new URLClassLoader(urlsList.toArray, classOf[Suite].getClassLoader)
    }
  }

  private[scalatest] def usingEventDispatchThread(f: => Unit): Unit = {
    SwingUtilities.invokeLater(
      new Runnable() {
        def run() {
          f
        }
      }
    )
  }
}
