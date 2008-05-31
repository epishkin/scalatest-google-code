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
 * Trait to which custom information about a running suite of tests can be reported.
 * <code>Informer</code> contains two <code>apply</code> methods, one which takes
 * a string and the other a <code>Report</code>. An <code>Informer</code> is essentially
 * used to wrap a <code>Reporter</code> and provide easy ways to send custom information
 * to that <code>Reporter</code> via its <code>infoProvided</code> method.
 *
 * <p>
 * The simplest way to use an <code>Informer</code> is to pass a string to its
 * <code>apply</code> method. Given this string, the <code>Informer</code> will
 * construct a <code>Report</code> using the string returned by invoking
 * <code>nameForReport</code>, a method defined on <code>Informer</code>, as the name and
 * the string passed to <code>apply</code> as the <code>message</code>.
 * The <code>Informer</code> will then pass the newly created <code>Report</code>
 * to its wrapped <code>Reporter</code> via the <code>infoProvided</code> method.
 * Here's an example of using an <code>Informer</code> in a <code>Suite</code>
 * subclass:
 * </p>
 * 
 * <pre>
 * import org.scalatest._
 * 
 * class MySuite extends Suite {
 *   def testAddition(info: Informer) {
 *     assert(1 + 1 === 2)
 *     info("Addition seems to work")
 *   }
 * }
 * </pre>
 *
 * If you run this <code>Suite</code> from the interpreter, you will see the message
 * included in the printed report:
 *
 * <pre>
 * scala> (new MySuite).execute()
 * Test Starting - MySuite.testAddition(Reporter)
 * Info Provided - MySuite.testAddition(Reporter): Addition seems to work
 * Test Succeeded - MySuite.testAddition(Reporter)
 * </pre>
 * 
 * Alternatively, you could create a <code>Report</code> yourself and pass it to
 * <code>apply</code>. You can invoke <code>nameForReport</code> on the
 * <code>Informer</code> to get a user-friendly name to pass to the constructor of
 * the <code>Report</code> you create.
 * The <code>Informer</code> will then forward the passed <code>Report</code>
 * to its wrapped <code>Reporter</code> via the <code>infoProvided</code> method.
 * Here's an example of passing your own <code>Report</code> to an <code>Informer</code>
 * in a <code>Suite</code> subclass:
 * </p>
 * 
 * <pre>
 * import org.scalatest._
 *
 * class MySuite extends Suite {
 *   def testAddition(info: Informer) {
 *     assert(1 + 1 === 2)
 *     val myReport =
 *       new Report(info.nameForReport, "Here's a stack trace", Some(new Exception), None)
 *     info(myReport)
 *   }
 * }
 * </pre>
 *
 * If you run this <code>Suite</code> from the interpreter, you will see the message
 * included in the printed report:
 *
 * <pre>
 * scala> (new MySuite).execute()
 * Test Starting - MySuite.testAddition(Informer)
 * Info Provided - MySuite.testAddition(Informer): Here's a stack trace
 * java.lang.Exception
 *   at line3$object$$iw$$iw$$iw$MySuite.testAddition(<console>:10)
 *   at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
 *   at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)
 *   at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
 *   at java.lang.reflect.Method.invoke(Method.java:585)
 *   at org.scalatest.Suite$class.runTest(Suite.scala:1085)
 *   at line3$object$$iw$$iw$$iw$MySuite.runTest(<console>:6)
 *   ...
 *   at scala.tools.nsc.MainGenericRunner.main(MainGenericRunner.scala)
 * Test Succeeded - MySuite.testAddition(Informer)
 * </pre>
 *
 * <p>
 * Besides sending a stack trace, you might want to create and pass your own
 * <code>Report</code> if you've defined one or more <code>Report</code> subclasses that
 * are recognized and handled specially by a <code>Reporter</code> subclass you've defined.
 * See the "Extensibility" section in the documentation for <code>Report</code> for more 
 * information.
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
   * wrapped <code>Reporter</code>'s <code>infoProvided</code> method. This method
   * will create a <code>Report</code> via <code>Report</code>'s auxiliary constructor that takes a
   * string name and message, using the string returned by invoking
   * <code>nameForReport</code> as the name and the passed string as the message, and pass
   * the newly created <code>Report</code> to the wrapped <code>Reporter</code>'s
   * <code>infoProvided</code> method.
   *
   * @param report a <code>Report</code> that encapsulates the event to report.
   *
   * @throws NullPointerException if <code>message</code> reference is <code>null</code>
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
   */
  def nameForReport: String
}
