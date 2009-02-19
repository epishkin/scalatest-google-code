/*
 * Copyright 2001-2009 Artima, Inc.
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

import prop.Checkers
import org.scalacheck._
import Arbitrary._
import Prop._

class ShouldBehaveLikeSpec extends Spec with ShouldMatchers with ShouldStackBehaviors with StackFixtureCreationMethods {

  // Checking for a specific size
  describe("The 'should behave like' syntax should work in a describe") {

    stackWithOneItem should behave like (nonEmptyStack(lastValuePushed))

    describe(", and in a nested describe") {

      stackWithOneItem should behave like (nonEmptyStack(lastValuePushed))
    }
  }
}
