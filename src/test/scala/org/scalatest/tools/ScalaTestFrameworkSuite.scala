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
    assert(fingerprints.size === 2)

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
  
  test("graphic not supported when runs in SBT test-framework") {
    intercept[IllegalArgumentException] { parsePropsAndTags("graphic") }
  }
  
  test("-f not supported when runs in SBT test-framework") {
    intercept[IllegalArgumentException] { parsePropsAndTags("-f test.xml") }
  }
  
  test("file not supported when runs in SBT test-framework") {
    intercept[IllegalArgumentException] { parsePropsAndTags("file(filename=\"test.xml\")") }
  }
  
  test("-u not supported when runs in SBT test-framework") {
    intercept[IllegalArgumentException] { parsePropsAndTags("-u test") }
  }
  
  test("junitxml not supported when runs in SBT test-framework") {
    intercept[IllegalArgumentException] { parsePropsAndTags("junitxml(directory=\"test\")") }
  }
  
  test("-d not supported when runs in SBT test-framework") {
    intercept[IllegalArgumentException] { parsePropsAndTags("-d test") }
  }
  
  test("-a not supported when runs in SBT test-framework") {
    intercept[IllegalArgumentException] { parsePropsAndTags("-a 99") }
  }
  
  test("dashboard not supported when runs in SBT test-framework") {
    intercept[IllegalArgumentException] { parsePropsAndTags("dashboard(directory=\"test\", archive=\"99\")") }
  }
  
  test("-x not supported when runs in SBT test-framework") {
    intercept[IllegalArgumentException] { parsePropsAndTags("-x test") }
  }
  
  test("xml not supported when runs in SBT test-framework") {
    intercept[IllegalArgumentException] { parsePropsAndTags("xml(directory=\"test\")") }
  }
  
  test("-h not supported when runs in SBT test-framework") {
    intercept[IllegalArgumentException] { parsePropsAndTags("-h test.html") }
  }
  
  test("html not supported when runs in SBT test-framework") {
    intercept[IllegalArgumentException] { parsePropsAndTags("html(filename=\"test.html\")") }
  }
  
  test("-r not supported when runs in SBT test-framework") {
    intercept[IllegalArgumentException] { parsePropsAndTags("-r a.b.c") }
  }
  
  test("reporterclass not supported when runs in SBT test-framework") {
    intercept[IllegalArgumentException] { parsePropsAndTags("reporterclass(classname=\"a.b.c\")") }
  }
  
  test("-c not supported when runs in SBT test-framework") {
    intercept[IllegalArgumentException] { parsePropsAndTags("-c") }
  }
  
  test("concurrent not supported when runs in SBT test-framework") {
    intercept[IllegalArgumentException] { parsePropsAndTags("concurrent") }
  }
  
  test("-m not supported when runs in SBT test-framework") {
    intercept[IllegalArgumentException] { parsePropsAndTags("-m a.b.c") }
  }
  
  test("memberonly not supported when runs in SBT test-framework") {
    intercept[IllegalArgumentException] { parsePropsAndTags("memberonly(a.b.c, a.b.d)") }
  }
  
  test("-w not supported when runs in SBT test-framework") {
    intercept[IllegalArgumentException] { parsePropsAndTags("-w a.b.c") }
  }
  
  test("wildcard not supported when runs in SBT test-framework") {
    intercept[IllegalArgumentException] { parsePropsAndTags("wildcard(a.b.c, a.b.d)") }
  }
  
  test("-s not supported when runs in SBT test-framework") {
    intercept[IllegalArgumentException] { parsePropsAndTags("-s a.b.c") }
  }
  
  test("suite not supported when runs in SBT test-framework") {
    intercept[IllegalArgumentException] { parsePropsAndTags("suite(a.b.c, a.b.d)") }
  }
  
  test("-j not supported when runs in SBT test-framework") {
    intercept[IllegalArgumentException] { parsePropsAndTags("-j a.b.c") }
  }
  
  test("junit not supported when runs in SBT test-framework") {
    intercept[IllegalArgumentException] { parsePropsAndTags("junit(a.b.c, a.b.d)") }
  }
  
  test("-t not supported when runs in SBT test-framework") {
    intercept[IllegalArgumentException] { parsePropsAndTags("-t a.b.c") }
  }
  
  test("testng not supported when runs in SBT test-framework") {
    intercept[IllegalArgumentException] { parsePropsAndTags("testng(a.b.c, a.b.d)") }
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
