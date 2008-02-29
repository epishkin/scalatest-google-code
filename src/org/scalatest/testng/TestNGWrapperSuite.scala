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


class TestNGWrapperSuite(xmlSuitesPropertyName: String) extends TestNGSuite{
  
  if( getSuites == null ) throw new IllegalArgumentException("no property: " + xmlSuitesPropertyName )
  
  def getSuites: String = System.getProperty(xmlSuitesPropertyName)
  
  override def execute(testName: Option[String], reporter: Reporter, stopper: Stopper, includes: Set[String], 
      excludes: Set[String], properties: Map[String, Any], distributor: Option[Distributor]) {
    
    runTestNG(reporter, includes, excludes);
  }
  
  override private[testng] def runTestNG(reporter: Reporter) : TestListenerAdapter = {
    runTestNG( reporter, Set(), Set() )
  }
  
  private[testng] def runTestNG(reporter: Reporter, groupsToInclude: Set[String], 
      groupsToExclude: Set[String]) : TestListenerAdapter = {
    
    val testng = new TestNG()
    handleGroups( groupsToInclude, groupsToExclude, testng )
    handleXmlSuites( testng )
    
    run( testng, reporter )
  }
  
  private def handleXmlSuites( testng: TestNG ){
    import java.io.File
    import java.io.FileNotFoundException
    
    val files = new java.util.ArrayList[String]
    
    getSuites.split(",").foreach( { name => 
        val f = new File( name )
        if( ! f.exists ) throw new FileNotFoundException( f.getAbsolutePath )
        files add name
      } 
    )
    testng.setTestSuites(files)
  }
  
  
}
