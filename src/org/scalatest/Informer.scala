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
 * to that <code>Reporter</code> via an <code>InfoProvided</code> event.
 *
 * <p>
 * The simplest way to use an <code>Informer</code> is to pass a string to its
 * <code>apply</code> method. Given this string, the <code>Informer</code> will
 * construct a <code>Report</code> using the string returned by invoking
 * <code>nameForReport</code>, a method defined on <code>Informer</code>, as the name and
 * the string passed to <code>apply</code> as the <code>message</code>.
 * The <code>Informer</code> will then pass the newly created <code>Report</code>
 * to its wrapped <code>Reporter</code> via an <code>InfoProvided</code> event.
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
 * @author Bill Venners
 */
trait Informer {

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
}
