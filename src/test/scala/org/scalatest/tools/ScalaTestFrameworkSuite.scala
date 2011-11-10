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
  
  def getRepoArgsList(args:Array[String]):List[String] = {
    val framework = new ScalaTestFramework
    import framework.ScalaTestRunner
    
    val loggers: Array[Logger] = Array(new TestLogger)
    val runner = framework.testRunner(currentThread.getContextClassLoader, loggers).asInstanceOf[ScalaTestRunner]
    val (propertiesArgsList, includesArgsList,
        excludesArgsList, repoArgsList) = runner.parsePropsAndTags(args.filter(!_.equals("")))
    repoArgsList
  }
  
  def shouldFailWithIllegalArgumentException(arg:String) {
    intercept[IllegalArgumentException] {
      getRepoArgsList(Array(arg))
    }
  }
  
  test("parse argument junitxml(directory=\"test\")") {
    val repoArgsList = getRepoArgsList(Array("junitxml(directory=\"test\")"))
    assert(repoArgsList.length == 2)
    assert(repoArgsList(0) == "-u")
    assert(repoArgsList(1) == "test")
  }
  
  test("parse argument junitxml should fail with IllegalArgumentException") {
    shouldFailWithIllegalArgumentException("junitxml")
  }
  
  test("parse argument junitxml (directory=\"test\") should fail with IllegalArgumentException") {
    shouldFailWithIllegalArgumentException("junitxml (directory=\"test\")")
  }
  
  test("parse argument junitxml directory=\"test\" should fail with IllegalArgumentException") {
    shouldFailWithIllegalArgumentException("junitxml directory=\"test\"")
  }
  
  test("parse argument junitxml(directory=\"test\" should fail with IllegalArgumentException") {
    shouldFailWithIllegalArgumentException("junitxml(directory=\"test\"")
  }
  
  test("parse argument junitxmldirectory=\"test\") should fail with IllegalArgumentException") {
    shouldFailWithIllegalArgumentException("junitxmldirectory=\"test\")")
  }
  
  test("parse argument junitxml(director=\"test\") should fail with IllegalArgumentException") {
    shouldFailWithIllegalArgumentException("junitxml(director=\"test\")")
  }
  
  test("parse argument file(filename=\"test.xml\")") {
    val repoArgsList = getRepoArgsList(Array("file(filename=\"test.xml\")"))
    assert(repoArgsList.length == 2)
    assert(repoArgsList(0) == "-f")
    assert(repoArgsList(1) == "test.xml")
  }
  
  test("parse argument file(filename=\"test.xml\", config=\"durations shortstacks dropteststarting\")") {
    val repoArgsList = getRepoArgsList(Array("file(filename=\"test.xml\", config=\"durations shortstacks dropteststarting\")"))
    assert(repoArgsList.length == 2)
    assert(repoArgsList(0) == "-fDSN")
    assert(repoArgsList(1) == "test.xml")
  }
  
  test("parse argument stdout") {
    val repoArgsList = getRepoArgsList(Array("stdout"))
    assert(repoArgsList.length == 1)
    assert(repoArgsList(0) == "-o")
  }
  
  test("parse argument stdout(config=\"nocolor fullstacks doptestsucceeded\")") {
    val repoArgsList = getRepoArgsList(Array("stdout(config=\"nocolor fullstacks doptestsucceeded\")"))
    assert(repoArgsList.length == 1)
    assert(repoArgsList(0) == "-oWFC")
  }
  
  test("parse argument stderr") {
    val repoArgsList = getRepoArgsList(Array("stderr"))
    assert(repoArgsList.length == 1)
    assert(repoArgsList(0) == "-e")
  }
  
  test("parse argument stderr(config=\"dropinfoprovided dropsuitestarting droptestignored\")") {
    val repoArgsList = getRepoArgsList(Array("stderr(config=\"dropinfoprovided dropsuitestarting droptestignored\")"))
    assert(repoArgsList.length == 1)
    assert(repoArgsList(0) == "-eOHX")
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
