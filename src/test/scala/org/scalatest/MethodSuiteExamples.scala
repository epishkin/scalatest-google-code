package org.scalatest

import org.scalatest.prop.Tables
import org.scalatest.fixture._
import org.scalatest.junit.JUnit3Suite
import org.scalatest.junit.JUnitSuite
import org.scalatest.testng.TestNGSuite

trait MethodSuiteExamples extends Tables {
  type FixtureServices
  
  def suite: Suite with FixtureServices
  def fixtureSuite: FixtureSuite with FixtureServices
  def junit3Suite: JUnit3Suite with FixtureServices
  def junitSuite: JUnitSuite with FixtureServices
  def testngSuite: TestNGSuite with FixtureServices
  
  def examples =
    Table(
      "suite",
      suite,
      fixtureSuite,
      junit3Suite, 
      junitSuite,
      testngSuite
    )
}