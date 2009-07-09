/*
 * Copyright 2001-2009 Artima, Inc.
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
package org.scalatest.matchers

trait ShouldVerb {
  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * <p>
   * This class is used in conjunction with an implicit conversion to enable <code>should</code> methods to
   * be invoked on <code>String</code>s.
   * </p>
   *
   * @author Bill Venners
   */
  class StringShouldWrapper(left: String) {

    /**
     * This method enables syntax such as the following in a <code>FlatSpec</code>:
     *
     * <pre>
     * "A Stack (when empty)" should "be empty" in {
     *   assert(emptyStack.empty)
     * }
     * </pre>
     *
     * <p>
     * <code>FlatSpec</code> passes in a function via the implicit parameter that takes
     * three strings and results in a <code>ResultOfStringPassedToVerb</code>. This method
     * simply invokes this function, passing in left, right, and the verb string
     * <code>"should"</code>.
     * </p>
     */
    def should(right: String)(implicit fun: (String, String, String) => ResultOfStringPassedToVerb): ResultOfStringPassedToVerb = {
      fun(left, right, "should")
    }

    def should(right: => Unit)(implicit fun: (String, () => Unit, String) => Unit) {
      fun(left, right _, "should")
    }
    def should(resultOfAfterWordApplication: ResultOfAfterWordApplication)(implicit fun: (String, ResultOfAfterWordApplication, String) => Unit) {
      fun(left, resultOfAfterWordApplication, "should")
    }
  }

  /**
   * Implicitly converts an object of type <code>java.lang.String</code> to a <code>StringShouldWrapper</code>,
   * to enable <code>should</code> methods to be invokable on that object.
   */
  implicit def convertToStringShouldWrapper(o: String): StringShouldWrapper = new StringShouldWrapper(o)
}