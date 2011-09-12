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

import org.scalatest.SharedHelpers.EventRecordingReporter
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.Doc.insert
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.Doc.stripMargin

class DocSpecASuite extends Suite

class DocSpec extends FreeSpec with ShouldMatchers with TableDrivenPropertyChecks {

  "A Doc" - {
    "with no run calls inside" - {

      // This one I'm putting flat against the margin on purpose.
      val flatAgainstMargin = new Doc(<markup>
This is a Title
===============

This is a paragraph later...
</markup>)
      // TODO: Blank line first, line with no chars, line with some white chars, and no lines, and only white lines
      // TODO: test with different end of line characters
      // This one is indented eight characters
      val indented8 = new Doc(<markup>
        This is a Title
        ===============

        This is a paragraph later...
      </markup>)

      val examples = Table("doc", flatAgainstMargin, indented8)
      "should send the markup unindented out the door" ignore {
        forAll (examples) { doc =>
          val rep = new EventRecordingReporter
          doc.run(None, rep, new Stopper {}, Filter(), Map(), None, new Tracker())
          val mp = rep.markupProvidedEventsReceived
          assert(mp.size === 1)
          val event = mp(0)
          // After a checkin, try a stripmargin here
          event.text should equal ("""
This is a Title
===============

This is a paragraph later...
""")
        }
      }
      "should return an empty list from nestedSuites" in {
        forAll (examples) { doc =>
          doc.nestedSuites should equal (Nil)
        }
      }
    }
    "with one run call inside" - {
      class BSuite extends Suite
      // This one I'm putting flat against the margin on purpose.
      val a = new Doc(<markup>
This is a Title
===============

This is a paragraph later...

{ insert[DocSpecASuite] }

And this is another paragraph.
</markup>)
      "should return an instance of the given suite to run in the list returned by nestedSuites" ignore {
        a.nestedSuites should have size 1
      }
    }
  }
  "The insert method" - {
    "should return a string that includes the suite class name" in {
      insert[DocSpecASuite] should equal ("\ninsert[org.scalatest.DocSpecASuite]\n")
    }
  }
  "The stripMargin method" - {
    "should throw NPE if null passed" in {
      evaluating { stripMargin(null) } should produce [NullPointerException] 
    }
    "should return an empty string as is" in {
      stripMargin("") should equal ("")
    }
    "when passed a string with leading space, should return the string with the leading space omitted" in {
      stripMargin(" Howdy") should equal ("Howdy")
      stripMargin("  Howdy") should equal ("Howdy")
      stripMargin("   Howdy") should equal ("Howdy")
      stripMargin("\tHowdy") should equal ("Howdy")
      stripMargin("\t\tHowdy") should equal ("Howdy")
      stripMargin(" \t \tHowdy") should equal ("Howdy")
    }
    "when passed a string with leading space and two lines, should return the string with the leading space omitted from the first line, and the same amound omitted from the second line, with tabs converted to one space" in {
      stripMargin(" Howdy\n123456789") should equal ("Howdy\n23456789")
      stripMargin("  Howdy\n123456789") should equal ("Howdy\n3456789")
      stripMargin("   Howdy\n123456789") should equal ("Howdy\n456789")
      stripMargin("\tHowdy\n123456789") should equal ("Howdy\n23456789")
      stripMargin("\t\tHowdy\n123456789") should equal ("Howdy\n3456789")
      stripMargin(" \t \tHowdy\n123456789") should equal ("Howdy\n56789")
    }
    "when passed a string with one or more blank lines, a line with leading space and two lines, should return the string with the leading space omitted from the first line, and the same amound omitted from the second line, with tabs converted to one space" in {
      stripMargin("\n Howdy\n123456789") should equal ("\nHowdy\n23456789")
      stripMargin("\n  \n\n  Howdy\n123456789") should equal ("\n\n\nHowdy\n3456789")
      stripMargin("\n  \t\t\n   Howdy\n123456789") should equal ("\n\t\nHowdy\n456789")
      stripMargin("\n\n\n\n\tHowdy\n123456789") should equal ("\n\n\n\nHowdy\n23456789")
      stripMargin("\n\t\tHowdy\n123456789") should equal ("\nHowdy\n3456789")
      stripMargin("\n      \n \t \tHowdy\n123456789") should equal ("\n  \nHowdy\n56789")
    }
  }
}
