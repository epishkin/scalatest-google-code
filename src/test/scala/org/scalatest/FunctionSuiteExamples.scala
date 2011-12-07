package org.scalatest

import org.scalatest.prop.Tables
import org.scalatest.fixture._

trait FunctionSuiteExamples extends Tables {

  type FixtureServices
  
  def funSuite: FunSuite with FixtureServices
  def fixtureFunSuite: FixtureFunSuite with FixtureServices
  def spec: Spec with FixtureServices
  def fixtureSpec: FixtureSpec with FixtureServices
  def featureSpec: FeatureSpec with FixtureServices
  def fixtureFeatureSpec: FixtureFeatureSpec with FixtureServices
  def flatSpec: FlatSpec with FixtureServices
  def fixtureFlatSpec: FixtureFlatSpec with FixtureServices
  def freeSpec: FreeSpec with FixtureServices
  def fixtureFreeSpec: FixtureFreeSpec with FixtureServices
  def propSpec: PropSpec with FixtureServices
  def fixturePropSpec: FixturePropSpec with FixtureServices
  def wordSpec: WordSpec with FixtureServices
  def fixtureWordSpec: FixtureWordSpec with FixtureServices
  
  def examples =
    Table(
      "suite",
      funSuite,
      fixtureFunSuite,
      spec,
      fixtureSpec,
      featureSpec,
      fixtureFeatureSpec,
      flatSpec,
      fixtureFlatSpec,
      freeSpec,
      fixtureFreeSpec,
      propSpec,
      fixturePropSpec,
      wordSpec,
      fixtureWordSpec
    )
  
}