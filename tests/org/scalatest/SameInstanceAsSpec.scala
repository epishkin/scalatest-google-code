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

class SameInstanceAsSpec extends Spec with ShouldMatchers {

  describe("The be a ('symbol) syntax") {

    val string = "Hi"
    val obj: AnyRef = string
    val otherString = new String("Hi")

    it("should do nothing if the object has an appropriately named method, which returns true") {
      string should be theSameInstanceAs (string)
      obj should be theSameInstanceAs (string)
      string should be theSameInstanceAs (obj)
    }

    it("should do nothing if the object has an appropriately named method, which returns false when used with not") {
      otherString should not { be theSameInstanceAs (string) }
      otherString should not be theSameInstanceAs (string)
    }

    it("should do nothing if the object has an appropriately named method, which returns true, when used in a logical-and expression") {
      obj should ((be theSameInstanceAs (string)) and (be theSameInstanceAs (string)))
      obj should (be theSameInstanceAs (string) and (be theSameInstanceAs (string)))
      obj should (be theSameInstanceAs (string) and be theSameInstanceAs (string))
    }

    it("should do nothing if the object has an appropriately named method, which returns true, when used in a logical-or expression") {

      obj should ((be theSameInstanceAs (otherString)) or (be theSameInstanceAs (string)))
      obj should (be theSameInstanceAs (otherString) or (be theSameInstanceAs (string)))
      obj should (be theSameInstanceAs (otherString) or be theSameInstanceAs (string))

      obj should ((be theSameInstanceAs (string)) or (be theSameInstanceAs (otherString)))
      obj should (be theSameInstanceAs (string) or (be theSameInstanceAs (otherString)))
      obj should (be theSameInstanceAs (string) or be theSameInstanceAs (otherString))
    }

    it("should do nothing if the object has an appropriately named method, which returns false, when used in a logical-and expression with not") {

      obj should (not (be theSameInstanceAs (otherString)) and not (be theSameInstanceAs (otherString)))
      obj should ((not be theSameInstanceAs (otherString)) and (not be theSameInstanceAs (otherString)))
      obj should (not be theSameInstanceAs (otherString) and not be theSameInstanceAs (otherString))
    }

    it("should do nothing if the object has an appropriately named method, which returns false, when used in a logical-or expression with not") {

      obj should (not (be theSameInstanceAs (string)) or not (be theSameInstanceAs (otherString)))
      obj should ((not be theSameInstanceAs (string)) or (not be theSameInstanceAs (otherString)))
      obj should (not be theSameInstanceAs (string) or not be theSameInstanceAs (otherString))

      obj should (not (be theSameInstanceAs (otherString)) or not (be theSameInstanceAs (string)))
      obj should ((not be theSameInstanceAs (otherString)) or (not be theSameInstanceAs (string)))
      obj should (not be theSameInstanceAs (otherString) or not be theSameInstanceAs (string))
    }

/*
    it("should throw AssertionError if the object has an appropriately named method, which returns false") {
      val caught1 = intercept[AssertionError] {
        notFileMock should be a ('file)
      }
      assert(caught1.getMessage === "NotFileMock was not a file")
      val caught2 = intercept[AssertionError] {
        isNotFileMock should be a ('file)
      }
      assert(caught2.getMessage === "IsNotFileMock was not a file")
    }

    it("should throw AssertionError if the object has an appropriately named method, which returns true when used with not") {
      val caught1 = intercept[AssertionError] {
        fileMock should not { be a ('file) }
      }
      assert(caught1.getMessage === "FileMock was a file")
      val caught2 = intercept[AssertionError] {
        fileMock should not be a ('file)
      }
      assert(caught2.getMessage === "FileMock was a file")
      val caught3 = intercept[AssertionError] {
        isFileMock should not { be a ('file) }
      }
      assert(caught3.getMessage === "IsFileMock was a file")
      val caught4 = intercept[AssertionError] {
        isFileMock should not be a ('file)
      }
      assert(caught4.getMessage === "IsFileMock was a file")
    }

    it("should throw AssertionError if the object has an appropriately named method, which returns false, when used in a logical-and expression") {
      val caught1 = intercept[AssertionError] {
        fileMock should ((be a ('file)) and (be a ('directory)))
      }
      assert(caught1.getMessage === "FileMock was a file, but FileMock was not a directory")
      val caught2 = intercept[AssertionError] {
        fileMock should (be a ('file) and (be a ('directory)))
      }
      assert(caught2.getMessage === "FileMock was a file, but FileMock was not a directory")
      val caught3 = intercept[AssertionError] {
        fileMock should (be a ('file) and be a ('directory))
      }
      assert(caught3.getMessage === "FileMock was a file, but FileMock was not a directory")
      val caught4 = intercept[AssertionError] {
        isFileMock should ((be a ('file)) and (be a ('directory)))
      }
      assert(caught4.getMessage === "IsFileMock was a file, but IsFileMock was not a directory")
      val caught5 = intercept[AssertionError] {
        isFileMock should (be a ('file) and (be a ('directory)))
      }
      assert(caught5.getMessage === "IsFileMock was a file, but IsFileMock was not a directory")
      val caught6 = intercept[AssertionError] {
        isFileMock should (be a ('file) and be a ('directory))
      }
      assert(caught6.getMessage === "IsFileMock was a file, but IsFileMock was not a directory")
    }

    it("should throw AssertionError if the object has an appropriately named method, which returns false, when used in a logical-or expression") {

      val caught1 = intercept[AssertionError] {
        notFileMock should ((be a ('file)) or (be a ('file)))
      }
      assert(caught1.getMessage === "NotFileMock was not a file, and NotFileMock was not a file")
      val caught2 = intercept[AssertionError] {
        notFileMock should (be a ('file) or (be a ('file)))
      }
      assert(caught2.getMessage === "NotFileMock was not a file, and NotFileMock was not a file")
      val caught3 = intercept[AssertionError] {
        notFileMock should (be a ('file) or be a ('file))
      }
      assert(caught3.getMessage === "NotFileMock was not a file, and NotFileMock was not a file")
      val caught4 = intercept[AssertionError] {
        isNotFileMock should ((be a ('file)) or (be a ('file)))
      }
      assert(caught4.getMessage === "IsNotFileMock was not a file, and IsNotFileMock was not a file")
      val caught5 = intercept[AssertionError] {
        isNotFileMock should (be a ('file) or (be a ('file)))
      }
      assert(caught5.getMessage === "IsNotFileMock was not a file, and IsNotFileMock was not a file")
      val caught6 = intercept[AssertionError] {
        isNotFileMock should (be a ('file) or be a ('file))
      }
      assert(caught6.getMessage === "IsNotFileMock was not a file, and IsNotFileMock was not a file")
    }

    it("should throw AssertionError if the object has an appropriately named method, which returns true, when used in a logical-and expression with not") {

      val caught1 = intercept[AssertionError] {
        fileMock should (not (be a ('directory)) and not (be a ('file)))
      }
      assert(caught1.getMessage === "FileMock was not a directory, but FileMock was a file")
      val caught2 = intercept[AssertionError] {
        fileMock should ((not be a ('directory)) and (not be a ('file)))
      }
      assert(caught2.getMessage === "FileMock was not a directory, but FileMock was a file")
      val caught3 = intercept[AssertionError] {
        fileMock should (not be a ('directory) and not be a ('file))
      }
      assert(caught3.getMessage === "FileMock was not a directory, but FileMock was a file")
      val caught4 = intercept[AssertionError] {
        isFileMock should (not (be a ('directory)) and not (be a ('file)))
      }
      assert(caught4.getMessage === "IsFileMock was not a directory, but IsFileMock was a file")
      val caught5 = intercept[AssertionError] {
        isFileMock should ((not be a ('directory)) and (not be a ('file)))
      }
      assert(caught5.getMessage === "IsFileMock was not a directory, but IsFileMock was a file")
      val caught6 = intercept[AssertionError] {
        isFileMock should (not be a ('directory) and not be a ('file))
      }
      assert(caught6.getMessage === "IsFileMock was not a directory, but IsFileMock was a file")
      // Check that the error message "short circuits"
      val caught7 = intercept[AssertionError] {
        fileMock should (not (be a ('file)) and not (be a ('directory)))
      }
      assert(caught7.getMessage === "FileMock was a file")
    }

    it("should throw AssertionError if the object has an appropriately named method, which returns true, when used in a logical-or expression with not") {

      val caught1 = intercept[AssertionError] {
        fileMock should (not (be a ('file)) or not (be a ('file)))
      }
      assert(caught1.getMessage === "FileMock was a file, and FileMock was a file")
      val caught2 = intercept[AssertionError] {
        fileMock should ((not be a ('file)) or (not be a ('file)))
      }
      assert(caught2.getMessage === "FileMock was a file, and FileMock was a file")
      val caught3 = intercept[AssertionError] {
        fileMock should (not be a ('file) or not be a ('file))
      }
      assert(caught3.getMessage === "FileMock was a file, and FileMock was a file")
      val caught4 = intercept[AssertionError] {
        isFileMock should (not (be a ('file)) or not (be a ('file)))
      }
      assert(caught4.getMessage === "IsFileMock was a file, and IsFileMock was a file")
      val caught5 = intercept[AssertionError] {
        isFileMock should ((not be a ('file)) or (not be a ('file)))
      }
      assert(caught5.getMessage === "IsFileMock was a file, and IsFileMock was a file")
      val caught6 = intercept[AssertionError] {
        isFileMock should (not be a ('file) or not be a ('file))
      }
      assert(caught6.getMessage === "IsFileMock was a file, and IsFileMock was a file")
    }
*/
  }
}
