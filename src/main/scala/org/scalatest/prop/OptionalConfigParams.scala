/*
 * Copyright 2001-2011 Artima, Inc.
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
package org.scalatest.prop

/**
 * Trait providing methods returning <code>PropertyCheckConfigParam</code> objects that can be used to
 * override the configuration values provided by the implicit <code>PropertyCheckConfig</code> object
 * passed to the <code>forAll</code> methods of traits <code>GeneratorDrivenPropertyChecks</code> (for ScalaTest-style
 * property checks) and <code>Checkers</code> (for ScalaCheck-style property checks).
 *
 * @author Bill Venners
 */
trait OptionalConfigParams {

  /**
   * Returns a <code>MinSuccessful</code> property check configuration parameter containing the passed value, which specifies the minimum number of successful
   * property evaluations required for the property to pass.
   *
   * @throws IllegalArgumentException if specified <code>value</code> is less than or equal to zero.
   */
  def minSuccessful(value: Int): MinSuccessful = new MinSuccessful(value)

  /**
   * Returns a <code>MaxSkipped</code> property check configuration parameter containing the passed value, which specifies the maximum number of skipped
   * property evaluations required for the property to pass.
   *
   * @throws IllegalArgumentException if specified <code>value</code> is less than zero.
   */
  def maxSkipped(value: Int): MaxSkipped = new MaxSkipped(value)

  /**
   * Returns a <code>MinSize</code> property check configuration parameter containing the passed value, which specifies the minimum size parameter to
   * provide to ScalaCheck, which it will use when generating objects for which size matters (such as
   * strings or lists).
   *
   * @throws IllegalArgumentException if specified <code>value</code> is less than zero.
   */
  def minSize(value: Int): MinSize = new MinSize(value)

  /**
   * Returns a <code>MaxSize</code> property check configuration parameter containing the passed value, which specifies the minimum size parameter to
   * provide to ScalaCheck, which it will use when generating objects for which size matters (such as
   * strings or lists).
   *
   * <p>
   * Note that the maximum size should be greater than or equal to the minimum size. This requirement is
   * enforced by the <code>PropertyCheckConfig</code> constructor and the <code>forAll</code> methods of
   * traits <code>PropertyChecks</code> and <code>Checkers</code>. In other words, it is enforced at the point
   * both a maximum and minimum size are provided together.
   * </p>
   * 
   * @throws IllegalArgumentException if specified <code>value</code> is less than zero.
   */
  def maxSize(value: Int): MaxSize = new MaxSize(value)

  /**
   * Returns a <code>Workers</code> property check configuration parameter containing the passed value, which suggests a number of worker threads
   * to us to evaluate a property.
   *
   * @throws IllegalArgumentException if specified <code>value</code> is less than or equal to zero.
   */
  def workers(value: Int): Workers = new Workers(value)
}

/**
 * Companion object that facilitates the importing of <code>OptionalConfigParams</code> members as
 * an alternative to mixing it in. One use case is to import <code>OptionalConfigParams</code> members so you can use
 * them in the Scala interpreter.
 */
object OptionalConfigParams extends OptionalConfigParams
