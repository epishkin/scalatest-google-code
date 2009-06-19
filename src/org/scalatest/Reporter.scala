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

import org.scalatest.events._

/**
 * Trait whose instances collect the results of a running
 * suite of tests and presents those results in some way to the user. Instances of this trait can
 * be called "report functions" or "reporters."
 *
 * <p>
 * Reporters receive test results via thirteen events.
 * Each event is fired to pass a particular kind of information to
 * the reporter. The events are:
 * </p>
 *
 * <ul>
 * <li><code>RunStarting</code>
 * <li><code>TestStarting</code>
 * <li><code>TestSucceeded</code>
 * <li><code>TestFailed</code>
 * <li><code>TestIgnored</code>
 * <li><code>TestPending</code>
 * <li><code>SuiteStarting</code>
 * <li><code>SuiteCompleted</code>
 * <li><code>SuiteAborted</code>
 * <li><code>InfoProvided</code>
 * <li><code>RunStopped</code>
 * <li><code>RunAborted</code>
 * <li><code>RunCompleted</code>
 * </ul>
 *
 * <p>
 * Reporters may be implemented such that they only present some of the reported events to the user. For example, you could
 * define a reporter class that doesn nothing in response to <code>SuiteStarting</code> events.
 * Such a class would always ignore <code>SuiteStarting</code> events.
 * </p>
 *
 * <p>
 * The term <em>test</em> as used in the <code>TestStarting</code>, <code>TestSucceeded</code>,
 * and <code>TestFailed</code> event names
 * is defined abstractly to enable a wide range of test implementations.
 * Trait <code>Suite</code> fires <code>TestStarting</code> to indicate it is about to invoke one
 * of its test methods, <code>TestSucceeded</code> to indicate a test method returned normally,
 * and <code>TestFailed</code> to indicate a test method completed abruptly with an exception.
 * Although the execution of a <code>Suite</code>'s test methods will likely be a common event
 * reported via the
 * <code>TestStarting</code>, <code>TestSucceeded</code>, and <code>TestFailed</code> methods, because
 * of the abstract definition of &#8220;test&#8221; used by the
 * the event classes, these events are not limited to this use. Information about any conceptual test
 * may be reported via the <code>TestStarting</code>, <code>TestSucceeded</code>, and
 * <code>TestFailed</code> events.
 *
 * <p>
 * Likewise, the term <em>suite</em> as used in the <code>SuiteStarting</code>, <code>SuiteAborted</code>,
 * and <code>SuiteCompleted</code> event names
 * is defined abstractly to enable a wide range of suite implementations.
 * Object <code>Runner</code> fires <code>SuiteStarting</code> to indicate it is about to invoke
 * <code>run</code> on a
 * <code>Suite</code>, <code>SuiteCompleted</code> to indicate a <code>Suite</code>'s
 * <code>run</code> method returned normally,
 * and <code>SuiteAborted</code> to indicate a <code>Suite</code>'s <code>run</code>
 * method completed abruptly with an exception.
 * Similarly, class <code>Suite</code> fires <code>SuiteStarting</code> to indicate it is about to invoke
 * <code>run</code> on a
 * nested <code>Suite</code>, <code>SuiteCompleted</code> to indicate a nested <code>Suite</code>'s
 * <code>run</code> method returned normally,
 * and <code>SuiteAborted</code> to indicate a nested <code>Suite</code>'s <code>run</code>
 * method completed abruptly with an exception.
 * Although the execution of a <code>Suite</code>'s <code>run</code> method will likely be a
 * common event reported via the
 * <code>SuiteStarting</code>, <code>SuiteAborted</code>, and <code>SuiteCompleted</code> events, because
 * of the abstract definition of "suite" used by the
 * event classes, these events are not limited to this use. Information about any conceptual suite
 * may be reported via the <code>SuiteStarting</code>, <code>SuiteAborted</code>, and
 * <code>SuiteCompleted</code> events.
 *
 * <p>
 * <strong>Extensibility</strong>
 * </p>
 *
 * <p>
 * You can create classes that extend <code>ReportFunction</code> to report test results in custom ways, and to
 * report custom information passed as an event "payload." For more information on the latter
 * use case, see the <em>Extensibility</em> section of the <a href="Event.html"><code>Event</code> documentation</a>.
 * </p>
 *
 * <p>
 * Reporter classes can handle events in any manner, including doing nothing.
 * For convenience, trait <code>ReporterFunction</code> includes a default implentation of <code>apply</code> that does nothing.
 * </p>
 *
 * @author Bill Venners
 */
trait Reporter extends (Event => Unit) {

    /**
     * Release any non-memory finite resources, such as file handles, held by this <code>Reporter</code>. Clients should
     * call this method when they no longer need the <code>Reporter</code>, before releasing the last reference
     * to the <code>Reporter</code>. After this method is invoked, the <code>Reporter</code> may be defunct,
     * and therefore not usable anymore. If the <code>Reporter</code> holds no resources, it may do nothing when
     * this method is invoked.
     */
    def dispose() = ()

  /**
   * Invoked to report an event that subclasses may wish to report in some way to the user.
   *
   * @param event the event being reported
   */
  def apply(event: Event)
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
/*
Todo: Make a ResourcefulReporter, a subclass of Reporter, that has the dispose method. Deprecate dispose in Reporter.
Make FileReporter a ResourcefulReporter. Change my code that calls dispose to do a pattern match on the type. Ask anyone
who has written a dispose method to make their reporter Resourceful. After 2 releases drop dispose() from Reporter.
*/

  /*
      case RunStarting(ordinal, testCount, formatter, payload, threadName, timeStamp) => runStarting(testCount)

      case TestStarting(ordinal, suiteName, suiteClassName, testName, formatter, rerunnable, payload, threadName, timeStamp) =>

      case TestSucceeded(ordinal, suiteName, suiteClassName, testName, duration, formatter, rerunnable, payload, threadName, timeStamp) => 

      case TestFailed(ordinal, message, suiteName, suiteClassName, testName, throwable, duration, formatter, rerunnable, payload, threadName, timeStamp) => 

      case TestIgnored(ordinal, suiteName, suiteClassName, testName, formatter, payload, threadName, timeStamp) => 

      case TestPending(ordinal, suiteName, suiteClassName, testName, formatter, payload, threadName, timeStamp) => 

      case SuiteStarting(ordinal, suiteName, suiteClassName, formatter, rerunnable, payload, threadName, timeStamp) =>

      case SuiteCompleted(ordinal, suiteName, suiteClassName, duration, formatter, rerunnable, payload, threadName, timeStamp) => 

      case SuiteAborted(ordinal, message, suiteName, suiteClassName, throwable, duration, formatter, rerunnable, payload, threadName, timeStamp) => 

      case InfoProvided(ordinal, message, nameInfo, throwable, formatter, payload, threadName, timeStamp) => {

      case RunStopped(ordinal, duration, summary, formatter, payload, threadName, timeStamp) => runStopped()

      case RunAborted(ordinal, message, throwable, duration, summary, formatter, payload, threadName, timeStamp) => 

      case RunCompleted(ordinal, duration, summary, formatter, payload, threadName, timeStamp) => runCompleted()
    }
  }
*/
