package org.scalatest

import Filter.IgnoreTag

// Pass a TestFilter to execute instead of groupsToInclude and groupsToExclude
// @throws IllegalArgumentException if <code>Some(Set())</code>, <em>i.e.</em>, a <code>Some</code> containing an empty set, is passed as <code>tagsToInclude</code>
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

  /**
   * Filter test names based on their tags.
   * The tuple contains a <code>String</code> test name, and a <code>Boolean</code> indicating whether the test should be ignored.
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

    // Check the preconditions
    val testWithEmptyTagSet = tags.find(tuple => tuple._2.isEmpty)
    testWithEmptyTagSet match {
      case Some((testName, _)) => throw new IllegalArgumentException(testName + " was associated with an empty set in the map passsed as tags")
      case None =>
    }

    val includedTestNames =
      tagsToInclude match {
        case None => testNames
        case Some(tagsToInclude) =>
          for {
            testName <- testNames
            if tags contains testName
            intersection = tagsToInclude ** tags(testName)
            if intersection.size > 0
          } yield testName
      }

    val filtered =
      for {
        testName <- includedTestNames
        if !tags.contains(testName) || tags(testName).contains(IgnoreTag)
      } yield (testName, tags contains testName)

    filtered.toList
  }

  def includedTestCount(testNames: Set[String], tags: Map[String, Set[String]]): Int = apply(testNames, tags).size
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
