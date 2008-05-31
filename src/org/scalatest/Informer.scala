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
trait Informer {

  /**
   * Provide information in the form of a <code>Report</code> to be reported to the
   * wrapped <code>Reporter</code>'s <code>infoProvided</code> method.
   *
   * @param report a <code>Report</code> that encapsulates the event to report.
   *
   * @throws NullPointerException if <code>report</code> reference is <code>null</code>
   */
  def apply(report: Report): Unit

  /**
   * Provide information in the form of a string message to be reported to the
   * wrapped <code>Reporter</code>'s <code>infoProvided</code> method.
   *
   * @param report a <code>Report</code> that encapsulates the event to report.
   *
   * @throws NullPointerException if <code>report</code> reference is <code>null</code>
   */
  def apply(message: String): Unit

  /**
   * A name suitable for passing to the constructor of a <code>Report</code>
   * when using the <code>apply</code> method that takes a <code>Report</code>.
   * For example, in an <code>Informer</code> passed to a test method in
   * trait <code>Suite</code>, this method will return from this method a
   * user-friendly name for the test (the same name used for the test by <code>Suite</code>
   * when making <code>testStarting</code>, <code>testSucceeded</code>, etc., reports).
   *
   * @return a name suitable for passing to a <code>Report</code> constructor.
   *
   * @throws NullPointerException if <code>report</code> reference is <code>null</code>
   */
  def nameForReport: String
}
