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

class DocSpec extends FreeSpec with ShouldMatchers {

  "A Doc" - {
    "with no run calls inside" - {
      // This one I'm putting flat against the margin on purpose.
      val a = new Doc(<markup>
This is a Title
===============

This is a paragraph later...
</markup>)
      "should send the markup verbatim out the door" in {
        val rep = new EventRecordingReporter
        a.run(None, rep, new Stopper {}, Filter(), Map(), None, new Tracker())
        val mp = rep.markupProvidedEventsReceived
        assert(mp.size === 1)
        val event = mp(0)
        event.text should equal ("""
This is a Title
===============

This is a paragraph later...
""")
      }
      "should return an empty list from nestedSuites" in {
        a.nestedSuites should equal (Nil)
      }
    }
  }

/*
  class SomeSuite extends Suite

  "The run method" should "produce a funky string" ignore {
    val a = Doc(<markup>
      This is a Title
      ===============

      { run[SomeSuite] }

      This is a paragraph later...
    </markup>)
  }
*/
}

