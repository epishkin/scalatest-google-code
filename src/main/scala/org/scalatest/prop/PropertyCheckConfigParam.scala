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
 * Abstract class defining a family of configuration parameters for property checks.
 * 
 * <p>
 * The subclasses of this abstract class are used to pass configuration information to
 * the <code>forAll</code> methods of traits <code>PropertyChecks</code> (for ScalaTest-style
 * property checks) and <code>Checkers</code>(for ScalaCheck-style property checks).
 * </p>
 *
 * @author Bill Venners
 */
sealed abstract class PropertyCheckConfigParam

/**
 * A <code>PropertyCheckConfigParam</code> that specifies the minimum number of successful
 * property evaluations required for the property to pass.
 *
 * @throws IllegalArgumentException if specified <code>value</code> is less than or equal to zero.
 *
 * @author Bill Venners
 */
case class MinSuccessful(value: Int) extends PropertyCheckConfigParam {
  require(value > 0)
}

/**
 * A <code>PropertyCheckConfigParam</code> that specifies the maximum number of skipped
 * property evaluations allowed during property evaluation.
 *
 * <p>
 * In <code>GeneratorDrivenPropertyChecks</code>, a property evaluation is skipped if it throws
 * <code>UnmetConditionException</code>, which is produce by <code>whenever</code> clause that
 * evaluates to false. For example, consider this ScalaTest property check:
 * </p>
 *
 * <pre>
 * // forAll defined in <code>GeneratorDrivenPropertyChecks</code>
 * forAll { (n: Int) => 
 *   whenever (n > 0) {
 *     doubleIt(n) should equal (n * 2)
 *   }
 * }
 *
 * </pre>
 *
 * <p>
 * In the above code, whenever a non-positive <code>n</code> is passed, the property function will complete abruptly
 * with <code>UnmetConditionException</code>.
 * </p>
 *
 * <p>
 * Simiarly, in <code>Checkers</code>, a property evaluation is skipped if the expression to the left
 * of ScalaCheck's <code>==></code> operator is false. Here's an example:
 * </p>
 *
 * <pre>
 * // forAll defined in <code>Checkers</code>
 * forAll { (n: Int) => 
 *   (n > 0) ==> doubleIt(n) == (n * 2)
 * }
 *
 * </pre>
 *
 * <p>
 * For either kind of property check, <code>MaxSkipped</code> indicates the maximum number of skipped 
 * evaluations that will be allowed. As soon as one past this number of evaluations indicates it needs to be skipped,
 * the property check will fail.
 * </p>
 *
 * @throws IllegalArgumentException if specified <code>value</code> is less than zero.
 *
 * @author Bill Venners
 */
case class MaxSkipped(value: Int) extends PropertyCheckConfigParam {
  require(value >= 0)
}

/**
 * A <code>PropertyCheckConfigParam</code> that specifies the minimum size parameter to
 * provide to ScalaCheck, which it will use when generating objects for which size matters (such as
 * strings or lists).
 *
 * @throws IllegalArgumentException if specified <code>value</code> is less than zero.
 *
 * @author Bill Venners
 */
case class MinSize(value: Int) extends PropertyCheckConfigParam {
  require(value >= 0)
}

/**
 * A <code>PropertyCheckConfigParam</code> that specifies the maximum size parameter to
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
 *
 * @author Bill Venners
 */
case class MaxSize(value: Int) extends PropertyCheckConfigParam {
  require(value >= 0)
}

/**
 * A <code>PropertyCheckConfigParam</code> that specifies the number of worker threads
 * to use when evaluating a property.
 *
 * @throws IllegalArgumentException if specified <code>value</code> is less than or equal to zero.
 *
 * @author Bill Venners
 */
case class Workers(value: Int) extends PropertyCheckConfigParam {
  require(value > 0)
}

