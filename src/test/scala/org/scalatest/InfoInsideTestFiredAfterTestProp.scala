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

import org.scalatest.fixture._
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.junit.JUnit3Suite
import org.scalatest.junit.JUnitSuite
import org.scalatest.testng.TestNGSuite

class InfoInsideTestFiredAfterTestProp extends SuiteProp {

  test("When info appears in the code of a successful test, it should be reported after the TestSucceeded.") {
    forAll (examples) { suite =>
      if(suite.supportInfo) {
        val (infoProvidedIndex, testStartingIndex, testSucceededIndex) =
          getIndexesForInformerEventOrderTests(suite, suite.nameOfTest, suite.msg)
        testSucceededIndex should be < infoProvidedIndex
      }
    }
  }

  trait Services {
    val msg = "hi there, dude"
    val nameOfTest: String = "test name"
    val supportInfo: Boolean = true
  }

  type FixtureServices = Services
  
  def suite = new InfoInsideTestFiredAfterTestSuite 
  class InfoInsideTestFiredAfterTestSuite extends Suite with FixtureServices {
    override val nameOfTest = "testInfo(Informer)"
    def testInfo(info: Informer) {
      info(msg)
    }
  }
  
  def fixtureSuite = new InfoInsideTestFiredAfterTestFixtureSuite
  class InfoInsideTestFiredAfterTestFixtureSuite extends FixtureSuite with FixtureServices with StringFixture {
    override val nameOfTest = "testInfo(Informer)"
    /*def testInfo(info: Informer) {
      info(msg)
    }*/
    override val supportInfo = false
  }
  
  def junit3Suite = 
    new JUnit3Suite with FixtureServices {
      override val supportInfo = false
    }
  
  def junitSuite = 
    new JUnitSuite with FixtureServices {
      override val supportInfo = false
    }
  
  def testngSuite = 
    new TestNGSuite with FixtureServices {
      override val supportInfo = false
    }
  
  def funSuite =
    new FunSuite with FixtureServices {
      test(nameOfTest) {
        info(msg)
      }
    }

  def fixtureFunSuite =
    new StringFixtureFunSuite with FixtureServices {
      test(nameOfTest) { s =>
        info(msg)
      }
    }
  
  def funSpec =
    new FunSpec with FixtureServices {
      it(nameOfTest) {
        info(msg)
      }
    }

  def fixtureSpec =
    new StringFixtureSpec with FixtureServices {
      it(nameOfTest) { s =>
        info(msg)
      }
    }
  
  def featureSpec = 
    new FeatureSpec with FixtureServices {
    override val nameOfTest = "test feature Scenario: test name"
      feature("test feature") {
        scenario("test name") {
          info(msg)
        }
      }
    }
  
  def fixtureFeatureSpec = 
    new FixtureFeatureSpec with FixtureServices with StringFixture {
      override val nameOfTest = "test feature Scenario: test name"
      feature("test feature") {
        scenario("test name") { arg =>
          info(msg)
        }
      }
    }
  
  def flatSpec = 
    new FlatSpec with FixtureServices {
      override val nameOfTest = "test should provides info"
      "test" should "provides info" in {
        info(msg)
      }
    }
  
  def fixtureFlatSpec = 
    new FixtureFlatSpec with FixtureServices with StringFixture {
      override val nameOfTest = "test should provides info"
      "test" should "provides info" in { param =>
        info(msg)
      }
    }
  
  def freeSpec = 
    new FreeSpec with FixtureServices {
      override val nameOfTest = "test should provides info"
      "test" - {
        "should provides info" in {
          info(msg)
        }
      }
    }
  
  def fixtureFreeSpec = 
    new FixtureFreeSpec with FixtureServices with StringFixture {
      override val nameOfTest = "test should provides info"
      "test" - {
        "should provides info" in { param =>
          info(msg)
        }
      }
    }
  
  def propSpec = 
    new PropSpec with FixtureServices {
      property(nameOfTest) {
        info(msg)
      }
    }
  
  def fixturePropSpec = 
    new FixturePropSpec with FixtureServices with StringFixture {
      property(nameOfTest) { param =>
        info(msg)
      }
    }
  
  def wordSpec = 
    new WordSpec with FixtureServices {
      override val nameOfTest = "test should provides info"
      "test" should {
        "provides info" in {
          info(msg)
        }
      }
    }
  
  def fixtureWordSpec = 
    new FixtureWordSpec with FixtureServices with StringFixture {
      override val nameOfTest = "test should provides info"
      "test" should {
        "provides info" in { param =>
          info(msg)
        }
      }
    }
}
