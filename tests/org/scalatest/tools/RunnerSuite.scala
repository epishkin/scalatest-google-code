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
      expectedBeginsWithList: List[String],
      expectedTestNGList: List[String]
    ) = {

      val (
        runpathList,
        reportersList,
        suitesList,
        propsList,
        includesList,
        excludesList,
        concurrentList,
        memberOfList,
        beginsWithList,
        testNGList
      ) = Runner.parseArgs(args)

      assert(runpathList === expectedRunpathList)
      assert(reportersList === expectedReporterList)
      assert(suitesList === expectedSuitesList)
      assert(propsList === expectedPropsList)
      assert(includesList === expectedIncludesList)
      assert(excludesList === expectedExcludesList)
      assert(concurrentList === expectedConcurrentList)
      assert(memberOfList === expectedMemberOfList)
      assert(beginsWithList === expectedBeginsWithList)
      assert(testNGList === expectedTestNGList)
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
      List("-w", "com.example.root"),
      Nil
    )
    // Try a TestNGSuite
    verify(
      Array("-c", "-g", "-Dincredible=whatshername", "-Ddbname=testdb", "-Dserver=192.168.1.188", "-p",
          "\"serviceuitest-1.1beta4.jar myjini http://myhost:9998/myfile.jar\"", "-g", "-f", "file.out",
          "-n", "One Two Three", "-x", "SlowTests", "-s", "SuiteOne", "-s", "SuiteTwo", "-m", "com.example.webapp",
          "-w", "com.example.root", "-t", "some/path/file.xml"),
      List("-p", "\"serviceuitest-1.1beta4.jar myjini http://myhost:9998/myfile.jar\""),
      List("-g", "-g", "-f", "file.out"),
      List("-s", "SuiteOne", "-s", "SuiteTwo"),
      List("-Dincredible=whatshername", "-Ddbname=testdb", "-Dserver=192.168.1.188"),
      List("-n", "One Two Three"),
      List("-x", "SlowTests"),
      List("-c"),
      List("-m", "com.example.webapp"),
      List("-w", "com.example.root"),
      List("-t", "some/path/file.xml")
    )
  }

  def testParseCompoundArgIntoSet() {
    expect(Set("Cat", "Dog")) {
      Runner.parseCompoundArgIntoSet(List("-n", "Cat Dog"), "-n")
    }
  }

  def testParseConfigSet() {

    intercept[NullPointerException] {
      Runner.parseConfigSet(null)
    }
    intercept[IllegalArgumentException] {
      Runner.parseConfigSet("-fX")
    }
    intercept[IllegalArgumentException] {
      Runner.parseConfigSet("-oYZTFUPBISARG-")
    }
    intercept[IllegalArgumentException] {
      Runner.parseConfigSet("-")
    }
    intercept[IllegalArgumentException] {
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
    intercept[NullPointerException] {
      Runner.parseReporterArgsIntoConfigurations(null)
    }
    intercept[NullPointerException] {
      Runner.parseReporterArgsIntoConfigurations(List("Hello", null, "World"))
    }
    intercept[IllegalArgumentException] {
      Runner.parseReporterArgsIntoConfigurations(List("Hello", "-", "World"))
    }
    intercept[IllegalArgumentException] {
      Runner.parseReporterArgsIntoConfigurations(List("Hello", "", "World"))
    }
    intercept[IllegalArgumentException] {
      Runner.parseReporterArgsIntoConfigurations(List("-g", "-x", "-o"))
    }
    intercept[IllegalArgumentException] {
      Runner.parseReporterArgsIntoConfigurations(List("Hello", " there", " world!"))
    }
    intercept[IllegalArgumentException] {
      Runner.parseReporterArgsIntoConfigurations(List("-g", "-o", "-g", "-e"))
    }
    intercept[IllegalArgumentException] {
      Runner.parseReporterArgsIntoConfigurations(List("-o", "-o", "-g", "-e"))
    }
    intercept[IllegalArgumentException] {
      Runner.parseReporterArgsIntoConfigurations(List("-e", "-o", "-g", "-e"))
    }
    intercept[IllegalArgumentException] {
      Runner.parseReporterArgsIntoConfigurations(List("-f")) // Can't have -f last, because need a file name
    }
    intercept[IllegalArgumentException] {
      Runner.parseReporterArgsIntoConfigurations(List("-r")) // Can't have -r last, because need a reporter class
    }
    expect(new ReporterConfigurations(None, Nil, None, None, Nil)) {
      Runner.parseReporterArgsIntoConfigurations(Nil)
    }
    expect(new ReporterConfigurations(Some(new GraphicReporterConfiguration(ReporterOpts.Set32(0))), Nil, None, None, Nil)) {
      Runner.parseReporterArgsIntoConfigurations(List("-g"))
    }
    expect(new ReporterConfigurations(Some(new GraphicReporterConfiguration(ReporterOpts.Set32(ReporterOpts.PresentTestFailed.mask32))), Nil, None, None, Nil)) {
      Runner.parseReporterArgsIntoConfigurations(List("-gF"))
    }
    expect(new ReporterConfigurations(None, Nil, Some(new StandardOutReporterConfiguration(ReporterOpts.Set32(0))), None, Nil)) {
      Runner.parseReporterArgsIntoConfigurations(List("-o"))
    }
    expect(new ReporterConfigurations(None, Nil, Some(new StandardOutReporterConfiguration(ReporterOpts.Set32(ReporterOpts.PresentTestFailed.mask32))), None, Nil)) {
      Runner.parseReporterArgsIntoConfigurations(List("-oF"))
    }
    expect(new ReporterConfigurations(None, Nil, None, Some(new StandardErrReporterConfiguration(ReporterOpts.Set32(0))), Nil)) {
      Runner.parseReporterArgsIntoConfigurations(List("-e"))
    }
    expect(new ReporterConfigurations(None, Nil, None, Some(new StandardErrReporterConfiguration(ReporterOpts.Set32(ReporterOpts.PresentTestFailed.mask32))), Nil)) {
      Runner.parseReporterArgsIntoConfigurations(List("-eF"))
    }
    expect(new ReporterConfigurations(None, List(new FileReporterConfiguration(ReporterOpts.Set32(0), "theFilename")), None, None, Nil)) {
      Runner.parseReporterArgsIntoConfigurations(List("-f", "theFilename"))
    }
    expect(new ReporterConfigurations(None, List(new FileReporterConfiguration(ReporterOpts.Set32(ReporterOpts.PresentTestFailed.mask32), "theFilename")), None, None, Nil)) {
      Runner.parseReporterArgsIntoConfigurations(List("-fF", "theFilename"))
    }
    expect(new ReporterConfigurations(None, Nil, None, None, List(new CustomReporterConfiguration(ReporterOpts.Set32(0), "the.reporter.Class")))) {
      Runner.parseReporterArgsIntoConfigurations(List("-r", "the.reporter.Class"))
    }
    expect(new ReporterConfigurations(None, Nil, None, None, List(new CustomReporterConfiguration(ReporterOpts.Set32(ReporterOpts.PresentTestFailed.mask32), "the.reporter.Class")))) {
      Runner.parseReporterArgsIntoConfigurations(List("-rF", "the.reporter.Class"))
    }
  }

  def testParseSuiteArgsIntoClassNameStrings() {
    intercept[NullPointerException] {
      Runner.parseSuiteArgsIntoNameStrings(null, "-s")
    }
    intercept[NullPointerException] {
      Runner.parseSuiteArgsIntoNameStrings(List("-s", null, "-s"), "-s")
    }
    intercept[IllegalArgumentException] {
      Runner.parseSuiteArgsIntoNameStrings(List("-s", "SweetSuite", "-s"), "-s")
    }
    intercept[IllegalArgumentException] {
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
    intercept[NullPointerException] {
      Runner.parseRunpathArgIntoList(null)
    }
    intercept[NullPointerException] {
      Runner.parseRunpathArgIntoList(List("-p", null))
    }
    intercept[NullPointerException] {
      Runner.parseRunpathArgIntoList(List(null, "serviceuitest-1.1beta4.jar myjini http://myhost:9998/myfile.jar"))
    }
    intercept[IllegalArgumentException] {
      Runner.parseRunpathArgIntoList(List("-p"))
    }
    intercept[IllegalArgumentException] {
      Runner.parseRunpathArgIntoList(List("-p", "bla", "bla"))
    }
    intercept[IllegalArgumentException] {
      Runner.parseRunpathArgIntoList(List("-pX", "bla"))
    }
    intercept[IllegalArgumentException] {
      Runner.parseRunpathArgIntoList(List("-p", "  "))
    }
    intercept[IllegalArgumentException] {
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
    intercept[NullPointerException] {
      Runner.parsePropertiesArgsIntoMap(null)
    }
    intercept[NullPointerException] {
      Runner.parsePropertiesArgsIntoMap(List("-Da=b", null))
    }
    intercept[IllegalArgumentException] {
      Runner.parsePropertiesArgsIntoMap(List("-Dab")) // = sign missing
    }
    intercept[IllegalArgumentException] {
      Runner.parsePropertiesArgsIntoMap(List("ab")) // needs to start with -D
    }
    intercept[IllegalArgumentException] {
      Runner.parsePropertiesArgsIntoMap(List("-D=ab")) // no key
    }
    intercept[IllegalArgumentException] {
      Runner.parsePropertiesArgsIntoMap(List("-Dab=")) // no value
    }
    expect(Map("a" -> "b", "cat" -> "dog", "Glorp" -> "Glib")) {
      Runner.parsePropertiesArgsIntoMap(List("-Da=b", "-Dcat=dog", "-DGlorp=Glib"))
    }
  }

  def testCheckArgsForValidity() {
    intercept[NullPointerException] {
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
  def testRunpathPropertyAddedToPropertiesMap() {
    val a = new Suite {
      var theProperties: Map[String, Any] = Map()
      override def execute(testName: Option[String], reporter: Reporter, stopper: Stopper, includes: Set[String], excludes: Set[String],
          properties: Map[String, Any], distributor: Option[Distributor]) {
        theProperties = properties
      }
    }

    val dispatchReporter = new DispatchReporter(Nil, System.out)
    val suitesList = List("org.scalatest.usefulstuff.RunpathPropCheckerSuite")

    // Runner.doRunRunRunADoRunRun(new DispatchReporter)
    // Runner.doRunRunRunADoRunRun(dispatchReporter, suitesList, new Stopper {}, Set(), Set(), Map(), false,
         List(), List(), runpath: "build_tests", loader: ClassLoader,
      doneListener: RunDoneListener) = {

    ()
  }
}

package org.scalatest.usefulstuff {

  class RunpathPropCheckerSuite extends Suite {
    var theProperties: Map[String, Any] = Map()
    override def execute(testName: Option[String], reporter: Reporter, stopper: Stopper, includes: Set[String], excludes: Set[String],
        properties: Map[String, Any], distributor: Option[Distributor]) {
      theProperties = properties
    }
  }
*/
}
