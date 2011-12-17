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
 * Trait that can be mixed in disable implicit conversion to Equalizer defined in Assertions:-
 * 
 * <pre class="stHighlight">
 * implicit def convertToEqualizer(left: Any) = new Equalizer(left)
 * </pre>
 * 
 * This is to solve the problem when the above implicit conversion clashes with an implicit conversion used in the code you are trying to test, 
 * causing your program won't compile.  You can just mix in this trait to disable implicit conversion from the ScalaTest's Assertions:-
 * 
 * <pre class="stHighlight">
 * class MySuite extends FunSuite with NoDefaultImplicits { 
 *   // ... 
 * } 
 * </pre>
 *
 * @author Chee Seng
 */
trait NoDefaultImplicits extends Suite { 
  override def convertToEqualizer(left: Any): Equalizer = super.convertToEqualizer(left)
}