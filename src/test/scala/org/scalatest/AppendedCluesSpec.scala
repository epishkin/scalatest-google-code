/*
 * Copyright 2001-2011 Artima, Inc.
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
package org.scalatest

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.JUnitTestFailedError
// TODO: Test with imported AppendedClues
class AppendedCluesSpec extends FlatSpec with ShouldMatchers with AppendedClues {

  it should "return the new exception with the clue string appended, separated by a space char if passed a function that does that" in {
    val tfe = new TestFailedException("message", 3)
    val clue = "clue"
    val fun: (Option[String] => Option[String]) =
      opt => opt match {
        case Some(msg) => Some(msg + " " + clue)
        case None => Some(clue)
      }
    tfe.modifyMessage(fun).message.get should be ("message clue")
  }

  it should "return the new JUnit exception with the clue string appended, separated by a space char if passed a function that does that" in {
    val jutfe = new JUnitTestFailedError("message", 3)
    val clue = "clue"
    val fun: (Option[String] => Option[String]) =
      opt => opt match {
        case Some(msg) => Some(msg + " " + clue)
        case None => Some(clue)
      }
    jutfe.modifyMessage(fun).message.get should be ("message clue")
  }

  // ******* withClue tests *******

  "The withClue construct" should "allow any non-ModifiableMessage exception to pass through" in {
    val iae = new IllegalArgumentException
    val caught = intercept[IllegalArgumentException] {
      { failWith(iae) } withClue "howdy"
    }
    caught should be theSameInstanceAs (iae)
  }

  it should "given an empty clue string, rethrow the same TFE exception" in {
    val tfe = new TestFailedException("before", 3)
    val caught = intercept[TestFailedException] {
      { failWith(tfe) } withClue ""
    }
    caught should be theSameInstanceAs (tfe)
  }

  it should "given an all-whitespace clue string, should throw a new TFE with the white space appended to the old message" in {
    val tfe = new TestFailedException("message", 3)
    val white = "    "
    val caught = intercept[TestFailedException] {
      { failWith(tfe) } withClue white
    }
    caught should not be theSameInstanceAs (tfe)
    caught.message should be ('defined)
    caught.message.get should equal ("message" + white)
  }

  it should "given a non-empty clue string with no trailing white space, throw a new instance of the caught TFE exception that has all fields the same except a appended clue string preceded by an extra space" in {
    val tfe = new TestFailedException("message", 3)
    val caught = intercept[TestFailedException] {
      { failWith(tfe) } withClue "clue"
    }
    caught should not be theSameInstanceAs (tfe)
    caught.message should be ('defined)
    caught.message.get should equal ("message clue")
  }

  it should "given a non-empty clue string with a leading space, throw a new instance of the caught TFE exception that has all fields the same except an appended clue string (preceded by no extra space)" in {
    val tfe = new TestFailedException("message", 3)
    val caught = intercept[TestFailedException] {
      { failWith(tfe) } withClue " clue" // has a leading space
    }
    caught should not be theSameInstanceAs (tfe)
    caught.message should be ('defined)
    caught.message.get should equal ("message clue")
  }

  it should "given a non-empty clue string preceded by an end of line, throw a new instance of the caught TFE exception that has all fields the same except an appended clue string (preceded by no extra space)" in {
    val tfe = new TestFailedException("message", 3)
    val caught = intercept[TestFailedException] {
      { failWith(tfe) } withClue "\nclue" // has an end of line character
    }
    caught should not be theSameInstanceAs (tfe)
    caught.message should be ('defined)
    caught.message.get should equal ("message\nclue")
  }

  it should "given an empty clue string, rethrow the same JUTFE exception" in {
    val jutfe = new JUnitTestFailedError("before", 3)
    val caught = intercept[JUnitTestFailedError] {
        { failWith(jutfe) } withClue ""
    }
    caught should be theSameInstanceAs (jutfe)
  }

  it should "given an all-whitespace clue string, should throw a new JUTFE with the white space appended to the old message" in {
    val jutfe = new JUnitTestFailedError("message", 3)
    val white = "    "
    val caught = intercept[JUnitTestFailedError] {
      { failWith(jutfe) } withClue white
    }
    caught should not be theSameInstanceAs (jutfe)
    caught.message should be ('defined)
    caught.message.get should equal ("message" + white)
  }

  it should "given a non-empty clue string with no leading white space, throw a new instance of the caught JUTFE exception that has all fields the same except an appended clue string preceded by an extra space" in {
    val jutfe = new JUnitTestFailedError("message", 3)
    val caught = intercept[JUnitTestFailedError] {
      { failWith(jutfe) } withClue "clue" 
    }
    caught should not be theSameInstanceAs (jutfe)
    caught.message should be ('defined)
    caught.message.get should equal ("message clue")
  }

  it should "given a non-empty clue string with a trailing space, throw a new instance of the caught JUTFE exception that has all fields the same except a prepended clue string (followed by no extra space)" in {
    val jutfe = new JUnitTestFailedError("message", 3)
    val caught = intercept[JUnitTestFailedError] {
      { failWith(jutfe) } withClue " clue" // has a leading space
    }
    caught should not be theSameInstanceAs (jutfe)
    caught.message should be ('defined)
    caught.message.get should equal ("message clue")
  }

  it should "given a non-empty clue string with an end of line, throw a new instance of the caught JUTFE exception that has all fields the same except an appended clue string (preceded by no extra space)" in {
    val jutfe = new JUnitTestFailedError("message", 3)
    val caught = intercept[JUnitTestFailedError] {
      { failWith(jutfe) } withClue ("\nclue") // has an end of line character
    }
    caught should not be theSameInstanceAs (jutfe)
    caught.message should be ('defined)
    caught.message.get should equal ("message\nclue")
  }

  // ***** tests with objects other than String *****

  def failWith(e: Throwable) { throw e }

  it should "given an object with a non-empty clue string with no leading white space, throw a new instance of the caught TFE exception that has all fields the same except a appended clue string preceded by an extra space" in {
    val tfe = new TestFailedException("message", 3)
    val list = List(1, 2, 3)
    val caught = intercept[TestFailedException] {
      { failWith(tfe) } withClue (list)
    }
    caught should not be theSameInstanceAs (tfe)
    caught.message should be ('defined)
    caught.message.get should equal ("message List(1, 2, 3)")
  }
}

