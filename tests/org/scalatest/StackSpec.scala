/*
 * Copyright 2001-2008 Artima, Inc.
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

import scala.collection.mutable.ListBuffer

class StackSpec extends Spec with StackFixtureCreationMethods with StackBehaviors {

  describe("A Stack") {

    describe("(when empty)") {
      
      it("should be empty") {
        assert(emptyStack.empty)
      }

      it("should complain on peek") {
        intercept[IllegalStateException] {
          emptyStack.peek
        }
      }

      it("should complain on pop") {
        intercept[IllegalStateException] {
          emptyStack.pop
        }
      }
    }

    describe("(with one item)") {
      nonEmptyStack(lastValuePushed)(stackWithOneItem)
      nonFullStack(stackWithOneItem)
    }
    
    describe("(with one item less than capacity)") {
      nonEmptyStack(lastValuePushed)(stackWithOneItemLessThanCapacity)
      nonFullStack(stackWithOneItemLessThanCapacity)
    }

    describe("(full)") {
      
      it("should be full") {
        assert(fullStack.full)
      }

      nonEmptyStack(lastValuePushed)(fullStack)

      it("should complain on a push") {
        intercept[IllegalStateException] {
          fullStack.push(10)
        }
      }
    }
  }
}
 
