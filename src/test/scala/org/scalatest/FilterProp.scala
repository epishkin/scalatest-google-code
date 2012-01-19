package org.scalatest

import org.scalatest.events.Ordinal
import org.scalatest.junit.JUnit3Suite
import org.scalatest.junit.JUnitSuite
import org.scalatest.testng.TestNGSuite

class FilterProp extends SuiteProp {
  
  test("All suite types should not run nested suite when Filters's includeNestedSuites is false.") {
    forAll(examples) { suite =>
      val reporter = new EventRecordingReporter
      suite.run(None, reporter, new Stopper {}, new Filter(None, Set[String](), false), Map(), None, new Tracker(new Ordinal(99)))
      if (!suite.isInstanceOf[TestNGSuite])
        reporter.suiteStartingEventsReceived should be ('empty)
    }
  }
  
  type FixtureServices = AnyRef
  
  def suite = new Suite {
    override def nestedSuites = List(new Suite {})
  }
  def fixtureSuite = new fixture.Suite with StringFixture {
    override def nestedSuites = List(new Suite {})
  }
  def junit3Suite = new JUnit3Suite {
    override def nestedSuites = List(new Suite {})
  }
  def junitSuite = new JUnitSuite {
    override def nestedSuites = List(new Suite {})
  }
  def testngSuite = new TestNGSuite {
    override def nestedSuites = List(new Suite {})
  }
  def funSuite = new FunSuite {
    override def nestedSuites = List(new Suite {})
  }
  def fixtureFunSuite = new fixture.FunSuite with StringFixture {
    override def nestedSuites = List(new Suite {})
  }
  def funSpec = new FunSpec {
    override def nestedSuites = List(new Suite {})
  }
  def fixtureSpec = new fixture.FunSpec with StringFixture {
    override def nestedSuites = List(new Suite {})
  }
  def featureSpec = new FeatureSpec {
    override def nestedSuites = List(new Suite {})
  }
  def fixtureFeatureSpec = new fixture.FeatureSpec with StringFixture {
    override def nestedSuites = List(new Suite {})
  }
  def flatSpec = new FlatSpec {
    override def nestedSuites = List(new Suite {})
  }
  def fixtureFlatSpec = new fixture.FlatSpec with StringFixture {
    override def nestedSuites = List(new Suite {})
  }
  def freeSpec = new FreeSpec {
    override def nestedSuites = List(new Suite {})
  }
  def fixtureFreeSpec = new fixture.FreeSpec with StringFixture {
    override def nestedSuites = List(new Suite {})
  }
  def propSpec = new PropSpec {
    override def nestedSuites = List(new Suite {})
  }
  def fixturePropSpec = new fixture.PropSpec with StringFixture {
    override def nestedSuites = List(new Suite {})
  }
  def wordSpec = new WordSpec {
    override def nestedSuites = List(new Suite {})
  }
  def fixtureWordSpec = new fixture.WordSpec with StringFixture {
    override def nestedSuites = List(new Suite {})
  }

}