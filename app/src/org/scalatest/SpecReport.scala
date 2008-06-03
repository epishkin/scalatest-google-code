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

@serializable
private[scalatest] class SpecReport(
  override val name: String,
  val shortName: String,
  val level: Int,
  override val message: String,
  override val throwable: Option[Throwable],
  override val rerunnable: Option[Rerunnable],
  override val threadName: String,
  override val date: Date
) extends Report(name, message, throwable, rerunnable, threadName, date) {

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
  def this(name: String, shortName: String, level: Int, message: String) = this(name, shortName, level, message,
      None, None, Thread.currentThread.getName, new Date)

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
  def this(name: String, shortName: String, level: Int, message: String, throwable: Option[Throwable], rerunnable: Option[Rerunnable])  = this(name,
      shortName, level, message, throwable, rerunnable, Thread.currentThread.getName, new Date)
}
