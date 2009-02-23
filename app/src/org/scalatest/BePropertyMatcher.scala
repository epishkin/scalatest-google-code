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

// T is the type of the object that has a Boolean property to verify with an instance of this trait
// This is not a subtype of BeMatcher, because BeMatcher only works after "be", but 
// BePropertyMatcher will work after "be", "be a", or "be an"
trait BePropertyMatcher[-T] extends Function1[T, BePropertyMatchResult] {
  def apply(objectWithProperty: T): BePropertyMatchResult
}

