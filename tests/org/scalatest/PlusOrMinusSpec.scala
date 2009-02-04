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

class PlusOrMinusSpec extends Spec with ShouldMatchers {

  describe("The be (x.x plusOrMinus y.y) syntax") {

    val sevenDotOh = 7.0
    val minusSevenDotOh = -7.0
    val sevenDotOhFloat = 7.0f
    val minusSevenDotOhFloat = -7.0f

    it("should do nothing if the floating point number is within the specified range") {

      sevenDotOh should be (7.1 plusOrMinus 0.2)
      sevenDotOh should be (6.9 plusOrMinus 0.2)
      sevenDotOh should be (7.0 plusOrMinus 0.2)
      sevenDotOh should be (7.2 plusOrMinus 0.2)
      sevenDotOh should be (6.8 plusOrMinus 0.2)
      minusSevenDotOh should be (-7.1 plusOrMinus 0.2)
      minusSevenDotOh should be (-6.9 plusOrMinus 0.2)
      minusSevenDotOh should be (-7.0 plusOrMinus 0.2)
      minusSevenDotOh should be (-7.2 plusOrMinus 0.2)
      minusSevenDotOh should be (-6.8 plusOrMinus 0.2)

      sevenDotOhFloat should be (7.1f plusOrMinus 0.2f)
      sevenDotOhFloat should be (6.9f plusOrMinus 0.2f)
      sevenDotOhFloat should be (7.0f plusOrMinus 0.2f)
      sevenDotOhFloat should be (7.2f plusOrMinus 0.2f)
      sevenDotOhFloat should be (6.8f plusOrMinus 0.2f)
      minusSevenDotOhFloat should be (-7.1f plusOrMinus 0.2f)
      minusSevenDotOhFloat should be (-6.9f plusOrMinus 0.2f)
      minusSevenDotOhFloat should be (-7.0f plusOrMinus 0.2f)
      minusSevenDotOhFloat should be (-7.2f plusOrMinus 0.2f)
      minusSevenDotOhFloat should be (-6.8f plusOrMinus 0.2f)
    }

/*
    it("should do nothing if the floating point number is within the specified range, when used with not") {
      sevenDotOh should not { be (7.5 plusOrMinus 0.2) }
      sevenDotOh should not be (7.5 plusOrMinus 0.2)
      sevenDotOh should not be (6.5 plusOrMinus 0.2)
      minusSevenDotOh should not { be (-7.5 plusOrMinus 0.2) }
      minusSevenDotOh should not be (-7.5 plusOrMinus 0.2)
      minusSevenDotOh should not be (-6.5 plusOrMinus 0.2)
    }

    it("should do nothing if the object is the same instnace as another object, when used in a logical-and expression") {
      obj should ((be theSameInstanceAs (string)) and (be theSameInstanceAs (string)))
      obj should (be theSameInstanceAs (string) and (be theSameInstanceAs (string)))
      obj should (be theSameInstanceAs (string) and be theSameInstanceAs (string))
    }

    it("should do nothing if the object is the same instance as another object, when used in a logical-or expression") {

      obj should ((be theSameInstanceAs (otherString)) or (be theSameInstanceAs (string)))
      obj should (be theSameInstanceAs (otherString) or (be theSameInstanceAs (string)))
      obj should (be theSameInstanceAs (otherString) or be theSameInstanceAs (string))

      obj should ((be theSameInstanceAs (string)) or (be theSameInstanceAs (otherString)))
      obj should (be theSameInstanceAs (string) or (be theSameInstanceAs (otherString)))
      obj should (be theSameInstanceAs (string) or be theSameInstanceAs (otherString))
    }

    it("should do nothing if the object is the same instance as another object, when used in a logical-and expression with not") {

      obj should (not (be theSameInstanceAs (otherString)) and not (be theSameInstanceAs (otherString)))
      obj should ((not be theSameInstanceAs (otherString)) and (not be theSameInstanceAs (otherString)))
      obj should (not be theSameInstanceAs (otherString) and not be theSameInstanceAs (otherString))
    }

    it("should do nothing if the object is the same instance as another object, when used in a logical-or expression with not") {

      obj should (not (be theSameInstanceAs (string)) or not (be theSameInstanceAs (otherString)))
      obj should ((not be theSameInstanceAs (string)) or (not be theSameInstanceAs (otherString)))
      obj should (not be theSameInstanceAs (string) or not be theSameInstanceAs (otherString))

      obj should (not (be theSameInstanceAs (otherString)) or not (be theSameInstanceAs (string)))
      obj should ((not be theSameInstanceAs (otherString)) or (not be theSameInstanceAs (string)))
      obj should (not be theSameInstanceAs (otherString) or not be theSameInstanceAs (string))
    }

    it("should throw AssertionError if the object is not the same instance as another object") {
      val caught1 = intercept[AssertionError] {
        otherString should be theSameInstanceAs (string)
      }
      assert(caught1.getMessage === "\"Hi\" was not the same instance as \"Hi\"")
    }

    it("should throw AssertionError if the object is the same instance as another object, when used with not") {
      val caught1 = intercept[AssertionError] {
        obj should not { be theSameInstanceAs (string) }
      }
      assert(caught1.getMessage === "\"Hi\" was the same instance as \"Hi\"")
      val caught2 = intercept[AssertionError] {
        obj should not be theSameInstanceAs (string)
      }
      assert(caught2.getMessage === "\"Hi\" was the same instance as \"Hi\"")
    }

    it("should throw AssertionError if the object is not the same instance as another object, when used in a logical-and expression") {
      val caught1 = intercept[AssertionError] {
        obj should ((be theSameInstanceAs (string)) and (be theSameInstanceAs (otherString)))
      }
      assert(caught1.getMessage === "\"Hi\" was the same instance as \"Hi\", but \"Hi\" was not the same instance as \"Hi\"")
      val caught2 = intercept[AssertionError] {
        obj should (be theSameInstanceAs (string) and (be theSameInstanceAs (otherString)))
      }
      assert(caught2.getMessage === "\"Hi\" was the same instance as \"Hi\", but \"Hi\" was not the same instance as \"Hi\"")
      val caught3 = intercept[AssertionError] {
        obj should (be theSameInstanceAs (string) and be theSameInstanceAs (otherString))
      }
      assert(caught3.getMessage === "\"Hi\" was the same instance as \"Hi\", but \"Hi\" was not the same instance as \"Hi\"")
    }

    it("should throw AssertionError if the object is not the same instance as another object, when used in a logical-or expression") {

      val caught1 = intercept[AssertionError] {
        obj should ((be theSameInstanceAs (otherString)) or (be theSameInstanceAs (otherString)))
      }
      assert(caught1.getMessage === "\"Hi\" was not the same instance as \"Hi\", and \"Hi\" was not the same instance as \"Hi\"")
      val caught2 = intercept[AssertionError] {
        obj should (be theSameInstanceAs (otherString) or (be theSameInstanceAs (otherString)))
      }
      assert(caught2.getMessage === "\"Hi\" was not the same instance as \"Hi\", and \"Hi\" was not the same instance as \"Hi\"")
      val caught3 = intercept[AssertionError] {
        obj should (be theSameInstanceAs (otherString) or be theSameInstanceAs (otherString))
      }
      assert(caught3.getMessage === "\"Hi\" was not the same instance as \"Hi\", and \"Hi\" was not the same instance as \"Hi\"")
    }

    it("should throw AssertionError if the object is the same instance as another object, when used in a logical-and expression with not") {

      val caught1 = intercept[AssertionError] {
        obj should (not (be theSameInstanceAs (otherString)) and not (be theSameInstanceAs (string)))
      }
      assert(caught1.getMessage === "\"Hi\" was not the same instance as \"Hi\", but \"Hi\" was the same instance as \"Hi\"")
      val caught2 = intercept[AssertionError] {
        obj should ((not be theSameInstanceAs (otherString)) and (not be theSameInstanceAs (string)))
      }
      assert(caught2.getMessage === "\"Hi\" was not the same instance as \"Hi\", but \"Hi\" was the same instance as \"Hi\"")
      val caught3 = intercept[AssertionError] {
        obj should (not be theSameInstanceAs (otherString) and not be theSameInstanceAs (string))
      }
      assert(caught3.getMessage === "\"Hi\" was not the same instance as \"Hi\", but \"Hi\" was the same instance as \"Hi\"")
      // Check that the error message "short circuits"
      val caught7 = intercept[AssertionError] {
        obj should (not (be theSameInstanceAs (string)) and not (be theSameInstanceAs (otherString)))
      }
      assert(caught7.getMessage === "\"Hi\" was the same instance as \"Hi\"")
    }

    it("should throw AssertionError if the object has an appropriately named method, which returns true, when used in a logical-or expression with not") {

      val caught1 = intercept[AssertionError] {
        obj should (not (be theSameInstanceAs (string)) or not (be theSameInstanceAs (string)))
      }
      assert(caught1.getMessage === "\"Hi\" was the same instance as \"Hi\", and \"Hi\" was the same instance as \"Hi\"")
      val caught2 = intercept[AssertionError] {
        obj should ((not be theSameInstanceAs (string)) or (not be theSameInstanceAs (string)))
      }
      assert(caught2.getMessage === "\"Hi\" was the same instance as \"Hi\", and \"Hi\" was the same instance as \"Hi\"")
      val caught3 = intercept[AssertionError] {
        obj should (not be theSameInstanceAs (string) or not be theSameInstanceAs (string))
      }
      assert(caught3.getMessage === "\"Hi\" was the same instance as \"Hi\", and \"Hi\" was the same instance as \"Hi\"")
    }
*/
  }
}
