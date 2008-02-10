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
package org.scalatest.testng.example;

import org.testng.annotations.Test
import org.testng.annotations.BeforeMethod
import org.testng.annotations.BeforeClass
import org.testng.annotations.BeforeSuite
import org.testng.annotations.AfterMethod
import org.testng.annotations.AfterClass
import org.testng.annotations.AfterSuite
import org.scalatest.Report
import org.scalatest.Reporter
import org.specs.mock._

class ExampleTestNGSuite extends TestNGSuite with JMocker{

  @AfterSuite
  def failAfterSuite(){ throw new Exception("fail in before method") }

  @BeforeMethod def passBeforeMethod(){}
  @BeforeClass def passBeforeClass(){}
  @BeforeSuite def passBeforeSuite(){}
  
  @AfterMethod def passAfterMethod(){}
  @AfterClass def passAfterClass(){}
  @AfterSuite def passAfterSuite(){}
  
  @Test{val invocationCount=10} def thisTestRunsTenTimes = {}
  
  @Test{val groups=Array("runMe")} 
  def testWithException(){ 
    throw new Exception("exception!!!") 
   }
  
  @Test{val groups=Array("runMe")} def testWithAssertFail = assert( 1 === 2, "assert fail!!!" )
  
  @Test{val dependsOnMethods=Array("testWithException")} def testToGetSkipped = {}
  
}

