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
package org.scalatest;

import java.lang.annotation.*; 

/**
 * Annotation to associate a <em>wrapper suite</em> with a non-<code>Suite</code> class, so it can be run via ScalaTest.
 *
 * <p>
 * A class will be considered annotated with <code>WrapWith</code> if it is annotated directly or one of its superclasses (but
 * not supertraits) are annotated with <code>WrapWith</code>.
 * The wrapper suite must have a public, one-arg constructor that takes a <code>Class</code> instance whose type parameter
 * is compatible with the <em>class to wrap</em>: <em>i.e.</em>, the class being annotated with <code>WrapWith</code>.
 * ScalaTest will load the class to wrap and construct a new instance of the wrapper suite, passing in the <code>Class</code>
 * instance for the class to wrap.
 * Here's an example:
 * </p>
 *
 * <pre class="stHighlight">
 * @WrapWith(classOf[Specs1Runner])
 * class LegacySpec extends Specification {
 *   // ...
 * }
 * </pre>
 *
 * <p>
 * The <code>Specs1Runner</code> would need to have a public, no-arg constructor that accepts subclasses of <code>Specification</code>:
 * </p>
 *
 * <pre class="stHighlight">
 * class Specs1Runner(clazz: Class[_ <: Specification]) {
 *   // ...
 * }
 * </pre>
 *
 * @author Bill Venners
 * @author Chua Chee Seng
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface WrapWith {

    /**
     * The wrapper suite.
     *
     * @return a wrapper suite class, which must have a constructor that takes a single <code>Class</code> to run
     */
    Class<? extends Suite> value();
}
