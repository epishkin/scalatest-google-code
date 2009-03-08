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
 * Trait whose instances collect the results of a running
 * suite of tests and presents those results in some way to the user.
 *
 * <p>
 * <code>Reporter</code>s receive test results via twelve
 * <em>report methods</em>. Each report method is invoked to pass a particular kind of report to
 * the reporter. The report methods are:
 * </p>
 *
 * <ul>
 * <li><code>runStarting</code>
 * <li><code>testStarting</code>
 * <li><code>testSucceeded</code>
 * <li><code>testFailed</code>
 * <li><code>testIgnored</code>
 * <li><code>suiteStarting</code>
 * <li><code>suiteCompleted</code>
 * <li><code>suiteAborted</code>
 * <li><code>infoProvided</code>
 * <li><code>runStopped</code>
 * <li><code>runAborted</code>
 * <li><code>runCompleted</code>
 * </ul>
 *
 * <p>
 * <code>Reporter</code>s may be implemented such that they only present some of the reports to the user. For example, you could
 * define a reporter class whose <code>suiteStarting</code> method
 * does nothing. Such a class would always ignore <code>suiteStarting</code> reports.
 * </p>
 *
 * <p>
 * The term <em>test</em> as used in the <code>testStarting</code>, <code>testSucceeded</code>,
 * and <code>testFailed</code> method names
 * is defined abstractly to enable a wide range of test implementations.
 * Trait <code>Suite</code> invokes <code>testStarting</code> to indicate it is about to invoke one
 * of its test methods, <code>testSucceeded</code> to indicate a test method returned normally,
 * and <code>testFailed</code> to indicate a test method completed abruptly with an exception.
 * Although the execution of a <code>Suite</code>'s test methods will likely be a common event
 * reported via the
 * <code>testStarting</code>, <code>testSucceeded</code>, and <code>testFailed</code> methods, because
 * of the abstract definition of &#8220;test&#8221; used by this
 * interface, these methods are not limited to this use. Information about any conceptual test
 * may be reported via the <code>testStarting</code>, <code>testSucceeded</code>, and
 * <code>testFailed</code> methods.
 *
 * <p>
 * Likewise, the term <em>suite</em> as used in the <code>suiteStarting</code>, <code>suiteAborted</code>,
 * and <code>suiteCompleted</code> method names
 * is defined abstractly to enable a wide range of suite implementations.
 * Object <code>Runner</code> invokes <code>suiteStarting</code> to indicate it is about to invoke
 * <code>execute</code> on a
 * <code>Suite</code>, <code>suiteCompleted</code> to indicate a <code>Suite</code>'s
 * <code>execute</code> method returned normally,
 * and <code>suiteAborted</code> to indicate a <code>Suite</code>'s <code>execute</code>
 * method completed abruptly with an exception.
 * Similarly, class <code>Suite</code> invokes <code>suiteStarting</code> to indicate it is about to invoke
 * <code>execute</code> on a
 * nested <code>Suite</code>, <code>suiteCompleted</code> to indicate a nested <code>Suite</code>'s
 * <code>execute</code> method returned normally,
 * and <code>suiteAborted</code> to indicate a nested <code>Suite</code>'s <code>execute</code>
 * method completed abruptly with an exception.
 * Although the execution of a <code>Suite</code>'s <code>execute</code> method will likely be a
 * common event reported via the
 * <code>suiteStarting</code>, <code>suiteAborted</code>, and <code>suiteCompleted</code> methods, because
 * of the abstract definition of "suite" used by this
 * interface, these methods are not limited to this use. Information about any conceptual suite
 * may be reported via the <code>suiteStarting</code>, <code>suiteAborted</code>, and
 * <code>suiteCompleted</code> methods.
 *
 * <p>
 * <strong>Extensibility</strong>
 * </p>
 *
 * <p>
 * You can create classes that extend <code>Reporter</code> to report test results in custom ways, and to
 * report custom information passed to <code>Report</code> subclass instances. For more information on the latter
 * use case, see the <em>Extensibility</em> section of the <a href="Report.html"><code>Report</code> documentation</a>.
 * </p>
 *
 * <p>
 * <code>Reporter</code> classes can handle invocations of its report methods in any manner, including doing nothing.
 * For convenience, trait <code>Reporter</code> includes a default implentation of each report method that does nothing.
 * </p>
 *
 * @author Bill Venners
 */
trait Reporter {

    /**
     * Indicates a runner is about run a suite of tests.
     *
     * <p>
     * Object <code>Runner</code> invokes <code>runStarting</code> to report
     * that the first <code>execute</code> method of a run's starting <code>Suite</code>s
     * is about to be invoked.
     *
     * @param testCount the number of tests expected during this run
     *
     * @throws IllegalArgumentException if <code>expectedTestsCount</code> is less than zero.
     */
    def runStarting(testCount: Int) = ()

    /**
     * Indicates a suite (or other entity) is about to start a test.
     *
     * <p>
     * Trait <code>Suite</code> uses <code>testStarting</code> to report
     * that a test method of a <code>Suite</code> is about to be invoked.
     *
     * @param report a <code>Report</code> that encapsulates the test starting event to report.
     *
     * @throws NullPointerException if <code>report</code> reference is <code>null</code>
     */
    def testStarting(report: Report) = ()

    /**
     * Indicates a suite (or other entity) has completed running a test that succeeded.
     *
     * <p>
     * Trait <code>Suite</code> uses <code>testSucceeded</code> to report
     * that a test method of a <code>Suite</code> returned normally
     * (without throwing an <code>Exception</code>).
     *
     * @param report a <code>Report</code> that encapsulates the test succeeded event to report.
     * @throws NullPointerException if <code>report</code> reference is <code>null</code>
     */
    def testSucceeded(report: Report) = ()
    
    /**
     * Indicates a suite (or other entity) is annotated as a ignore test.
     * 
     * <p>
     * Trait <code>Suite</code> uses <code>testIgnored</code> to report 
     * that a test method of a <code>Suite</code> is annotated as @Ignore.
     * Ignored test will not be run, but will be reported as reminder to fix the broken test.
     * 
     * @param report a <code>Report</code> that encapsulates the ignored test event to report.
     * @throws NullPointerException if <code>report</code> reference is <code>null</code>
     */
    def testIgnored(report: Report) = ()

    /**
     * Indicates a suite (or other entity) has completed running a test that failed.
     *
     * <p>
     * Trait <code>Suite</code> uses <code>testFailed</code> to report
     * that a test method of a <code>Suite</code>
     * completed abruptly with an <code>Exception</code>.
     *
     * @param report a <code>Report</code> that encapsulates the test failed event to report.
     * @throws NullPointerException if <code>report</code> reference is <code>null</code>
     */
    def testFailed(report: Report) = ()

    /**
     * Indicates a suite of tests is about to start executing.
     *
     * <p>
     * Trait <code>Suite</code> and Object <code>Runner</code> use <code>suiteStarting</code> to report
     * that the <code>execute</code> method of a <code>Suite</code>
     * is about to be invoked.
     *
     * @param report a <code>Report</code> that encapsulates the suite starting event to report.
     *
     * @throws NullPointerException if <code>report</code> reference is <code>null</code>
     */
    def suiteStarting(report: Report) = ()

    /**
     * Indicates a suite of tests has completed executing.
     *
     * <p>
     * Trait <code>Suite</code> and Object <code>Runner</code> use <code>suiteCompleted</code> to report
     * that the <code>execute</code> method of a <code>Suite</code>
     * has returned normally (without throwing a <code>RuntimeException</code>).
     *
     * @param report a <code>Report</code> that encapsulates the suite completed event to report.
     * @throws NullPointerException if <code>report</code> reference is <code>null</code>
     */
    def suiteCompleted(report: Report) = ()

    /**
     * Indicates the execution of a suite of tests has aborted, likely because of an error, prior
     * to completion.
     *
     * <p>
     * Trait <code>Suite</code> and Object <code>Runner</code> use <code>suiteAborted</code> to report
     * that the <code>execute</code> method of a <code>Suite</code>
     * has completed abruptly with a <code>RuntimeException</code>.
     *
     * @param report a <code>Report</code> that encapsulates the suite aborted event to report.
     * @throws NullPointerException if <code>report</code> reference is <code>null</code>
     */
    def suiteAborted(report: Report) = ()

    /**
     * Provides information that is not appropriate to report via any other
     * <code>Reporter</code> method.
     *
     * @param report a <code>Report</code> that encapsulates the event to report.
     *
     * @throws NullPointerException if <code>report</code> reference is <code>null</code>
     */
    def infoProvided(report: Report) = ()

    /**
     * Indicates a runner has stopped running a suite of tests prior to completion, likely
     * because of a stop request.
     *
     * <p>
     * <code>Suite</code>'s <code>execute</code> method takes a <code>Stopper</code>, whose <code>stopRequested</code>
     * method indicates a stop was requested. If <code>true</code> is returned by
     * <code>stopRequested</code> while a suite of tests is running, the
     * <code>execute</code> method should promptly
     * return even if that suite hasn't finished running all of its tests.
     * </p>
     *
     * <p>If a stop was requested via the <code>Stopper</code>.
     * <code>Runner</code> will invoke <code>runStopped</code>
     * when the <code>execute</code> method of the run's starting <code>Suite</code> returns.
     * If a stop is not requested, class <code>Runner</code> will invoke <code>runCompleted</code>
     * when the last <code>execute</code> method of the run's starting <code>Suite</code>s returns.
     * </p>
     */
    def runStopped() = ()

    /**
     * Indicates a runner encountered an error while attempting to run a suite of tests.
     *
     * <P>
     * Object <code>Runner</code> invokes <code>runAborted</code> if the
     * <code>execute</code> method of any of the run's starting <code>Suite</code>s completes
     * abruptly with a <code>Throwable</code>.
     *
     * @param report a <code>Report</code> that encapsulates the run aborted event to report.
     * @throws NullPointerException if <code>report</code> reference is <code>null</code>
     */

    def runAborted(report: Report) = ()

    /**
     * Indicates a runner has completed running a suite of tests.
     *
     * <p>
     * <code>Suite</code>'s <code>execute</code> method takes a <code>Stopper</code>, whose <code>stopRequested</code>
     * method indicates a stop was requested. If <code>true</code> is returned by
     * <code>stopRequested</code> while a suite of tests is running, the
     * <code>execute</code> method should promptly
     * return even if that suite hasn't finished running all of its tests.
     * </p>
     *
     * <p>If a stop was requested via the <code>Stopper</code>.
     * <code>Runner</code> will invoke <code>runStopped</code>
     * when the <code>execute</code> method of the run's starting <code>Suite</code> returns.
     * If a stop is not requested, <code>Runner</code> will invoke <code>runCompleted</code>
     * when the last <code>execute</code> method of the run's starting <code>Suite</code>s returns.
     * </p>
     */
    def runCompleted() = ()

    /**
     * Release any non-memory finite resources, such as file handles, held by this <code>Reporter</code>. Clients should
     * call this method when they no longer need the <code>Reporter</code>, before releasing the last reference
     * to the <code>Reporter</code>. After this method is invoked, the <code>Reporter</code> may be defunct,
     * and therefore not usable anymore. If the <code>Reporter</code> holds no resources, it may do nothing when
     * this method is invoked.
     */
    def dispose() = ()
}
/*
So I remember, this is why I decided not to make case class subclasses of
Report, and then have Reporter just have a submit(report: Report) method.
I considered doing that, because it is more Scala like and I thought it might
make implementing the Reporter interface easier. One thing I thought about was
LavaLampReporter, which turns the red lava lamp on if there's a failure, else it
turns the green one on. It would be a pain for such an implementer to have to
implement empty methods for all the other methods in Reporter. But then I realized
that I could put empty definitions, = (), in the Reporter trait itself. I'm about
to do that. Also, the tradeoff is that suddenly the API would have a lot more surface
area visible in all those case classes. I was struggling whether to call them
InfoProvided and RunStarting or InfoProvidedReport and RunStartingReport, etc. I
felt it was a tradeoff between making implementation of Reporter easier, which is
someting people rarely want to do, versus making the surface area of the API smaller,
which is something everyone who uses ScalaTest will look at.

The other thing is that I was thinking you could just fire a new Report subclass
into Reporter, and that would be easier than doing instanceof and downcasting, but
then I realized that wasn't using the type system to prevent errors. If I fired a
NewFangledReport into a plain old StandardOutReporter, it would choke on it. So I
need to check anyway, so I may as well model this Reporter extensions the old way,
but subtyping Reporter and adding a method. But now we could use pattern matching:

reporter match {
  case nfr: NewFangledReporter => nfr.newFangledHappened(report)
  case r: _ => r.infoProvided(report)
}

I don't know the pattern matching syntax very well yet. But something like that.
*/
