package org.scalatest.tools

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class FriendlyParamsTranslatorSuite extends FunSuite {
  
  private def parsePropsAndTags(rawargs:String) = {
    val translator = new FriendlyParamsTranslator()
    translator.parsePropsAndTags(Array(rawargs).filter(!_.equals("")))
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
  test("parse argument junitxml(directory=\"test\")") {
    val repoArgsList = getRepoArgsList("junitxml(directory=\"test\")")
    assert(repoArgsList.length == 2)
    assert(repoArgsList(0) == "-u")
    assert(repoArgsList(1) == "test")
  }
  
  test("parse argument junitxml should fail with IllegalArgumentException") {
    intercept[IllegalArgumentException] { getRepoArgsList("junitxml") }
  }
  
  test("parse argument junitxml (directory=\"test\") should fail with IllegalArgumentException") {
    intercept[IllegalArgumentException] { getRepoArgsList("junitxml (directory=\"test\")") }
  }
  
  test("parse argument junitxml directory=\"test\" should fail with IllegalArgumentException") {
    intercept[IllegalArgumentException] { getRepoArgsList("junitxml directory=\"test\"") }
  }
  
  test("parse argument junitxml(directory=\"test\" should fail with IllegalArgumentException") {
    intercept[IllegalArgumentException] { getRepoArgsList("junitxml(directory=\"test\"") }
  }
  
  test("parse argument junitxmldirectory=\"test\") should fail with IllegalArgumentException") {
    intercept[IllegalArgumentException] { getRepoArgsList("junitxmldirectory=\"test\")") }
  }
  
  test("parse argument junitxml(director=\"test\") should fail with IllegalArgumentException") {
    intercept[IllegalArgumentException] { getRepoArgsList("junitxml(director=\"test\")") }
  }
  
  test("parse argument file(filename=\"test.xml\")") {
    val repoArgsList = getRepoArgsList("file(filename=\"test.xml\")")
    assert(repoArgsList.length == 2)
    assert(repoArgsList(0) == "-f")
    assert(repoArgsList(1) == "test.xml")
  }
  
  test("parse argument file(filename=\"test.xml\", config=\"durations shortstacks dropteststarting\")") {
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
}