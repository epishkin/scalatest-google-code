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

import java.util.Date

/**
 * <p>
 * Class used to send specification-style reports to a <code>Reporter</code>.
 * </p>
 *
 * <p>
 * The primary constructor instantiates a new <code>Report</code> with specified name,
 * message, specification text, formatted specification text, flag indicating whether to include the report in 
 * a specification-style output, optional throwable, optional rerunnable, thread name, and date. This class can be subclassed to send more information
 * to a reporter.
 * </p>
 *
 * <p>
 * The difference between <code>message</code>, <code>plainSpecText</code>, and <code>formattedSpecText</code> is that <code>message</code> is
 * formatted as a "normal" test report, <code>plainSpecText</code> is formatted for specification-style output, and
 * <code>formattedSpecText</code> is formatted for specification-style output
 * to the console. For example, if the <code>message</code> looks like this:
 * </p>
 *
 * <p>
 * <code>StackSpec - A Stack (with one item less than capacity) should not be full</code>
 * </p>
 *
 * <p>
 * The <code>plainSpecText</code> might look like:
 * </p>
 *
 * <p>
 * <code>A Stack (with one item less than capacity) should not be full</code>
 * </p>
 *
 * <p>
 * And the <code>formattedSpecText</code> might look like:
 * </p>
 *
 * <p>
 * <code>- A Stack (with one item less than capacity) should not be full</code>
 * </p>
 *
 * <p>
 * The <code>includeInSpecOutput</code> <code>Boolean</code> flag is used to omit reports that
 * would detract from the readability of the specification-style output. For example, trait <code>Spec</code>
 * sets <code>includeInSpecOutput</code> to <code>false</code> for <code>testStarting</code> reports, but to
 * <code>true</code> for <code>testSucceeded</code> and <code>testFailed</code> reports. This produces reports
 * that read more like specifications, for example:
 * </p>
 *
 * <pre>
 * A Stack (when not empty)
 * - should allow me to pop
 * - should be invoked
 * A Stack (when not full)
 * - should allow me to push
 * </pre>
 *
 * <p>
 * <strong>Extensibility</strong>
 * </p>
 *
 * <p>
 * <code>SpecReport</code>, like its superclass <code>Report</code>, can be subclassed so that custom information can
 * be passed to the subclass's constructor.
 * This information can be passed in when the <code>SpecReport</code> subclass is instantiated, from within
 * test methods, overridden <code>Suite</code> methods, or other code. Instances of <code>SpecReport</code> subclasses
 * can be passed to any <code>Reporter</code>, but any custom information they contain will not be presented to the
 * user except by <code>Reporter</code>s written with knowledge of the <code>SpecReport</code> subclass and its custom
 * information. Thus, when you define a <code>SpecReport</code> subclass that embeds custom information, you would typically
 * also define one or more <code>Reporter</code> classes that present the custom information to the user.
 * </p>
 *
 * @param name the name of the entity about which this report was generated.
 * @param message a <code>String</code> message.
 * @param plainSpecText the plain specification text <code>String</code>, with no formatting.
 * @param formattedSpecText the specification text <code>String</code> formatted for printing to the console.
 * @param includeInSpecOutput a <code>Boolean</code> flag that indicates whether this <code>SpecReport</code> should be
 *   included in a specification-style output.
 * @param throwable the <code>Throwable</code> that indicated the problem, or a <code>Throwable</code> created
 *     to capture stack trace information about the problem, or <code>None</code>. If <code>None</code> is passed, the problem
 *     is reported without describing a <code>Throwable</code>.
 * @param rerunnable a <code>Rerunnable</code> that can be used to rerun a test or other entity (such as a suite),
 *     or <code>None</code>. If <code>None</code> is passed, the test or other entity can not be rerun.
 * @param threadName a name for the <code>Thread</code> about whose activity this report was generated.
 * @param date a relevant <code>Date</code>. For example, the a <code>Date</code>
 *     indicating the time this <code>Report</code> was generated, or a <code>Date</code>
 *      indicating the time the event reported by this <code>Report</code> occurred.
 *
 * @throws NullPointerException if any of the specified
 *     <code>name</code>, <code>message</code>, <code>plainSpecText</code>, <code>formattedSpecText</code>, <code>throwable</code>,
 *     <code>rerunnable</code>, <code>threadName</code>, or <code>date</code> references are <code>null</code>.
 *
 * @author Bill Venners
 */
@serializable
class SpecReport(
  override val name: String,
  override val message: String,
  val plainSpecText: String,
  val formattedSpecText: String,
  val includeInSpecOutput: Boolean,
  override val throwable: Option[Throwable],
  override val rerunnable: Option[Rerunnable],
  override val threadName: String,
  override val date: Date
) extends Report(name, message, throwable, rerunnable, threadName, date) {

  if (plainSpecText == null)
    throw new NullPointerException("plainSpecText was null")
  if (formattedSpecText == null)
    throw new NullPointerException("formattedSpecText was null")


   /**
   * Constructs a new <code>Report</code> with specified name
   * and message.
   *
   * @param name the name of the entity about which this report was generated.
   * @param message a <code>String</code> message.
   *
   * @throws NullPointerException if any of the specified <code>name</code>,
   *     <code>message</code>, <code>plainSpecText</code>, or <code>formattedSpecText</code> parameters are <code>null</code>.
   */
  def this(name: String, message: String, plainSpecText: String, formattedSpecText: String, includeInSpecOutput: Boolean) =
    this(name, message, plainSpecText, formattedSpecText, includeInSpecOutput, None, None, Thread.currentThread.getName, new Date)

    /**
   * Constructs a new <code>Report</code> with specified name,
   * message, optional throwable, and optional rerunnable.
   *
   * @param name the name of the entity about which this report was generated.
   * @param message a <code>String</code> message.
   * @param throwable a relevant <code>Throwable</code>, or <code>None</code>. For example, this
   *     <code>Throwable</code> may have indicated a problem being reported by this
   *     <code>Report</code>, or it may have been created to provide stack trace
   *     information in the <code>Report</code>.
   * @param rerunnable a <code>Rerunnable</code> that can be used to rerun a test or other entity, or <code>None</code>.
   *
   * @throws NullPointerException if any of the specified 
   *     <code>name</code>, <code>message</code>, , <code>plainSpecText</code>, <code>formattedSpecText</code>, <code>throwable</code>,
   *     or <code>rerunnable</code> parameters are <code>null</code>.
   */
  def this(name: String, message: String, plainSpecText: String, formattedSpecText: String, includeInSpecOutput: Boolean, throwable: Option[Throwable], rerunnable: Option[Rerunnable])  = this(name,
      message, plainSpecText, formattedSpecText, includeInSpecOutput, throwable, rerunnable, Thread.currentThread.getName, new Date)
}
