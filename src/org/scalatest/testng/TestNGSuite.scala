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
package org.scalatest.testng;

import org.scalatest.Suite
import org.scalatest.Report
import org.scalatest.TestRerunner

import org.testng.internal.annotations.ITest
import org.testng.internal.annotations.IAnnotationTransformer
import org.testng.TestNG
import org.testng.ITestResult
import org.testng.TestListenerAdapter
import java.lang.reflect.Method
import java.lang.reflect.Constructor

trait TestNGSuite extends Suite{

  override def execute(testName: Option[String], reporter: Reporter, stopper: Stopper, includes: Set[String], 
      excludes: Set[String], properties: Map[String, Any], distributor: Option[Distributor]) {
    
    runTestNG(testName, reporter, includes, excludes);
    //super.execute(XtestName, Xreporter, stopper, Xincludes, Xexcludes, properties, distributor)
  }
  
  private[testng] def runTestNG(reporter: Reporter) : TestListenerAdapter = {
    runTestNG( None, reporter, Set(), Set() )
  }
 
  private[testng] def runTestNG(testName: String, reporter: Reporter) : TestListenerAdapter = {
    runTestNG( Some(testName), reporter, Set(), Set() )
  }
  
  
  private[testng] def runTestNG(testName: Option[String], reporter: Reporter, groupsToInclude: Set[String], 
      groupsToExclude: Set[String]) : TestListenerAdapter = {
    
    val testng = new TestNG()
    testng.setTestClasses(Array(this.getClass))
    
    
    testName match {
      case Some(tn) => handleGroupsForRunningSingleMethod(tn, testng)
      case None => handleGroups( groupsToInclude, groupsToExclude, testng )
    }

    this.run(testng, reporter)
    
  }
  
  private[testng] def run( testng: TestNG, reporter: Reporter ): TestListenerAdapter = {
    val tla = new MyTestListenerAdapter(reporter)
    testng.addListener(tla)
    testng.run()

    tla
  }
  
  
  private[testng] def handleGroups( groupsToInclude: Set[String], groupsToExclude: Set[String], testng: TestNG){
    testng.setGroups(groupsToInclude.mkString(","))
    testng.setExcludedGroups(groupsToExclude.mkString(","))
  }
  
  private def handleGroupsForRunningSingleMethod( testName: String, testng: TestNG ) = {
    
    class MyTransformer extends IAnnotationTransformer {
      override def transform( annotation: ITest, testClass: Class, testConstructor: Constructor, testMethod: Method){
        if (testName.equals(testMethod.getName)) {
          annotation.setGroups(Array("org.scalatest.testng.singlemethodrun.methodname"))  
        }
      }
    }
    testng.setGroups("org.scalatest.testng.singlemethodrun.methodname")
    testng.setAnnotationTransformer(new MyTransformer())
  }
  

  private[testng] class MyTestListenerAdapter( reporter: Reporter ) extends TestListenerAdapter{
    
    val className = TestNGSuite.this.getClass.getName
    
    override def onTestStart(result: ITestResult) = {
      reporter.testStarting( buildReport( result, None ) )
    }
    
    override def onTestSuccess(itr: ITestResult) = {
      reporter.testSucceeded( buildReport( itr, None ) )
    }
    
    override def onTestFailure(itr: ITestResult) = {
      reporter.testFailed( buildReport( itr, Some(itr.getThrowable)) )
    }
    
    override def onTestSkipped(itr: ITestResult) = {
      reporter.testIgnored( buildReport( itr, None) )
    }

    override def onConfigurationFailure(itr: ITestResult) = {
      reporter.testFailed( new Report(itr.getName, className, Some(itr.getThrowable), None) )
      //reporter.infoProvided( new Report(itr.getName, className, Some(itr.getThrowable), None) )
    }

    override def onConfigurationSuccess(itr: ITestResult) = {
      reporter.infoProvided( new Report(itr.getName, className) )
    }
    
    private def buildReport( itr: ITestResult, t: Option[Throwable] ): Report = {
      new Report(itr.getName, className, t, Some(new TestRerunner(className, itr.getName)) )
    }
  }
  
  
  
  /**
     TODO
    (12:02:27 AM) bvenners: onTestFailedButWithinSuccessPercentage(ITestResult tr) 
    (12:02:34 AM) bvenners: maybe a testSucceeded with some extra info in the report
    (12:02:49 AM) bvenners: onStart and onFinish are starting and finishing what, a run?
    (12:02:57 AM) bvenners: if so then runStarting and runCompleted
    **/    
    
      
   /**
    val xmlSuite = new XmlSuite()
    val xmlClass = new XmlClass(this.getClass.getName)
    //xmlClass.setIncludedMethods(java.util.Collections.singletonList("testWithAssertFail"))
    val xmlTest = new XmlTest(xmlSuite)
    xmlTest.setXmlClasses(java.util.Collections.singletonList(xmlClass))
    println(xmlSuite.toXml())
    testng.setXmlSuites(java.util.Collections.singletonList(xmlSuite))
    **/
  
}
