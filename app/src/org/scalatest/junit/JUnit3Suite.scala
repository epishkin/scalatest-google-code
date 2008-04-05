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
package org.scalatest.junit;

import org.scalatest.Suite
import org.scalatest.Report

import _root_.junit.framework._
import _root_.junit.textui._

/**
 * @author Josh Cough
 */
class JUnit3Suite extends TestCase with Suite {

  
  /**
   * Runs JUnit. <br>
   * 
   * @param   testName   If present (Some), then only the method with the supplied name is executed and groups will be ignored.
   * @param   reporter	 The reporter to be notified of test events (success, failure, etc).
   * @param   groupsToInclude	Contains the names of groups to run. Only tests in these groups will be executed.
   * @param   groupsToExclude	Tests in groups in this Set will not be executed.
   *
   * @param   stopper    	Currently Ignored.
   * @param   properties	Currently ignored. see note above...should we be ignoring these?
   * @param   distributor	Currently ignored.
   */
  override def execute(testName: Option[String], reporter: Reporter, stopper: Stopper, includes: Set[String], 
      excludes: Set[String], properties: Map[String, Any], distributor: Option[Distributor]) {
    
    runJUnit(testName, reporter, includes, excludes);
  }
  

  /**
   * Runs JUnit. The meat and potatoes. 
   *
   * @param   testName   if present (Some), then only the method with the supplied name is executed and groups will be ignored
   * @param   reporter   the reporter to be notified of test events (success, failure, etc)
   * @param   groupsToInclude    contains the names of groups to run. only tests in these groups will be executed
   * @param   groupsToExclude    tests in groups in this Set will not be executed
   */  
  private def runJUnit(testName: Option[String], reporter: Reporter, groupsToInclude: Set[String], groupsToExclude: Set[String]) {
    runJUnit(reporter)
  }   
   
  private def buildReport( testName: String, t: Option[Throwable] ): Report = {
    new Report(testName, this.getClass.getName, t, None )
  }
  
  private[junit] def runJUnit(reporter: Reporter) = {
    reporter.suiteStarting( buildReport( this.getClass.getName, None ) ) 
    testNames.foreach(runSingleTest( _, reporter))
    //new TestSuite(this.getClass).run(new MyTestResult(reporter))
    reporter.suiteCompleted( buildReport( this.getClass.getName, None ) )
  }
  
  private def runSingleTest( testName: String, reporter: Reporter ) = {
    this.setName(testName)
    this.run(new MyTestResult(reporter))
  }
   
  private class MyTestResult(reporter: Reporter) extends TestResult {

    override def addFailure(test: Test, t: AssertionFailedError) = {
      super.addFailure(test, t)
      reporter.testFailed( buildReport( test, Some(t) ) ) 
    }

    override def addError(test: Test, t: Throwable) = {
      super.addError(test, t)
      reporter.testFailed( buildReport( test, Some(t) ) )
    }
    
    override def startTest(test: Test) = {
      super.startTest(test)
      reporter.testStarting( buildReport( test, None ) )
    }
    
    override def endTest(test: Test) = {
      super.endTest(test)
      if( this.wasSuccessful ) reporter.testSucceeded( buildReport( test, None ) ) 
    }
    
    implicit def testToString(t: Test) = JUnitVersionHelper.getTestCaseName(t)
  }
  

}
