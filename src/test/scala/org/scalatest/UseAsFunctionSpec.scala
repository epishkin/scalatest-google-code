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

import org.scalatest.events.Event

class UseAsFunctionSpec extends FreeSpec {

  "You should be able to use the following as a function after moving to a deprecated implicit conversion:" - {
    "a Reporter" in {
      def takesFun(fun: Event => Unit) {}
      class MyReporter extends Reporter { 
        def apply(e: Event) {}
      }
      takesFun(new MyReporter) // If it compiles, the test passes
    }
  }
}
