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
    val framework = new ScalaTestFramework
    import framework.ScalaTestRunner
    
    val loggers: Array[Logger] = Array(new TestLogger)
    val runner = framework.testRunner(currentThread.getContextClassLoader, loggers).asInstanceOf[ScalaTestRunner]
    runner.parsePropsAndTags(Array(rawargs).filter(!_.equals("")))
  }
  
  private def getRepoArgsList(rawargs:String):List[String] = {
    val (propertiesArgsList, includesArgsList, excludesArgsList, repoArgsList) = parsePropsAndTags(rawargs)
    repoArgsList
  }
  
  private def getIncludesArgsList(rawargs:String):List[String] = {
    val (propertiesArgsList, includesArgsList, excludesArgsList, repoArgsList) = parsePropsAndTags(rawargs)
    includesArgsList
  }
  
  private def getExcludesArgsList(rawargs:String):List[String] = {
    val (propertiesArgsList, includesArgsList, excludesArgsList, repoArgsList) = parsePropsAndTags(rawargs)
    excludesArgsList
  }
  
  // junitxml and file has been disabled until we sort out the way for pararrel execution.
  ignore("parse argument junitxml(directory=\"test\")") {
    val repoArgsList = getRepoArgsList("junitxml(directory=\"test\")")
    assert(repoArgsList.length == 2)
    assert(repoArgsList(0) == "-u")
    assert(repoArgsList(1) == "test")
  }
  
  ignore("parse argument junitxml should fail with IllegalArgumentException") {
    intercept[IllegalArgumentException] { getRepoArgsList("junitxml") }
  }
  
  ignore("parse argument junitxml (directory=\"test\") should fail with IllegalArgumentException") {
    intercept[IllegalArgumentException] { getRepoArgsList("junitxml (directory=\"test\")") }
  }
  
  ignore("parse argument junitxml directory=\"test\" should fail with IllegalArgumentException") {
    intercept[IllegalArgumentException] { getRepoArgsList("junitxml directory=\"test\"") }
  }
  
  ignore("parse argument junitxml(directory=\"test\" should fail with IllegalArgumentException") {
    intercept[IllegalArgumentException] { getRepoArgsList("junitxml(directory=\"test\"") }
  }
  
  ignore("parse argument junitxmldirectory=\"test\") should fail with IllegalArgumentException") {
    intercept[IllegalArgumentException] { getRepoArgsList("junitxmldirectory=\"test\")") }
  }
  
  ignore("parse argument junitxml(director=\"test\") should fail with IllegalArgumentException") {
    intercept[IllegalArgumentException] { getRepoArgsList("junitxml(director=\"test\")") }
  }
  
  ignore("parse argument file(filename=\"test.xml\")") {
    val repoArgsList = getRepoArgsList("file(filename=\"test.xml\")")
    assert(repoArgsList.length == 2)
    assert(repoArgsList(0) == "-f")
    assert(repoArgsList(1) == "test.xml")
  }
  
  ignore("parse argument file(filename=\"test.xml\", config=\"durations shortstacks dropteststarting\")") {
    val repoArgsList = getRepoArgsList("file(filename=\"test.xml\", config=\"durations shortstacks dropteststarting\")")
    assert(repoArgsList.length == 2)
    assert(repoArgsList(0) == "-fDSN")
    assert(repoArgsList(1) == "test.xml")
  }
  
  test("parse argument stdout") {
    val repoArgsList = getRepoArgsList("stdout")
    assert(repoArgsList.length == 1)
    assert(repoArgsList(0) == "-o")
  }
  
  test("parse argument stdout(config=\"nocolor fullstacks doptestsucceeded\")") {
    val repoArgsList = getRepoArgsList("stdout(config=\"nocolor fullstacks droptestsucceeded\")")
    assert(repoArgsList.length == 1)
    assert(repoArgsList(0) == "-oWFC")
  }
  
  test("parse argument stdout (config=\"nocolor fullstacks doptestsucceeded\") should fail with IllegalArgumentException") {
    intercept[IllegalArgumentException] { getRepoArgsList("stdout (config=\"nocolor fullstacks doptestsucceeded\")") }
  }
  
  test("parse argument stdout config=\"nocolor fullstacks doptestsucceeded\" should fail with IllegalArgumentException") {
    intercept[IllegalArgumentException] { getRepoArgsList("stdout config=\"nocolor fullstacks doptestsucceeded\"") }
  }
  
  test("parse argument stdout(config=\"nocolor fullstacks doptestsucceeded\" should fail with IllegalArgumentException") {
    intercept[IllegalArgumentException] { getRepoArgsList("stdout(config=\"nocolor fullstacks doptestsucceeded\"") }
  }
  
  test("parse argument stdoutconfig=\"nocolor fullstacks doptestsucceeded\") should fail with IllegalArgumentException") {
    intercept[IllegalArgumentException] { getRepoArgsList("stdoutconfig=\"nocolor fullstacks doptestsucceeded\")") }
  }
  
  test("parse argument stdout(confi=\"nocolor fullstacks doptestsucceeded\") should fail with IllegalArgumentException") {
    intercept[IllegalArgumentException] { getRepoArgsList("stdout(confi=\"nocolor fullstacks doptestsucceeded\")") }
  }
  
  test("parse argument stderr") {
    val repoArgsList = getRepoArgsList("stderr")
    assert(repoArgsList.length == 1)
    assert(repoArgsList(0) == "-e")
  }
  
  test("parse argument stderr(config=\"dropinfoprovided dropsuitestarting droptestignored\")") {
    val repoArgsList = getRepoArgsList("stderr(config=\"dropinfoprovided dropsuitestarting droptestignored\")")
    assert(repoArgsList.length == 1)
    assert(repoArgsList(0) == "-eOHX")
  }
  
  test("parse argument include(org.scala.a, org.scala.b, org.scala.c)") {
    val inclArgsList = getIncludesArgsList("include(org.scala.a, org.scala.b, org.scala.c)")
    assert(inclArgsList.length == 2)
    assert(inclArgsList(0) == "-n")
    assert(inclArgsList(1) == "org.scala.a org.scala.b org.scala.c")
  }
  
  test("parse argument include(\"org.scala.a\", \"org.scala.b\", \"org.scala.c\")") {
    val inclArgsList = getIncludesArgsList("include(\"org.scala.a\", \"org.scala.b\", \"org.scala.c\")")
    assert(inclArgsList.length == 2)
    assert(inclArgsList(0) == "-n")
    assert(inclArgsList(1) == "org.scala.a org.scala.b org.scala.c")
  }
  
  test("parse argument include should fail with IllegalArgumentException") {
    intercept[IllegalArgumentException] { getIncludesArgsList("include") }
  }
  
  test("parse argument include (org.scala.a, org.scala.b, org.scala.c) should fail with IllegalArgumentException") {
    intercept[IllegalArgumentException] { getIncludesArgsList("include (org.scala.a, org.scala.b, org.scala.c)") }
  }
  
  test("parse argument include(org.scala.a, org.scala.b, org.scala.c should fail with IllegalArgumentException") {
    intercept[IllegalArgumentException] { getIncludesArgsList("include (org.scala.a, org.scala.b, org.scala.c") }
  }
  
  test("parse argument includeorg.scala.a, org.scala.b, org.scala.c) should fail with IllegalArgumentException") {
    intercept[IllegalArgumentException] { getIncludesArgsList("includeorg.scala.a, org.scala.b, org.scala.c)") }
  }
  
  test("parse argument include org.scala.a, org.scala.b, org.scala.c should fail with IllegalArgumentException") {
    intercept[IllegalArgumentException] { getIncludesArgsList("include org.scala.a, org.scala.b, org.scala.c") }
  }
  
  test("parse argument exclude(org.scala.a, org.scala.b, org.scala.c)") {
    val exclArgsList = getExcludesArgsList("exclude(org.scala.a, org.scala.b, org.scala.c)")
    assert(exclArgsList.length == 2)
    assert(exclArgsList(0) == "-l")
    assert(exclArgsList(1) == "org.scala.a org.scala.b org.scala.c")
  }
  
  test("parse argument exclude(\"org.scala.a\", \"org.scala.b\", \"org.scala.c\")") {
    val exclArgsList = getExcludesArgsList("exclude(\"org.scala.a\", \"org.scala.b\", \"org.scala.c\")")
    assert(exclArgsList.length == 2)
    assert(exclArgsList(0) == "-l")
    assert(exclArgsList(1) == "org.scala.a org.scala.b org.scala.c")
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
