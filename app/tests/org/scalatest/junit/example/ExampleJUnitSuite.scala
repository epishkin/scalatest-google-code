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
package org.scalatest.junit.example;

import _root_.junit.framework.Assert

class ExampleJUnitSuite extends JUnit3Suite {
  
  override def setUp = println("this should get printed out before each method!")
  
  def testThatThrows = {
    throw new Exception("this test throws!")
  }
  
  def testThatFails = {
    Assert.assertEquals( "this test fails!", 1, 2 )
  }
  
  def testThatPasses = {
    // do nothing
  }
  
  override def tearDown = println("this should get printed out after each method!")

}
