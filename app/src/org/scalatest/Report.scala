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
 * Class used to send reports to a <code>Reporter</code>.
 * </p>
 *
 * <p>
 * The primary constructor instantiates a new <code>Report</code> with specified name,
 * message, optional throwable, optional rerunnable, thread name, and date. This class can be subclassed to send more information
 * to a reporter.
 * </p>
 *
 * <p>
 * <strong>Extensibility</strong>
 * </p>
 *
 * <p>
 * <code>Report</code> can be subclassed so that custom information can be passed to the subclass's constructor.
 * This information can be passed in when the <code>Report</code> subclass is instantiated, from within
 * test methods, overridden <code>Suite</code> methods, or other code. Instances of <code>Report</code> subclasses
 * can be passed to any <code>Reporter</code>, but any custom information they contain will not be presented to the
 * user except by <code>Reporter</code>s written with knowledge of the <code>Report</code> subclass and its custom
 * information. Thus, when you define a <code>Report</code> subclass that embeds custom information, you would typically
 * also define one or more <code>Reporter</code> classes that present the custom information to the user.
 * </p>
 *
 * @param name the name of the entity about which this report was generated.
 * @param message a <code>String</code> message.
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
 *     <code>name</code>, <code>message</code>, <code>throwable</code>, or <code>rerunnable</code>, <code>threadName</code>, or
 *     <code>date</code> references are <code>null</code>.
 *
 * @author Bill Venners
 */
@serializable
class Report(val name: String, val message: String, val throwable: Option[Throwable], val rerunnable: Option[Rerunnable],
    val threadName: String, val date: Date) {

  if (name == null)
    throw new NullPointerException("name was null")

  if (message == null)
    throw new NullPointerException("message was null")

  if (threadName == null)
    throw new NullPointerException("thread was null")
  
  if (date == null)
    throw new NullPointerException("date was null")
  
  if (throwable == null)
    throw new NullPointerException("throwable was null")
  
  if (rerunnable == null)
    throw new NullPointerException("rerunnable was null")

  /**
   * Constructs a new <code>Report</code> with specified name
   * and message.
   *
   * @param name the name of the entity about which this report was generated.
   * @param message a <code>String</code> message.
   *
   * @throws NullPointerException if either of the specified <code>name</code>
   *     or <code>message</code> parameters are <code>null</code>.
   */
  def this(name: String, message: String) = this(name, message,
      None, None, Thread.currentThread.getName, new Date)

// def this(name: String, message: String, rerunnable: Option[Rerunnable]) = this(name,
//    message, None, rerunnable, Thread.currentThread.getName, new Date)
// [bv: this will trip people up. Option's type is erased, so overloading this way didn't work. So
// may want to mention this somewhere]
// I realized that it would be dumb to use this form if you didn't have a rerunable, so it shouldn't be an Option anyway.
// It still may be worth mentioning, because this error will probably happen to every newbie at some point.

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
   *     <code>name</code>, <code>message</code>, <code>throwable</code>,
   *     or <code>rerunnable</code> parameters are <code>null</code>.
   */
  def this(name: String, message: String, throwable: Option[Throwable], rerunnable: Option[Rerunnable])  = this(name,
      message, throwable, rerunnable, Thread.currentThread.getName, new Date)
}
/*
This was an interesting excercise to decide whether to provide overloaded constructors
for say:

1. String, String, Option[Throwable]

2. String, String, Throwable, Rerunnable

3. String String, Throwable

4. String, String, Rerunable

Decided to make people say None at the end, so no 1. Decided it was confusing to have throwable be
both an Option[Throwable] and a Throwable. So didn't do the others. Maybe convenient, but confusing
and i realized sometimes I had an Option[Rerunnable] variable already, so I really did want to pass
that in without checking it with a match. (for a while I thought I would only have 2, 3, and 4.)
*/
