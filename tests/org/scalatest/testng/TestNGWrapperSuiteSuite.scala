import org.scalatest.testng._
import org.scalatest.jmock.SMocker
import org.scalatest.jmock.SMockFunSuite
import java.io.File
import org.apache.commons.io.FileUtils

package org.scalatest.testng{

  class TestNGWrapperSuiteSuite extends SMockFunSuite with TestNGSuiteExpectations{
  
    val XML_SUITES_PROPERTY = "xml_suites"
      
    val legacySuiteXml = 
      <suite name="Simple Suite">
        <test verbose="10" name="org.scalatest.testng.test" annotations="JDK">
          <classes>
            <class name="org.scalatest.testng.test.LegacySuite"/>
          </classes>
        </test>
      </suite>
      
    mockTest("wrapper suite properly notifies reporter when tests start, and pass"){
    
      this.createSuite( legacySuiteXml )
          
      val reporter = mock(classOf[Reporter])

      expecting { singleTestToPass( reporter ) }
      
      when { new TestNGWrapperSuite(XML_SUITES_PROPERTY).runTestNG(reporter) }
    }

    val legacySuiteWithThreeTestsXml = 
      <suite name="Simple Suite">
        <test verbose="10" name="org.scalatest.testng.test" annotations="JDK">
          <classes>
            <class name="org.scalatest.testng.test.LegacySuite"/>
            <class name="org.scalatest.testng.test.LegacySuiteWithTwoTests"/>
          </classes>
        </test>
      </suite>
    
    mockTest("wrapper suite should be notified for all tests"){
      
      this.createSuite( legacySuiteWithThreeTestsXml )
          
      val reporter = mock(classOf[Reporter])

      expecting { 
        nTestsToPass( 3, reporter ) 
      }
      
      when{ new TestNGWrapperSuite(XML_SUITES_PROPERTY).runTestNG(reporter) }
    }
    
    
    def createSuite( suiteNode: scala.xml.Elem ) = {
      val tmp = File.createTempFile( "testng", "wrapper" )
      FileUtils.writeStringToFile( tmp, suiteNode.toString )
      System.setProperty( XML_SUITES_PROPERTY, tmp.getAbsolutePath )
    }
    
  }
  
  package test{
    import org.testng.annotations._
  
    class LegacySuite extends TestNGSuite {
      @Test def legacyTestThatPasses() {}
    }
    class LegacySuiteWithTwoTests extends TestNGSuite {
      @Test def anotherLegacyTestThatPasses() {}
      @Test def anotherLegacyTestThatPasses2() {}
    }
  }

}
