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

import org.scalatest.events.Ordinal

/**
 * Instances of this class are not thread safe. It should be used by only one thread. Anytime a new
 * thread will be involved in sending events, a new Tracker should be obtained by invoking <code>nextTracker()</code>.
 * 
 * <p>
 * The reason Tracker is not immutable is that methods would have to pass back, and that's hard because exceptions can
 * also be thrown. So this mutable object is how methods invoked "returns" updates to the current ordinal whether those
 * methods return normally or complete abruptly with an exception. Also, sometimes with closures capturing free variables,
 * those free variables may want to grab an ordinal in the context of a callee even after the callee has already called
 * some other method. So in other words the calling method may need to know the "current ordinal" even before the method
 * it calls has completed in any manner, i.e., while it is running. (The example is the info stuff in FunSuite, which sets
 * up an info that's useful during a run, then calls super.run(...).
 * </p>
 */
class Tracker(firstOrdinal: Ordinal) {

  private var currentOrdinal = firstOrdinal

  /**
   * Constructs a new <code>Tracker</code> with a new <code>Ordinal</code> initialized with a run stamp of 0.
   */
  def this() = this(new Ordinal(0))

  def nextOrdinal(): Ordinal = {
    val ordinalToReturn = currentOrdinal
    currentOrdinal = currentOrdinal.next
    ordinalToReturn
  }

  def nextTracker(): Tracker = {
    val (nextForNewThread, nextForThisThread) = currentOrdinal.nextNewOldPair
    currentOrdinal = nextForThisThread
    new Tracker(nextForNewThread)
  }
}
