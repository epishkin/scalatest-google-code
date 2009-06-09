package org.scalatest

import Filter.IgnoreTag

// Pass a TestFilter to execute instead of groupsToInclude and groupsToExclude
// @throws IllegalArgumentException if <code>Some(Set())</code>, <em>i.e.</em>, a <code>Some</code> containing an empty set, is passed as <code>tagsToInclude</code>
/**
 * Filter that determines which tests to run and ignore based on tags to include and exclude.
 */
final class Filter(tagsToInclude: Option[Set[String]], tagsToExclude: Set[String]) extends Function2[Set[String], Map[String, Set[String]], List[(String, Boolean)]] {

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
   * test name and a <code>Boolean</code> that indicates whether the test should be ignored.
   *
   * <pre>
   * for ((testName, ignoreTest) <- filter(testNames, tags))
   *   if (!ignoreTest)
   *     // execute the test
   *   else
   *     // ignore the test
   * </pre>
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
   * with the <code>tagsToInclude</code> and <code>tagsToExclude</code> class parameters.
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

private object Filter {
  private final val IgnoreTag = "org.scalatest.Ignore"
}

/*
    val tns =
      for (tn <- testNames; if (groupsToInclude.isEmpty || !(groupsToInclude ** groups.getOrElse(tn, Set())).isEmpty)
         && ((groupsToExclude ** groups.getOrElse(tn, Set())).isEmpty) && (!(groups.getOrElse(tn, Set()).contains(IgnoreAnnotation))))
        yield tn


        for (tn <- testNames) {
          if (!stopper.stopRequested && (groupsToInclude.isEmpty || !(groupsToInclude ** groups.getOrElse(tn, Set())).isEmpty)) {
            if (groupsToExclude.contains(IgnoreAnnotation) && groups.getOrElse(tn, Set()).contains(IgnoreAnnotation)) {
              //wrappedReporter.testIgnored(new Report(getTestNameForReport(tn), "", Some(suiteName), Some(thisSuite.getClass.getName), Some(tn)))
              wrappedReporter.testIgnored(new Report(getTestNameForReport(tn), ""))
            }
            else if ((groupsToExclude ** groups.getOrElse(tn, Set())).isEmpty) {
              runTest(tn, wrappedReporter, stopper, goodies)
            }
          }
        }
*/
