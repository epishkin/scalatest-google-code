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
package org.scalatest.prop

class ScalaCheckSuiteSuite() extends Suite {

  def testCheckProp() {
    
  }

/*
OK, here's what I think we want for the property checks. I'd like to have the ability to call check like I can call assert, so that
means that check needs to be a method in Suite. But sometimes people will want to know how many tests were run. To do that, I'll let
you pass a reporter into check. That means you need to write your test method that takes a reporter. The info will come out as infoProvided.
So,

def testSomething(reporter: Reporter) {
  val concatListsProp = property((l1: List[Int], l2: List[Int]) => l1.size + l2.size == (l1 ::: l2).size)
  check(concatListsProp, reporter)
}) 
*/
}
