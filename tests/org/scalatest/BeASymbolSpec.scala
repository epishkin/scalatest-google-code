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

class BeASymbolSpec extends Spec with ShouldMatchers with EmptyMocks {

  describe("The be a ('symbol) syntax") {
/*

    it("should do nothing if the object has an appropriately named method, which returns true") {
      emptyMock should be ('empty)
      isEmptyMock should be ('empty)
    }

    it("should throw IllegalArgumentException if no <symbol> or is<Symbol> method exists") {
      val ex1 = intercept[IllegalArgumentException] {
        noPredicateMock should be ('empty)
      }
      ex1.getMessage should equal ("NoPredicateMock has neither an empty nor an isEmpty method")
      // Check message for name that starts with a consonant (should use a instead of an)
      val ex2 = intercept[IllegalArgumentException] {
        noPredicateMock should be ('full)
      }
      ex2.getMessage should equal ("NoPredicateMock has neither a full nor an isFull method")
    }

    it("should do nothing if the object has an appropriately named method, which returns false when used with not") {
      notEmptyMock should not { be ('empty) }
      notEmptyMock should not be ('empty)
      isNotEmptyMock should not { be ('empty) }
      isNotEmptyMock should not be ('empty)
    }

    it("should throw IllegalArgumentException if no <symbol> or is<Symbol> method exists, when used with not") {
      val ex1 = intercept[IllegalArgumentException] {
        noPredicateMock should not { be ('empty) }
      }
      ex1.getMessage should equal ("NoPredicateMock has neither an empty nor an isEmpty method")
      val ex2 = intercept[IllegalArgumentException] {
        noPredicateMock should not (be ('full))
      }
      ex2.getMessage should equal ("NoPredicateMock has neither a full nor an isFull method")
      val ex3 = intercept[IllegalArgumentException] {
        noPredicateMock should not be ('empty)
      }
      ex3.getMessage should equal ("NoPredicateMock has neither an empty nor an isEmpty method")
      val ex4 = intercept[IllegalArgumentException] {
        noPredicateMock should not be ('full)
      }
      ex4.getMessage should equal ("NoPredicateMock has neither a full nor an isFull method")
    }

    it("should do nothing if the object has an appropriately named method, which returns true, when used in a logical-and expression") {
      emptyMock should ((be ('empty)) and (be ('empty)))
      emptyMock should (be ('empty) and (be ('empty)))
      emptyMock should (be ('empty) and be ('empty))
      isEmptyMock should ((be ('empty)) and (be ('empty)))
      isEmptyMock should (be ('empty) and (be ('empty)))
      isEmptyMock should (be ('empty) and be ('empty))
    }

    it("should do nothing if the object has an appropriately named method, which returns true, when used in a logical-or expression") {

      emptyMock should ((be ('full)) or (be ('empty)))
      emptyMock should (be ('full) or (be ('empty)))
      emptyMock should (be ('full) or be ('empty))
      isEmptyMock should ((be ('full)) or (be ('empty)))
      isEmptyMock should (be ('full) or (be ('empty)))
      isEmptyMock should (be ('full) or be ('empty))

      emptyMock should ((be ('empty)) or (be ('full)))
      emptyMock should (be ('empty) or (be ('full)))
      emptyMock should (be ('empty) or be ('full))
      isEmptyMock should ((be ('empty)) or (be ('full)))
      isEmptyMock should (be ('empty) or (be ('full)))
      isEmptyMock should (be ('empty) or be ('full))
    }

    it("should do nothing if the object has an appropriately named method, which returns false, when used in a logical-and expression with not") {

      notEmptyMock should (not (be ('empty)) and not (be ('empty)))
      notEmptyMock should ((not be ('empty)) and (not be ('empty)))
      notEmptyMock should (not be ('empty) and not be ('empty))

      isNotEmptyMock should (not (be ('empty)) and not (be ('empty)))
      isNotEmptyMock should ((not be ('empty)) and (not be ('empty)))
      isNotEmptyMock should (not be ('empty) and not be ('empty))
    }

    it("should do nothing if the object has an appropriately named method, which returns false, when used in a logical-or expression with not") {

      notEmptyMock should (not (be ('empty)) or not (be ('empty)))
      notEmptyMock should ((not be ('empty)) or (not be ('empty)))
      notEmptyMock should (not be ('empty) or not be ('empty))

      isNotEmptyMock should (not (be ('empty)) or not (be ('empty)))
      isNotEmptyMock should ((not be ('empty)) or (not be ('empty)))
      isNotEmptyMock should (not be ('empty) or not be ('empty))

      notEmptyMock should (not (be ('full)) or not (be ('empty)))
      notEmptyMock should ((not be ('full)) or (not be ('empty)))
      notEmptyMock should (not be ('full) or not be ('empty))

      isNotEmptyMock should (not (be ('full)) or not (be ('empty)))
      isNotEmptyMock should ((not be ('full)) or (not be ('empty)))
      isNotEmptyMock should (not be ('full) or not be ('empty))
    }

    it("should throw AssertionError if the object has an appropriately named method, which returns false") {
      val caught1 = intercept[AssertionError] {
        notEmptyMock should be ('empty)
      }
      assert(caught1.getMessage === "NotEmptyMock was not empty")
      val caught2 = intercept[AssertionError] {
        isNotEmptyMock should be ('empty)
      }
      assert(caught2.getMessage === "IsNotEmptyMock was not empty")
    }

    it("should throw AssertionError if the object has an appropriately named method, which returns true when used with not") {
      val caught1 = intercept[AssertionError] {
        emptyMock should not { be ('empty) }
      }
      assert(caught1.getMessage === "EmptyMock was empty")
      val caught2 = intercept[AssertionError] {
        emptyMock should not be ('empty)
      }
      assert(caught2.getMessage === "EmptyMock was empty")
      val caught3 = intercept[AssertionError] {
        isEmptyMock should not { be ('empty) }
      }
      assert(caught3.getMessage === "IsEmptyMock was empty")
      val caught4 = intercept[AssertionError] {
        isEmptyMock should not be ('empty)
      }
      assert(caught4.getMessage === "IsEmptyMock was empty")
    }

    it("should throw AssertionError if the object has an appropriately named method, which returns false, when used in a logical-and expression") {
      val caught1 = intercept[AssertionError] {
        emptyMock should ((be ('empty)) and (be ('full)))
      }
      assert(caught1.getMessage === "EmptyMock was empty, but EmptyMock was not full")
      val caught2 = intercept[AssertionError] {
        emptyMock should (be ('empty) and (be ('full)))
      }
      assert(caught2.getMessage === "EmptyMock was empty, but EmptyMock was not full")
      val caught3 = intercept[AssertionError] {
        emptyMock should (be ('empty) and be ('full))
      }
      assert(caught3.getMessage === "EmptyMock was empty, but EmptyMock was not full")
      val caught4 = intercept[AssertionError] {
        isEmptyMock should ((be ('empty)) and (be ('full)))
      }
      assert(caught4.getMessage === "IsEmptyMock was empty, but IsEmptyMock was not full")
      val caught5 = intercept[AssertionError] {
        isEmptyMock should (be ('empty) and (be ('full)))
      }
      assert(caught5.getMessage === "IsEmptyMock was empty, but IsEmptyMock was not full")
      val caught6 = intercept[AssertionError] {
        isEmptyMock should (be ('empty) and be ('full))
      }
      assert(caught6.getMessage === "IsEmptyMock was empty, but IsEmptyMock was not full")
    }

    it("should throw AssertionError if the object has an appropriately named method, which returns false, when used in a logical-or expression") {

      val caught1 = intercept[AssertionError] {
        notEmptyMock should ((be ('empty)) or (be ('empty)))
      }
      assert(caught1.getMessage === "NotEmptyMock was not empty, and NotEmptyMock was not empty")
      val caught2 = intercept[AssertionError] {
        notEmptyMock should (be ('empty) or (be ('empty)))
      }
      assert(caught2.getMessage === "NotEmptyMock was not empty, and NotEmptyMock was not empty")
      val caught3 = intercept[AssertionError] {
        notEmptyMock should (be ('empty) or be ('empty))
      }
      assert(caught3.getMessage === "NotEmptyMock was not empty, and NotEmptyMock was not empty")
      val caught4 = intercept[AssertionError] {
        isNotEmptyMock should ((be ('empty)) or (be ('empty)))
      }
      assert(caught4.getMessage === "IsNotEmptyMock was not empty, and IsNotEmptyMock was not empty")
      val caught5 = intercept[AssertionError] {
        isNotEmptyMock should (be ('empty) or (be ('empty)))
      }
      assert(caught5.getMessage === "IsNotEmptyMock was not empty, and IsNotEmptyMock was not empty")
      val caught6 = intercept[AssertionError] {
        isNotEmptyMock should (be ('empty) or be ('empty))
      }
      assert(caught6.getMessage === "IsNotEmptyMock was not empty, and IsNotEmptyMock was not empty")
    }

    it("should throw AssertionError if the object has an appropriately named method, which returns true, when used in a logical-and expression with not") {

      val caught1 = intercept[AssertionError] {
        emptyMock should (not (be ('full)) and not (be ('empty)))
      }
      assert(caught1.getMessage === "EmptyMock was not full, but EmptyMock was empty")
      val caught2 = intercept[AssertionError] {
        emptyMock should ((not be ('full)) and (not be ('empty)))
      }
      assert(caught2.getMessage === "EmptyMock was not full, but EmptyMock was empty")
      val caught3 = intercept[AssertionError] {
        emptyMock should (not be ('full) and not be ('empty))
      }
      assert(caught3.getMessage === "EmptyMock was not full, but EmptyMock was empty")
      val caught4 = intercept[AssertionError] {
        isEmptyMock should (not (be ('full)) and not (be ('empty)))
      }
      assert(caught4.getMessage === "IsEmptyMock was not full, but IsEmptyMock was empty")
      val caught5 = intercept[AssertionError] {
        isEmptyMock should ((not be ('full)) and (not be ('empty)))
      }
      assert(caught5.getMessage === "IsEmptyMock was not full, but IsEmptyMock was empty")
      val caught6 = intercept[AssertionError] {
        isEmptyMock should (not be ('full) and not be ('empty))
      }
      assert(caught6.getMessage === "IsEmptyMock was not full, but IsEmptyMock was empty")
      // Check that the error message "short circuits"
      val caught7 = intercept[AssertionError] {
        emptyMock should (not (be ('empty)) and not (be ('full)))
      }
      assert(caught7.getMessage === "EmptyMock was empty")
    }

    it("should throw AssertionError if the object has an appropriately named method, which returns true, when used in a logical-or expression with not") {

      val caught1 = intercept[AssertionError] {
        emptyMock should (not (be ('empty)) or not (be ('empty)))
      }
      assert(caught1.getMessage === "EmptyMock was empty, and EmptyMock was empty")
      val caught2 = intercept[AssertionError] {
        emptyMock should ((not be ('empty)) or (not be ('empty)))
      }
      assert(caught2.getMessage === "EmptyMock was empty, and EmptyMock was empty")
      val caught3 = intercept[AssertionError] {
        emptyMock should (not be ('empty) or not be ('empty))
      }
      assert(caught3.getMessage === "EmptyMock was empty, and EmptyMock was empty")
      val caught4 = intercept[AssertionError] {
        isEmptyMock should (not (be ('empty)) or not (be ('empty)))
      }
      assert(caught4.getMessage === "IsEmptyMock was empty, and IsEmptyMock was empty")
      val caught5 = intercept[AssertionError] {
        isEmptyMock should ((not be ('empty)) or (not be ('empty)))
      }
      assert(caught5.getMessage === "IsEmptyMock was empty, and IsEmptyMock was empty")
      val caught6 = intercept[AssertionError] {
        isEmptyMock should (not be ('empty) or not be ('empty))
      }
      assert(caught6.getMessage === "IsEmptyMock was empty, and IsEmptyMock was empty")
    }
*/
  }
}
