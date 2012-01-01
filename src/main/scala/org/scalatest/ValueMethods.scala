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
package org.scalatest

/**
 * Trait providing value methods on <code>Option</code>, <code>Either</code>, and <code>PartialFunction</code>.
 *
 * <p>
 * Here are some examples:
 * </p>
 *
 * <pre>
 * // On Option
 * option.value should be &gt; 9
 *
 * // On Either
 * either.rightValue should be &lt;= 10
 *
 * // On PartialFunction
 * pf.valueAt("IV") should equal (4)
 * </pre>
 *
 * <p>
 * For more details, see the Scaladoc documentation for <a href="ValueOnOption.html"><code>ValueOnOption</code></a>,
 * <a href="ValueOnEither.html"><code>ValueOnEither</code></a>, <a href="ValueOnPartialFunction.html"><code>ValueOnPartialFunction</code></a>.
 * </p>
 */
trait ValueMethods extends ValueOnOption with ValueOnEither with ValueOnPartialFunction 

/**
 * Companion object that facilitates the importing of <code>ValueMethods</code> members as 
 * an alternative to mixing it in. One use case is to import <code>ValueMethods</code>'s members so you can use
 * value methods on options, eithers, and partial functions in the Scala interpreter:
 *
 * <pre class="stREPL">
 * $ scala -cp target/jar_contents/
 * Welcome to Scala version 2.9.1.final (Java HotSpot(TM) 64-Bit Server VM, Java 1.6.0_29).
 * Type in expressions to have them evaluated.
 * Type :help for more information.
 *
 * scala&gt; import org.scalatest._
 * import org.scalatest._
 *
 * scala&gt; import matchers.ShouldMatchers._
 * import matchers.ShouldMatchers._
 *
 * scala&gt; import ValueMethods._
 * import ValueMethods._
 *
 * scala&gt; val opt1: Option[Int] = Some(1)
 * opt1: Option[Int] = Some(1)
 * 
 * scala&gt; val opt2: Option[Int] = None
 * opt2: Option[Int] = None
 * 
 * scala&gt; opt1.value should be &lt; 10
 * 
 * scala&gt; opt2.value should be &lt; 10
 * org.scalatest.TestFailedException: The Option on which value was invoked was not defined.
 *   at org.scalatest.ValueOnOption$Valuable.value(ValueOnOption.scala:68)
 *   at .&lt;init&gt;(&lt;console&gt;:18)
 *   ...
 * </pre>
 *
 */
object ValueMethods extends ValueMethods
