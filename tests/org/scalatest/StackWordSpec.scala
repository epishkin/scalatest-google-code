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


import matchers.ShouldMatchers

class StackWordSpec extends WordSpec with StackFixtureCreationMethods with WordStackBehaviors with ShouldMatchers {

  "A Stack" when it {
    
      "is empty" should {

        "be empty" in {
          assert(emptyStack.empty)
        }

        "complain on peek" in {
          intercept[IllegalStateException] {
            emptyStack.peek
          }
        }

        "complain on pop" in {
          intercept[IllegalStateException] {
            emptyStack.pop
          }
        }
    }

    "has one item" can {
  
      nonEmptyStack(lastValuePushed)(stackWithOneItem)
      nonFullStack(stackWithOneItem)
    }

    "has one item less than capacity" can {

      nonEmptyStack(lastValuePushed)(stackWithOneItemLessThanCapacity)
      nonFullStack(stackWithOneItemLessThanCapacity)
    }

    "is full" can {

      "be full" in {
        assert(fullStack.full)
      }

      "go to sleep soon" in (pending)

      nonEmptyStack(lastValuePushed)(fullStack)

      "complain on a push" in {
        intercept[IllegalStateException] {
          fullStack.push(10)
        }
      }
    }
  }
}