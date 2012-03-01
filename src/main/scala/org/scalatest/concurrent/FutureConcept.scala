/*
 * Copyright 2001-2012 Artima, Inc.
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
package org.scalatest.concurrent

/**
 * Concept trait for futures, instances of which are passed to the <code>whenReady</code>
 * methods of trait <code>WhenReady</code>.
 *
 * @author Bill Venners
 */
trait FutureConcept[T] {

  /**
   * Queries this future for its value.
   * 
   * <p>
   * If the future is not ready, this method will return <code>None</code>. If ready, it will either return an exception
   * or a <code>T</code>.
   * </p>
   */
  def value: Option[Either[Throwable, T]]

  /**
   * Indicates whether this future has expired (timed out).
   * 
   * <p>
   * The timeout detected by this method is different from the timeout supported by <code>whenReady</code>. This timeout
   * is a timeout of the underlying future. If the underlying future does not support timeouts, this method must always
   * return <code>false</code>. 
   * </p>
   */
  def isExpired: Boolean

  /**
   * Indicates whether this future has been canceled.
   * 
   * <p>
   * If the underlying future does not support the concept of cancellation, this method must always return <code>false</code>. 
   * </p>
   */
  def isCanceled: Boolean
}
