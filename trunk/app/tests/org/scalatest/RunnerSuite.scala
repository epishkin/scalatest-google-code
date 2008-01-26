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
package org.scalatest

class RunnerSuite() extends Suite {


  def testParseArgsIntoLists() {

    // this is how i solved the problem of wanting to reuse these val names, runpathList, reportersList, etc.
    // by putting them in a little verify method, it gets reused each time i call that method
    def verify(
      args: Array[String],
      expectedRunpathList: List[String],
      expectedReporterList: List[String],
      expectedSuitesList: List[String],
      expectedPropsList: List[String],
      expectedIncludesList: List[String],
      expectedExcludesList: List[String],
      expectedConcurrentList: List[String],
      expectedMemberOfList: List[String],
      expectedBeginsWithList: List[String]
    ) = {

      val (runpathList, reportersList, suitesList, propsList, includesList, excludesList, concurrentList, memberOfList, beginsWithList) = Runner.parseArgs(args)

      assert(runpathList === expectedRunpathList)
      assert(reportersList === expectedReporterList)
      assert(suitesList === expectedSuitesList)
      assert(propsList === expectedPropsList)
      assert(includesList === expectedIncludesList)
      assert(excludesList === expectedExcludesList)
      assert(concurrentList === expectedConcurrentList)
      assert(memberOfList === expectedMemberOfList)
      assert(beginsWithList === expectedBeginsWithList)
    }

    verify(
      Array("-g", "-Dincredible=whatshername", "-Ddbname=testdb", "-Dserver=192.168.1.188", "-p",
            "\"serviceuitest-1.1beta4.jar myjini http://myhost:9998/myfile.jar\"", "-g", "-f", "file.out", "-p"),
      List("-p", "\"serviceuitest-1.1beta4.jar myjini http://myhost:9998/myfile.jar\"", "-p"),
      List("-g", "-g", "-f", "file.out"),
      Nil,
      List("-Dincredible=whatshername", "-Ddbname=testdb", "-Dserver=192.168.1.188"),
      Nil,
      Nil,
      Nil,
      Nil,
      Nil
    )

    verify(
      Array("-g", "-Dincredible=whatshername", "-Ddbname=testdb", "-Dserver=192.168.1.188", "-p",
            "\"serviceuitest-1.1beta4.jar myjini http://myhost:9998/myfile.jar\"", "-g", "-f", "file.out",
            "-s", "SuiteOne", "-s", "SuiteTwo"),
      List("-p", "\"serviceuitest-1.1beta4.jar myjini http://myhost:9998/myfile.jar\""),
      List("-g", "-g", "-f", "file.out"),
      List("-s", "SuiteOne", "-s", "SuiteTwo"),
      List("-Dincredible=whatshername", "-Ddbname=testdb", "-Dserver=192.168.1.188"),
      Nil,
      Nil,
      Nil,
      Nil,
      Nil
    )

    verify(
      Array(),
      Nil,
      Nil,
      Nil,
      Nil,
      Nil,
      Nil,
      Nil,
      Nil,
      Nil
    )

    verify(
      Array("-g", "-Dincredible=whatshername", "-Ddbname=testdb", "-Dserver=192.168.1.188", "-p",
            "\"serviceuitest-1.1beta4.jar myjini http://myhost:9998/myfile.jar\"", "-g", "-f", "file.out",
            "-n", "JustOne", "-s", "SuiteOne", "-s", "SuiteTwo"),
      List("-p", "\"serviceuitest-1.1beta4.jar myjini http://myhost:9998/myfile.jar\""),
      List("-g", "-g", "-f", "file.out"),
      List("-s", "SuiteOne", "-s", "SuiteTwo"),
      List("-Dincredible=whatshername", "-Ddbname=testdb", "-Dserver=192.168.1.188"),
      List("-n", "JustOne"),
      Nil,
      Nil,
      Nil,
      Nil
    )

    verify(
      Array("-g", "-Dincredible=whatshername", "-Ddbname=testdb", "-Dserver=192.168.1.188", "-p",
            "\"serviceuitest-1.1beta4.jar myjini http://myhost:9998/myfile.jar\"", "-g", "-f", "file.out",
            "-n", "One Two Three", "-x", "SlowTests", "-s", "SuiteOne", "-s", "SuiteTwo"),
      List("-p", "\"serviceuitest-1.1beta4.jar myjini http://myhost:9998/myfile.jar\""),
      List("-g", "-g", "-f", "file.out"),
      List("-s", "SuiteOne", "-s", "SuiteTwo"),
      List("-Dincredible=whatshername", "-Ddbname=testdb", "-Dserver=192.168.1.188"),
      List("-n", "One Two Three"),
      List("-x", "SlowTests"),
      Nil,
      Nil,
      Nil
    )

    verify(
      Array("-c", "-g", "-Dincredible=whatshername", "-Ddbname=testdb", "-Dserver=192.168.1.188", "-p",
            "\"serviceuitest-1.1beta4.jar myjini http://myhost:9998/myfile.jar\"", "-g", "-f", "file.out",
            "-n", "One Two Three", "-x", "SlowTests", "-s", "SuiteOne", "-s", "SuiteTwo"),
      List("-p", "\"serviceuitest-1.1beta4.jar myjini http://myhost:9998/myfile.jar\""),
      List("-g", "-g", "-f", "file.out"),
      List("-s", "SuiteOne", "-s", "SuiteTwo"),
      List("-Dincredible=whatshername", "-Ddbname=testdb", "-Dserver=192.168.1.188"),
      List("-n", "One Two Three"),
      List("-x", "SlowTests"),
      List("-c"),
      Nil,
      Nil
    )

    verify(
      Array("-c", "-g", "-Dincredible=whatshername", "-Ddbname=testdb", "-Dserver=192.168.1.188", "-p",
          "\"serviceuitest-1.1beta4.jar myjini http://myhost:9998/myfile.jar\"", "-g", "-f", "file.out",
          "-n", "One Two Three", "-x", "SlowTests", "-s", "SuiteOne", "-s", "SuiteTwo", "-m", "com.example.webapp",
          "-w", "com.example.root"),
      List("-p", "\"serviceuitest-1.1beta4.jar myjini http://myhost:9998/myfile.jar\""),
      List("-g", "-g", "-f", "file.out"),
      List("-s", "SuiteOne", "-s", "SuiteTwo"),
      List("-Dincredible=whatshername", "-Ddbname=testdb", "-Dserver=192.168.1.188"),
      List("-n", "One Two Three"),
      List("-x", "SlowTests"),
      List("-c"),
      List("-m", "com.example.webapp"),
      List("-w", "com.example.root")
    )
  }

  def testParseCompoundArgIntoSet() {
    expect(Set("Cat", "Dog")) {
      Runner.parseCompoundArgIntoSet(List("-n", "Cat Dog"), "-n")
    }
  }

  def testParseConfigSet() {

    intercept(classOf[NullPointerException]) {
      Runner.parseConfigSet(null)
    }
    intercept(classOf[IllegalArgumentException]) {
      Runner.parseConfigSet("-fX")
    }
    intercept(classOf[IllegalArgumentException]) {
      Runner.parseConfigSet("-oYZTFUPBISARG-")
    }
    intercept(classOf[IllegalArgumentException]) {
      Runner.parseConfigSet("-")
    }
    intercept(classOf[IllegalArgumentException]) {
      Runner.parseConfigSet("")
    }

    expect(ReporterOpts.Set32(ReporterOpts.PresentRunStarting.mask32)) {
      Runner.parseConfigSet("-oY")
    }
    expect(ReporterOpts.Set32(ReporterOpts.PresentTestStarting.mask32)) {
      Runner.parseConfigSet("-oZ")
    }
    expect(ReporterOpts.Set32(ReporterOpts.PresentTestSucceeded.mask32)) {
      Runner.parseConfigSet("-oT")
    }
    expect(ReporterOpts.Set32(ReporterOpts.PresentTestFailed.mask32)) {
      Runner.parseConfigSet("-oF")
    }
    expect(ReporterOpts.Set32(ReporterOpts.PresentSuiteStarting.mask32)) {
      Runner.parseConfigSet("-oU")
    }
    expect(ReporterOpts.Set32(ReporterOpts.PresentSuiteCompleted.mask32)) {
      Runner.parseConfigSet("-oP")
    }
    expect(ReporterOpts.Set32(ReporterOpts.PresentSuiteAborted.mask32)) {
      Runner.parseConfigSet("-oB")
    }
    expect(ReporterOpts.Set32(ReporterOpts.PresentInfoProvided.mask32)) {
      Runner.parseConfigSet("-oI")
    }
    expect(ReporterOpts.Set32(ReporterOpts.PresentRunStopped.mask32)) {
      Runner.parseConfigSet("-oS")
    }
    expect(ReporterOpts.Set32(ReporterOpts.PresentRunAborted.mask32)) {
      Runner.parseConfigSet("-oA")
    }
    expect(ReporterOpts.Set32(ReporterOpts.PresentRunCompleted.mask32)) {
      Runner.parseConfigSet("-oR")
    }
    expect(ReporterOpts.Set32(ReporterOpts.PresentTestIgnored.mask32)) {
      Runner.parseConfigSet("-oG")
    }
    expect(ReporterOpts.Set32(0)) {
      Runner.parseConfigSet("-f")
    }

    expect(ReporterOpts.Set32(ReporterOpts.PresentRunStarting.mask32 | ReporterOpts.PresentTestStarting.mask32)) {
      Runner.parseConfigSet("-oZY")
    }
    expect(ReporterOpts.Set32(ReporterOpts.PresentRunStarting.mask32 | ReporterOpts.PresentTestStarting.mask32)) {
      Runner.parseConfigSet("-oYZ") // Just reverse the order of the params
    }
    val allOpts = ReporterOpts.Set32(
      ReporterOpts.PresentRunStarting.mask32 |
      ReporterOpts.PresentTestStarting.mask32 |
      ReporterOpts.PresentTestSucceeded.mask32 |
      ReporterOpts.PresentTestFailed.mask32 |
      ReporterOpts.PresentSuiteStarting.mask32 |
      ReporterOpts.PresentSuiteCompleted.mask32 |
      ReporterOpts.PresentSuiteAborted.mask32 |
      ReporterOpts.PresentInfoProvided.mask32 |
      ReporterOpts.PresentRunStopped.mask32 |
      ReporterOpts.PresentRunAborted.mask32 |
      ReporterOpts.PresentRunCompleted.mask32 |
      ReporterOpts.PresentTestIgnored.mask32
    )
    expect(allOpts) {
      Runner.parseConfigSet("-oYZTFUPBISARG")
    }
  }

  def testParseReporterArgsIntoSpecs() {
    intercept(classOf[NullPointerException]) {
      Runner.parseReporterArgsIntoSpecs(null)
    }
    intercept(classOf[NullPointerException]) {
      Runner.parseReporterArgsIntoSpecs(List("Hello", null, "World"))
    }
    intercept(classOf[IllegalArgumentException]) {
      Runner.parseReporterArgsIntoSpecs(List("Hello", "-", "World"))
    }
    intercept(classOf[IllegalArgumentException]) {
      Runner.parseReporterArgsIntoSpecs(List("Hello", "", "World"))
    }
    intercept(classOf[IllegalArgumentException]) {
      Runner.parseReporterArgsIntoSpecs(List("-g", "-x", "-o"))
    }
    intercept(classOf[IllegalArgumentException]) {
      Runner.parseReporterArgsIntoSpecs(List("Hello", " there", " world!"))
    }
    intercept(classOf[IllegalArgumentException]) {
      Runner.parseReporterArgsIntoSpecs(List("-g", "-o", "-g", "-e"))
    }
    intercept(classOf[IllegalArgumentException]) {
      Runner.parseReporterArgsIntoSpecs(List("-o", "-o", "-g", "-e"))
    }
    intercept(classOf[IllegalArgumentException]) {
      Runner.parseReporterArgsIntoSpecs(List("-e", "-o", "-g", "-e"))
    }
    intercept(classOf[IllegalArgumentException]) {
      Runner.parseReporterArgsIntoSpecs(List("-f")) // Can't have -f last, because need a file name
    }
    intercept(classOf[IllegalArgumentException]) {
      Runner.parseReporterArgsIntoSpecs(List("-r")) // Can't have -r last, because need a reporter class
    }
    expect(new ReporterSpecs(None, Nil, None, None, Nil)) {
      Runner.parseReporterArgsIntoSpecs(Nil)
    }
    expect(new ReporterSpecs(Some(new GraphicReporterSpec(ReporterOpts.Set32(0))), Nil, None, None, Nil)) {
      Runner.parseReporterArgsIntoSpecs(List("-g"))
    }
    expect(new ReporterSpecs(Some(new GraphicReporterSpec(ReporterOpts.Set32(ReporterOpts.PresentTestFailed.mask32))), Nil, None, None, Nil)) {
      Runner.parseReporterArgsIntoSpecs(List("-gF"))
    }
    expect(new ReporterSpecs(None, Nil, Some(new StandardOutReporterSpec(ReporterOpts.Set32(0))), None, Nil)) {
      Runner.parseReporterArgsIntoSpecs(List("-o"))
    }
    expect(new ReporterSpecs(None, Nil, Some(new StandardOutReporterSpec(ReporterOpts.Set32(ReporterOpts.PresentTestFailed.mask32))), None, Nil)) {
      Runner.parseReporterArgsIntoSpecs(List("-oF"))
    }
    expect(new ReporterSpecs(None, Nil, None, Some(new StandardErrReporterSpec(ReporterOpts.Set32(0))), Nil)) {
      Runner.parseReporterArgsIntoSpecs(List("-e"))
    }
    expect(new ReporterSpecs(None, Nil, None, Some(new StandardErrReporterSpec(ReporterOpts.Set32(ReporterOpts.PresentTestFailed.mask32))), Nil)) {
      Runner.parseReporterArgsIntoSpecs(List("-eF"))
    }
    expect(new ReporterSpecs(None, List(new FileReporterSpec(ReporterOpts.Set32(0), "theFilename")), None, None, Nil)) {
      Runner.parseReporterArgsIntoSpecs(List("-f", "theFilename"))
    }
    expect(new ReporterSpecs(None, List(new FileReporterSpec(ReporterOpts.Set32(ReporterOpts.PresentTestFailed.mask32), "theFilename")), None, None, Nil)) {
      Runner.parseReporterArgsIntoSpecs(List("-fF", "theFilename"))
    }
    expect(new ReporterSpecs(None, Nil, None, None, List(new CustomReporterSpec(ReporterOpts.Set32(0), "the.reporter.Class")))) {
      Runner.parseReporterArgsIntoSpecs(List("-r", "the.reporter.Class"))
    }
    expect(new ReporterSpecs(None, Nil, None, None, List(new CustomReporterSpec(ReporterOpts.Set32(ReporterOpts.PresentTestFailed.mask32), "the.reporter.Class")))) {
      Runner.parseReporterArgsIntoSpecs(List("-rF", "the.reporter.Class"))
    }
  }

  def testParseSuiteArgsIntoClassNameStrings() {
    intercept(classOf[NullPointerException]) {
      Runner.parseSuiteArgsIntoNameStrings(null, "-s")
    }
    intercept(classOf[NullPointerException]) {
      Runner.parseSuiteArgsIntoNameStrings(List("-s", null, "-s"), "-s")
    }
    intercept(classOf[IllegalArgumentException]) {
      Runner.parseSuiteArgsIntoNameStrings(List("-s", "SweetSuite", "-s"), "-s")
    }
    intercept(classOf[IllegalArgumentException]) {
      Runner.parseSuiteArgsIntoNameStrings(List("-s", "SweetSuite", "-s", "-s"), "-s")
    }
    expect(List("SweetSuite", "OKSuite")) {
      Runner.parseSuiteArgsIntoNameStrings(List("-s", "SweetSuite", "-s", "OKSuite"), "-s")
    }
    expect(List("SweetSuite", "OKSuite", "SomeSuite")) {
      Runner.parseSuiteArgsIntoNameStrings(List("-s", "SweetSuite", "-s", "OKSuite", "-s", "SomeSuite"), "-s")
    }
  }

  def testParseRunpathArgIntoList() {
    intercept(classOf[NullPointerException]) {
      Runner.parseRunpathArgIntoList(null)
    }
    intercept(classOf[NullPointerException]) {
      Runner.parseRunpathArgIntoList(List("-p", null))
    }
    intercept(classOf[NullPointerException]) {
      Runner.parseRunpathArgIntoList(List(null, "serviceuitest-1.1beta4.jar myjini http://myhost:9998/myfile.jar"))
    }
    intercept(classOf[IllegalArgumentException]) {
      Runner.parseRunpathArgIntoList(List("-p"))
    }
    intercept(classOf[IllegalArgumentException]) {
      Runner.parseRunpathArgIntoList(List("-p", "bla", "bla"))
    }
    intercept(classOf[IllegalArgumentException]) {
      Runner.parseRunpathArgIntoList(List("-pX", "bla"))
    }
    intercept(classOf[IllegalArgumentException]) {
      Runner.parseRunpathArgIntoList(List("-p", "  "))
    }
    intercept(classOf[IllegalArgumentException]) {
      Runner.parseRunpathArgIntoList(List("-p", "\t"))
    }
    expect(List("bla")) {
      Runner.parseRunpathArgIntoList(List("-p", "bla"))
    }
    expect(List("bla", "bla", "bla")) {
      Runner.parseRunpathArgIntoList(List("-p", "bla bla bla"))
    }
    expect(List("serviceuitest-1.1beta4.jar", "myjini", "http://myhost:9998/myfile.jar")) {
      Runner.parseRunpathArgIntoList(List("-p", "serviceuitest-1.1beta4.jar myjini http://myhost:9998/myfile.jar"))
    }
  }

  def testParsePropertiesArgsIntoMap() {
    intercept(classOf[NullPointerException]) {
      Runner.parsePropertiesArgsIntoMap(null)
    }
    intercept(classOf[NullPointerException]) {
      Runner.parsePropertiesArgsIntoMap(List("-Da=b", null))
    }
    intercept(classOf[IllegalArgumentException]) {
      Runner.parsePropertiesArgsIntoMap(List("-Dab")) // = sign missing
    }
    intercept(classOf[IllegalArgumentException]) {
      Runner.parsePropertiesArgsIntoMap(List("ab")) // needs to start with -D
    }
    intercept(classOf[IllegalArgumentException]) {
      Runner.parsePropertiesArgsIntoMap(List("-D=ab")) // no key
    }
    intercept(classOf[IllegalArgumentException]) {
      Runner.parsePropertiesArgsIntoMap(List("-Dab=")) // no value
    }
    expect(Map("a" -> "b", "cat" -> "dog", "Glorp" -> "Glib")) {
      Runner.parsePropertiesArgsIntoMap(List("-Da=b", "-Dcat=dog", "-DGlorp=Glib"))
    }
  }

  def testCheckArgsForValidity() {
    intercept(classOf[NullPointerException]) {
      Runner.checkArgsForValidity(null)
    }
    expect(None) {
      Runner.checkArgsForValidity(Array("-Ddbname=testdb", "-Dserver=192.168.1.188", "-p", "serviceuitest-1.1beta4.jar", "-g", "-eFBA", "-s", "MySuite"))
    }
    assert(Runner.checkArgsForValidity(Array("-Ddbname=testdb", "-Dserver=192.168.1.188", "-z", "serviceuitest-1.1beta4.jar", "-g", "-eFBA", "-s", "MySuite")) != None)
    expect(None) {
      Runner.checkArgsForValidity(Array("-Ddbname=testdb", "-Dserver=192.168.1.188", "-p", "serviceuitest-1.1beta4.jar", "-g", "-eFBA", "-s", "MySuite", "-c"))
    }
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
