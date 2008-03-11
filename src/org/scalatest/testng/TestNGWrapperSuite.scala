
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

import org.testng.TestNG
import org.testng.TestListenerAdapter

/**
 * <p>
 * Extending <code>TestNGWrapperSuite</code> enables you to run TestNG xml suites in ScalaTest. 
 * </p>
 *
 * <p>
 * Users will be (and should be) reluctant to stop running old tests. This complicates moving to a 
 * new test framework because now two frameworks would have to be supported. TestNGWrapper suite 
 * solves this by allowing users to run their legacy TestNG tests together with new tests written in Scala.
 * </p>
 * 
 * <p>
 * But, making this all happen by extending this class is a little painful. We will soon be adding support
 * to <code>org.scalatest.tools.Runner</code> to make this far easier. The goal is to simply set a property
 * via the command line, or the Ant task, and we'll take care of the test. This will be something like: 
 * </p>
 *
 * <p>
 * scala -classpath scalatest-0.9.1.jar org.scalatest.Runner -p legacy-tests.jar -testngxml mysuite1.xml,mysuite2.xml
 * </p>
 *
 * <p>
 * There may be cases in which providing the property at the command line isn't good enough, though I'm not going
 * to try to guess at the reasons why. In this situation, one can extend TestNGWrapperSuite, providing the 
 * name of the property containing the xml suite file names. Here is a simple example:
 * </p>
 *
 * <pre>class ExampleTestNGWrapperSuite extends TestNGWrapperSuite( "testngXmlSuite" ) {}</pre>
 * 
 * <p>
 * This class can then be incuded in any ScalaTest suite. At runtime, the property provided in the constuctor
 * (in this case "testngXmlSuite") will be looked up, parsed, and the List of filenames will be passed 
 * to TestNG. If the property is not available, an IllegalArgumentException will be thrown
 * If any files contained in the property cannot be found, a FileNotFoundException will be thrown.
 * </p>
 *
 * @author Josh Cough
 */
class TestNGWrapperSuite(xmlSuitesPropertyName: String) extends TestNGSuite{
  
  if( getSuites == null ) throw new IllegalArgumentException("no property: " + xmlSuitesPropertyName )
  
  def getSuites: String = System.getProperty(xmlSuitesPropertyName)
  
  /**
   * Runs TestNG with the xml suites provided via the constructor. <br>
   * 
   * @param   testName   If present (Some), then only the method with the supplied name is executed and groups will be ignored.
   * @param   reporter         The reporter to be notified of test events (success, failure, etc).
   * @param   groupsToInclude        Contains the names of groups to run. Only tests in these groups will be executed.
   * @param   groupsToExclude        Tests in groups in this Set will not be executed.
   *
   * @param   stopper            Ignored. TestNG doesn't have a stopping mechanism.
   * @param   properties        Currently ignored. see note above...should we be ignoring these?
   * @param   distributor        Ignored. TestNG handles its own concurrency. We consciously chose to leave that as it was. 
   * <br><br>
   * TODO: Currently doing nothing with properties. Should we be?<br>
   * NOTE: TestNG doesn't have a stopping mechanism. Stopper is ignored.<br>
   * NOTE: TestNG handles its own concurrency. Distributor is ignored.<br>
   */
  override def execute(testName: Option[String], reporter: Reporter, stopper: Stopper, includes: Set[String], 
      excludes: Set[String], properties: Map[String, Any], distributor: Option[Distributor]) {
    
    runTestNG(reporter, includes, excludes);
  }
  
  /**
   * Runs all tests in the xml suites.
   * @param   reporter   the reporter to be notified of test events (success, failure, etc)
   */
  override private[testng] def runTestNG(reporter: Reporter) : TestListenerAdapter = {
    runTestNG( reporter, Set(), Set() )
  }
  
  /**
   * Executes the following:
   * 
   * 1) Calls the super class to set up groups with the given groups Sets.
   * 2) Adds the xml suites to TestNG
   * 3) Runs TestNG
   *
   * @param   reporter   the reporter to be notified of test events (success, failure, etc)
   * @param   groupsToInclude    contains the names of groups to run. only tests in these groups will be executed
   * @param   groupsToExclude    tests in groups in this Set will not be executed
   */ 
  private[testng] def runTestNG(reporter: Reporter, groupsToInclude: Set[String], 
      groupsToExclude: Set[String]) : TestListenerAdapter = {
    
    val testng = new TestNG()
    handleGroups( groupsToInclude, groupsToExclude, testng )
    addXmlSuitesToTestNG( testng )
    
    run( testng, reporter )
  }
  
  /**
   * TestNG allows users to programmatically tell it which xml suites to run via the setTestSuites method.
   * This method takes a java.util.List containing java.io.File objects, where each file is a TestNG xml suite. 
   * TestNGWrapperSuite takes xmlSuitesPropertyName in its constructor. This property should contain
   * the full paths of one or more xml suites, comma seperated. This method simply creates a java.util.List 
   * containing each xml suite contained in xmlSuitesPropertyName and calls the setTestSuites method on the
   * given TestNG object. 
   *
   * @param testng	the TestNG object to set the suites on 
   *
   * @throws FileNotFoundexception if a file in xmlSuitesPropertyName does not exist.
   *
   * TODO: We should probably do this checking in the constructor.    
   */
  private def addXmlSuitesToTestNG( testng: TestNG ){
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
