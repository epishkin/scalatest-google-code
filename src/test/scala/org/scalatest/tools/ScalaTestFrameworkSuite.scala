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
package org.scalatest.tools

import org.scalatest.FunSuite
import org.scalatools.testing.Logger
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ScalaTestFrameworkSuite extends FunSuite{

  test("framework name"){
    assert(new ScalaTestFramework().name === "ScalaTest")
  }

  test("tests contains single test fingerprint"){
    val framework = new ScalaTestFramework
    val fingerprints = framework.tests
    assert(fingerprints.size === 1)

    val fingerprint =
      fingerprints(0).asInstanceOf[org.scalatools.testing.TestFingerprint]

    assert(fingerprint.isModule === false)
    assert(fingerprint.superClassName === "org.scalatest.Suite")
  }

  test("creates runner with given arguments"){
    val framework = new ScalaTestFramework

    import framework.ScalaTestRunner

    val loggers: Array[Logger] = Array(new TestLogger)
    val runner = framework.testRunner(currentThread.getContextClassLoader, loggers).asInstanceOf[ScalaTestRunner]
    assert(runner.testLoader == currentThread.getContextClassLoader)
    assert(runner.loggers === loggers)
  }
  
  private def parsePropsAndTags(rawargs:String) = {
    val translator = new SbtFriendlyParamsTranslator()
    translator.parsePropsAndTags(Array(rawargs).filter(!_.equals("")))
  }
  
  test("-g not supported when runs in SBT test-framework") {
    intercept[IllegalArgumentException] { parsePropsAndTags("-g") }
  }
  
  test("-f not supported when runs in SBT test-framework") {
    intercept[IllegalArgumentException] { parsePropsAndTags("-f") }
  }
  
  test("file not supported when runs in SBT test-framework") {
    intercept[IllegalArgumentException] { parsePropsAndTags("file") }
  }
  
  test("-u not supported when runs in SBT test-framework") {
    intercept[IllegalArgumentException] { parsePropsAndTags("-u") }
  }
  
  test("junitxml not supported when runs in SBT test-framework") {
    intercept[IllegalArgumentException] { parsePropsAndTags("junitxml") }
  }
  
  test("-d not supported when runs in SBT test-framework") {
    intercept[IllegalArgumentException] { parsePropsAndTags("-d") }
  }
  
  test("-a not supported when runs in SBT test-framework") {
    intercept[IllegalArgumentException] { parsePropsAndTags("-a") }
  }
  
  test("-x not supported when runs in SBT test-framework") {
    intercept[IllegalArgumentException] { parsePropsAndTags("-x") }
  }
  
  test("-h not supported when runs in SBT test-framework") {
    intercept[IllegalArgumentException] { parsePropsAndTags("-h") }
  }

  class TestLogger extends Logger{
    def trace(t:Throwable){}
    def error(msg:String){}
    def warn(msg:String){}
    def info(msg:String){}
    def debug(msg:String){}
    def ansiCodesSupported = false
  }
}
