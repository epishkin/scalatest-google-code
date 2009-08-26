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

abstract class SubjectVerbStringTaggedAs(val verb: String, val rest: String, val tags: List[Tag]) {

  // "A Stack" should "bla bla" taggedAs(SlowTest) in {
  //                                               ^
  // def in(testFun: => Unit)

  // "A Stack" must "test this" taggedAs(mytags.SlowAsMolasses) is (pending)
  //                                                            ^
  def is(testFun: => PendingNothing)

  // "A Stack" should "bla bla" taggedAs(SlowTest) ignore {
  //                                               ^
  // def ignore(testFun: => Unit)

  // "A Stack" should "bla bla" taggedAs(SlowTest) in { fixture =>
  //                                               ^
 //  def in(testFun: T => Any)

  // "A Stack" should "bla bla" taggedAs(SlowTest) ignore { fixture =>
  //                                               ^
  // def ignore(testFun: T => Any)
}
