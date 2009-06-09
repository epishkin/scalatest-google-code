package org.scalatest

import org.scalatest.matchers.ShouldMatchers
import scala.collection.immutable.TreeSet
import org.scalacheck._
import Arbitrary._
import Prop._

class FilterSpec extends Spec with ShouldMatchers {

  describe("A Filter") {

    it("should throw NPEs if constructed with nulls") {
      intercept[NullPointerException] {
        new Filter(null, null)
      }
      intercept[NullPointerException] {
        new Filter(None, null)
      }
      intercept[NullPointerException] {
        new Filter(null, Set())
      }
    }

    it("should throw IAE if passed a Some(Set()) for tagsToInclude") {
      intercept[IllegalArgumentException] {
        new Filter(Some(Set()), Set())
      }
    }

    it("should throw IAE if passed an empty set for testName in the apply method") {
      val caught = intercept[IllegalArgumentException] {
        val filter = new Filter(None, Set())
        filter.apply(Set("hi", "ho"), Map("hi" -> Set()))
      }
      assert(caught.getMessage === "hi was associated with an empty set in the map passsed as tags")
    }

    it("should throw IAE if passed an empty set for testName in the includedTestCount method") {
      val caught = intercept[IllegalArgumentException] {
        val filter = new Filter(None, Set())
        filter.runnableTestsCount(Set("hi", "ho"), Map("hi" -> Set()))
      }
      assert(caught.getMessage === "hi was associated with an empty set in the map passsed as tags")
    }

    val potentialTestNames = List("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z")
    val potentialTagNames = List("tag0", "tag1", "tag2", "tag3", "org.scalatest.Ignore", "tag5", "tag6", "tag7", "tag9", "tag9")

    def randomPositiveInt(max: Int) = (Math.random * 10000).toInt % (max + 1)

    it("should handle Ignore properly when None is passed to filter for tagsToInclude, and org.scalatest.Ignore is not passed in the tagsToExclude") {
      val filter = new Filter(None, Set("no ignore here"))
      val filtered = filter(Set("myTestName"), Map("myTestName" -> Set("org.scalatest.Ignore")))
      assert(filtered exists (tuple => tuple._1 == "myTestName"), "myTestName was not in the tags map, but did not show up in the result of apply") 
      assert(filtered exists (tuple => tuple._1 == "myTestName" && tuple._2 == true), "myTestName was in the result of apply, but was not marked as ignored") 
    }

    it("should work properly when None is passed to filter for tagsToInclude") {
      
      // I want to pass None in for includes, pick a set of test names. From those test names, put some of them in the tags map, grabbing an arbitrary nonzero number of tags

      for (i <- 0 to 1000) {
        val testNames = potentialTestNames.drop(randomPositiveInt(potentialTestNames.length))
        val testNamesWithTags = testNames.drop(randomPositiveInt(testNames.length))
        //val tuples = for (testName <- testNamesWithTags) yield (testName, Set() ++ potentialTagNames.drop(randomPositiveInt(potentialTagNames.length - 1)))
        val tuples =
          for (testName <- testNamesWithTags) yield
            (testName, Set() ++ potentialTagNames.drop(randomPositiveInt(potentialTagNames.length - 1))) // subtract one, so never end up with an empty list
        val tags = Map() ++ tuples

        val tagsToExclude = Set() ++ potentialTagNames.drop(randomPositiveInt(potentialTagNames.length)) // Do want an empty set here occasionally
        val filter = new Filter(None, tagsToExclude)
        val filtered = filter(TreeSet[String]() ++ testNames, tags)

        assert(filtered.sort(_ < _) === filtered)

        for ((testName, ignore) <- filtered) {

          // testName should not be in the tagsToExclude map unless it is ignored
          if (tagsToExclude contains testName)
            assert(tags(testName) exists (_ == "org.scalatest.Ignore"), testName + " was in the filtered list and in the tags, but didn't have an Ignore tag")
        }
/*
        println("testNames = " + testNames)
        println("tags = " + tags)
        println("tagsToExclude = " + tagsToExclude)
        println
*/

        // Check that every test name that is not at all in the tags map, should be in the filtered
        for (testName <- testNames) {
          if (!tags.contains(testName)) {
            assert(filtered exists (tuple => tuple._1 == testName), testName + " was not in the tags map, but did not show up in the result of apply") 
            assert(filtered exists (tuple => tuple._1 == testName && tuple._2 == false), testName + " was not in the tags map, and did show up in the result of apply, but was marked as ignored") 
          }
        }

        // Check that every test name that is in the tags as ignored, should be in the filtered as ignored
        for (testName <- testNames) {
          if (tags.contains(testName) && tags(testName).exists(_ == "org.scalatest.Ignore"))
            assert(filtered exists (tuple => tuple._1 == testName && tuple._2 == true), testName + " was in the tags map as ignored, but did not show up in the result of apply marked as ignored") 
        }

        // Check that only the non-ignored tests are counted in the runnableTestsCount
        val runnableTests =
          for {
            (testName, ignore) <- filtered
            if !ignore
          } yield testName

        assert(filter.runnableTestsCount(Set() ++ testNames, tags) === runnableTests.size, "runnableTests = " + runnableTests + ", testNames = " + testNames + ", tags = " + tags + ", tagsToExclude = " + tagsToExclude)
      }
    }

    it("should not include an excluded tag even if it also appears as an included tag") {
      val filter = new Filter(Some(Set("Slow")), Set("Slow"))
      val filtered = filter(Set("myTestName"), Map("myTestName" -> Set("Slow")))
      assert(filtered.size === 0) 
    }

    it("should include an included tag if there are no excluded tags") {
      val filter = new Filter(Some(Set("Slow")), Set())
      val filtered = filter(Set("myTestName"), Map("myTestName" -> Set("Slow")))
      assert(filtered.size === 1) 
    }

    it("should work properly when Some is passed to filter for tagsToInclude") {
      
      // I want to pass None in for includes, pick a set of test names. From those test names, put some of them in the tags map, grabbing an arbitrary nonzero number of tags

      for (i <- 0 to 1000) {
        val testNames = potentialTestNames.drop(randomPositiveInt(potentialTestNames.length))
        val testNamesWithTags = testNames.drop(randomPositiveInt(testNames.length))
        //val tuples = for (testName <- testNamesWithTags) yield (testName, Set() ++ potentialTagNames.drop(randomPositiveInt(potentialTagNames.length - 1)))
        val tuples =
          for (testName <- testNamesWithTags) yield
            (testName, Set() ++ potentialTagNames.drop(randomPositiveInt(potentialTagNames.length - 1))) // subtract one, so never end up with an empty list
        val tags = Map() ++ tuples

        val tagsToExclude = Set() ++ potentialTagNames.drop(randomPositiveInt(potentialTagNames.length)) // Do want an empty set here occasionally
        val tagsToInclude = Set() ++ potentialTagNames.drop(randomPositiveInt(potentialTagNames.length - 1)) // Again, subtracting one to avoid an empty set, which is an illegal argument. 

        val filter = new Filter(Some(tagsToInclude), tagsToExclude)
        val filtered = filter(TreeSet[String]() ++ testNames, tags)

        assert(filtered.sort(_ < _) === filtered)

        // Anything that's not in the include tags should not appear in the output
        // Look at everything in the output, and make sure it is in the include tags
        for ((testName, _) <- filtered) {
          assert(tags contains testName, "tags did not contain as a key the test name: " + testName)
          val tagsForTestName = tags(testName)
          val intersection = tagsToInclude ** tagsForTestName
          assert(intersection.size != 0, "None of the tags for the test name showed up in the non-empty tags to include set")
        }
        for ((testName, ignore) <- filtered) {

          // testName should not be in the tagsToExclude map unless it is ignored
          if (tagsToExclude contains testName)
            assert(tags(testName) exists (_ == "org.scalatest.Ignore"), testName + " was in the filtered list and in the tags, but didn't have an Ignore tag")
        }

        // Check that every test name that is not at all in the tags map, should not be in the filtered, because it has to be tagged by one of the tags in tagsToInclude
        for (testName <- testNames) {
          if (!tags.contains(testName)) {
            assert(!filtered.exists(tuple => tuple._1 == testName), testName + " was not in the tags map, but showed up in the result of apply even though tagsToInclude was a Some") 
          }
        }

        // Check that every test name that is in the tags as ignored, which also shared a tag in common with tagsToInclude, should be in the filtered as ignored
        for (testName <- testNames) {
          if (tags.contains(testName) && tags(testName).exists(_ == "org.scalatest.Ignore") && ((tags(testName) ** tagsToInclude).size > 0))
            assert(filtered exists (tuple => tuple._1 == testName && tuple._2 == true), testName + " was in the tags map as ignored, but did not show up in the result of apply marked as ignored") 
        }
      }
    }
/*
 def apply(testNames: Set[String], tags: Map[String, Set[String]]): List[(String, Boolean)] = List()

  def includedTestCount(testNames: Set[String], tags: Map[String, Set[String]]): Int = apply(testNames, tags).size
*/
  }
}
