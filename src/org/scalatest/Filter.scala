package org.scalatest

import Filter.IgnoreTag

// Pass a TestFilter to execute instead of groupsToInclude and groupsToExclude
// @throws IllegalArgumentException if <code>Some(Set())</code>, <em>i.e.</em>, a <code>Some</code> containing an empty set, is passed as <code>tagsToInclude</code>
/**
 * Filter whose <code>apply</code> method determines which of the passed tests to run and ignore based on tags to include and exclude passed as
 * as class parameters. This class handles the <code>org.scalatest.Ignore</code> tag specially, in that its <code>apply</code> method indicates which
 * tests should be ignored based on whether they are tagged with <code>org.scalatest.Ignore</code>. If
 * <code>"org.scalatest.Ignore"</code> is not passed in the <code>tagsToExclude</code> set, it will be implicitly added. However, if the 
 * <code>tagsToInclude</code> option is defined, and the contained set does not include <code>"org.scalatest.Ignore"</code>, then only those tests
 * that are both tagged with <code>org.scalatest.Ignore</code> and at least one of the tags in the <code>tagsToInclude</code> set
 * will be included in the result of <code>apply</code> and marked as ignored.
 *
 * @param tagsToInclude an optional <code>Set</code> of <code>String</code> tag names to include (<em>i.e.</em>, not filter out) when filtering tests
 * @param tagsToExclude a <code>Set</code> of <code>String</code> tag names to exclude (<em>i.e.</em>, filter out) when filtering tests
 *
 * @throws NullPointerException if either <code>tagsToInclude</code> or <code>tagsToExclude</code> are null
 * @throws IllegalArgumentException if <code>tagsToInclude</code> is defined, but contains an empty set
 */
final class Filter(val tagsToInclude: Option[Set[String]], val tagsToExclude: Set[String]) extends Function2[Set[String], Map[String, Set[String]], List[(String, Boolean)]] {

  if (tagsToInclude == null)
    throw new NullPointerException("tagsToInclude was null")
  if (tagsToExclude == null)
    throw new NullPointerException("tagsToExclude was null")

  tagsToInclude match {
    case Some(tagsToInclude) =>
      if (tagsToInclude.isEmpty)
        throw new IllegalArgumentException("tagsToInclude was defined, but contained an empty set")
    case None =>
  }

  private def includedTestNames(testNamesAsList: List[String], tags: Map[String, Set[String]]): List[String] = 
    tagsToInclude match {
      case None => testNamesAsList
      case Some(tagsToInclude) =>
        for {
          testName <- testNamesAsList
          if tags contains testName
          intersection = tagsToInclude ** tags(testName)
          if intersection.size > 0
        } yield testName
    }

  private def verifyPreconditionsForMethods(testNames: Set[String], tags: Map[String, Set[String]]) {
    val testWithEmptyTagSet = tags.find(tuple => tuple._2.isEmpty)
    testWithEmptyTagSet match {
      case Some((testName, _)) => throw new IllegalArgumentException(testName + " was associated with an empty set in the map passsed as tags")
      case None =>
    }
  }

  /**
   * Filter test names based on their tags.  The returned tuple contains a <code>String</code>
   * test name and a <code>Boolean</code> that indicates whether the test should be ignored. A test will be marked as ignored
   * if <code>org.scalatest.Ignore</code> is in its tags set, and either <code>tagsToInclude</code> is <code>None</code>, or
   * <code>tagsToInclude</code>'s value (a set) contains the test's name.
   *
   * <pre>
   * for ((testName, ignoreTest) <- filter(testNames, tags))
   *   if (!ignoreTest)
   *     // execute the test
   *   else
   *     // ignore the test
   * </pre>
   *
   * @param testNames test names to be filtered
   * @param tags a map from test name to tags, containing only test names included in the <code>testNames</code> set, and
   *   only test names that have at least one tag
   *
   * @throws IllegalArgumentException if any set contained in the passed <code>tags</code> map is empty
   */
  def apply(testNames: Set[String], tags: Map[String, Set[String]]): List[(String, Boolean)] = {

    verifyPreconditionsForMethods(testNames, tags)

    val testNamesAsList = testNames.toList // to preserve the order
    val filtered =
      for {
        testName <- includedTestNames(testNamesAsList, tags)
        if !tags.contains(testName) || tags(testName).contains(IgnoreTag) || (tags(testName) ** tagsToExclude).size == 0
      } yield (testName, tags.contains(testName) && tags(testName).contains(IgnoreTag))

    filtered
  }

  /**
   * Returns the number of tests that should be run after the passed <code>testNames</code> and <code>tags</code> have been filtered
   * with the <code>tagsToInclude</code> and <code>tagsToExclude</code> class parameters. This may be smaller than the number of
   * elements in the list returned by <code>apply</code>, because the count returned by this method does not include ignored tests,
   * and the list returned by <code>apply</code> does include ignored tests.
   *
   * @param testNames test names to be filtered
   * @param tags a map from test name to tags, containing only test names included in the <code>testNames</code> set, and
   *   only test names that have at least one tag
   *
   * @throws IllegalArgumentException if any set contained in the passed <code>tags</code> map is empty
   */
  def runnableTestsCount(testNames: Set[String], tags: Map[String, Set[String]]): Int = {

    verifyPreconditionsForMethods(testNames, tags)

    val testNamesAsList = testNames.toList // to preserve the order
    val runnableTests = 
      for {
        testName <- includedTestNames(testNamesAsList, tags)
        if !tags.contains(testName) || (!tags(testName).contains(IgnoreTag) && (tags(testName) ** tagsToExclude).size == 0)
      } yield testName

    runnableTests.size
  }
}

object Filter {
  private final val IgnoreTag = "org.scalatest.Ignore"

/**
 * Factory method for a <code>Filter</code> initialized with the passed <code>tagsToInclude</code>
 * and <code>tagsToExclude</code>.
 *
 * @param tagsToInclude an optional <code>Set</code> of <code>String</code> tag names to include (<em>i.e.</em>, not filter out) when filtering tests
 * @param tagsToExclude a <code>Set</code> of <code>String</code> tag names to exclude (<em>i.e.</em>, filter out) when filtering tests
 *
 * @throws NullPointerException if either <code>tagsToInclude</code> or <code>tagsToExclude</code> are null
 * @throws IllegalArgumentException if <code>tagsToInclude</code> is defined, but contains an empty set
 */
  def apply(tagsToInclude: Option[Set[String]], tagsToExclude: Set[String]) =
    new Filter(tagsToInclude, tagsToExclude)

/**
 * Factory method for a <code>Filter</code> initialized with <code>None</code> for <code>tagsToInclude</code>
 * and an empty set for <code>tagsToExclude</code>.
 *
 * @param tagsToInclude an optional <code>Set</code> of <code>String</code> tag names to include (<em>i.e.</em>, not filter out) when filtering tests
 * @param tagsToExclude a <code>Set</code> of <code>String</code> tag names to exclude (<em>i.e.</em>, filter out) when filtering tests
 *
 * @throws NullPointerException if either <code>tagsToInclude</code> or <code>tagsToExclude</code> are null
 * @throws IllegalArgumentException if <code>tagsToInclude</code> is defined, but contains an empty set
 */
  def apply() =
    new Filter(None, Set("org.scalatest.Ignore"))
}
