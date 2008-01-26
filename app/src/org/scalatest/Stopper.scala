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
 * Trait whose instances can indicate whether a stop has been requested. This is passed in
 * to the <code>execute</code> method of <code>Suite</code>, so that running suites of tests can be
 * requested to stop early.
 *
 * @author Bill Venners
 */
trait Stopper {

  /**
   * Indicates whether a stop has been requested.  Call this method
   * to determine whether a running test should stop. The <code>execute</code> method of any <code>Suite</code>, or
   * code invoked by <code>execute</code>, should periodically check the
   * <code>stopRequested</code> property. If <code>true</code>,
   * the <code>execute</code> method should interrupt its work and simply return.
   */
  def stopRequested = false
}
// One question people will have is do I make this a val or a def in the supertype.
// A val can override a def. Can it be the other way around? How does he implement
// this?
