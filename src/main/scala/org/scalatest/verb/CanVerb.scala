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
package org.scalatest.verb

import org.scalatest._

trait CanVerb {
  /**
   * This class is part of the ScalaTest matchers DSL. Please see the documentation for <a href="ShouldMatchers.html"><code>ShouldMatchers</code></a> or <a href="MustMatchers.html"><code>MustMatchers</code></a> for an overview of
   * the matchers DSL.
   *
   * <p>
   * This class is used in conjunction with an implicit conversion to enable <code>can</code> methods to
   * be invoked on <code>String</code>s.
   * </p>
   *
   * @author Bill Venners
   */
  class StringCanWrapperForVerb(left: String) {

    /**
     * This method enables syntax such as the following in a <code>FlatSpec</code>:
     *
     * <pre>
     * "A Stack (when empty)" can "be empty" in {
     *   assert(emptyStack.empty)
     * }
     * </pre>
     *
     * <p>
     * <code>FlatSpec</code> passes in a function via the implicit parameter that takes
     * three strings and results in a <code>ResultOfStringPassedToVerb</code>. This method
     * simply invokes this function, passing in left, right, and the verb string
     * <code>"can"</code>.
     * </p>
     */
    def can(right: String)(implicit fun: (String, String, String) => ResultOfStringPassedToVerb): ResultOfStringPassedToVerb = {
      fun(left, "can", right)
    }

    // For FlatSpec "bla" should behave like bla syntax
    def can(right: BehaveWord)(implicit fun: (String) => ResultOfBehaveWordPassedToVerb): ResultOfBehaveWordPassedToVerb = {
      fun(left)
    }

    // TODO: Make this a type alias, no, that won't work. Hmm. Would like something that. I know, make the
    // result type. Well either make the result type some bous value, or probably better define a type that
    // extends () => Unit and use that  as the center one. Probably very unlikely it would ever clash, but
    // a better practice would be do use one "role-defining type" in here.
    // These two are for WordSpec. Won't work elsewhere because only WordSpec defines these implicit
    // parameters.
    def can(right: => Unit)(implicit fun: StringVerbBlockRegistration) {
      fun(left, "can", right _)
    }

    def can(resultOfAfterWordApplication: ResultOfAfterWordApplication)(implicit fun: (String, ResultOfAfterWordApplication, String) => Unit) {
      fun(left, resultOfAfterWordApplication, "can")
    }
  }

  /**
   * Implicitly converts an object of type <code>java.lang.String</code> to a <code>StringShouldWrapper</code>,
   * to enable <code>should</code> methods to be invokable on that object.
   */
  implicit def convertToStringCanWrapper(o: String): StringCanWrapperForVerb = new StringCanWrapperForVerb(o)
}
