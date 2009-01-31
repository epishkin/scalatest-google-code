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

class BeASymbolSpec extends Spec with ShouldMatchers with FileMocks {

  describe("The be a ('symbol) syntax") {

    it("should do nothing if the object has an appropriately named method, which returns true") {
      fileMock should be a ('file)
      isFileMock should be a ('file)
    }

    it("should throw IllegalArgumentException if no <symbol> or is<Symbol> method exists") {
      val ex1 = intercept[IllegalArgumentException] {
        noPredicateMock should be a ('apple)
      }
      ex1.getMessage should equal ("NoPredicateMock has neither an apple nor an isApple method")
      // Check message for name that starts with a consonant (should use a instead of an)
      val ex2 = intercept[IllegalArgumentException] {
        noPredicateMock should be a ('file)
      }
      ex2.getMessage should equal ("NoPredicateMock has neither a file nor an isFile method")
    }

    it("should do nothing if the object has an appropriately named method, which returns false when used with not") {
      notFileMock should not { be a ('file) }
      notFileMock should not be a ('file)
      isNotFileMock should not { be a ('file) }
      isNotFileMock should not be a ('file)
    }

    it("should throw IllegalArgumentException if no <symbol> or is<Symbol> method exists, when used with not") {
      val ex1 = intercept[IllegalArgumentException] {
        noPredicateMock should not { be a ('apple) }
      }
      ex1.getMessage should equal ("NoPredicateMock has neither an apple nor an isApple method")
      val ex2 = intercept[IllegalArgumentException] {
        noPredicateMock should not (be a ('directory))
      }
      ex2.getMessage should equal ("NoPredicateMock has neither a directory nor an isDirectory method")
      val ex3 = intercept[IllegalArgumentException] {
        noPredicateMock should not be a ('apple)
      }
      ex3.getMessage should equal ("NoPredicateMock has neither an apple nor an isApple method")
      val ex4 = intercept[IllegalArgumentException] {
        noPredicateMock should not be a ('directory)
      }
      ex4.getMessage should equal ("NoPredicateMock has neither a directory nor an isDirectory method")
    }

    it("should do nothing if the object has an appropriately named method, which returns true, when used in a logical-and expression") {
      fileMock should ((be a ('file)) and (be a ('file)))
      fileMock should (be a ('file) and (be a ('file)))
      fileMock should (be a ('file) and be a ('file))
      isFileMock should ((be a ('file)) and (be a ('file)))
      isFileMock should (be a ('file) and (be a ('file)))
      isFileMock should (be a ('file) and be a ('file))
    }

    it("should do nothing if the object has an appropriately named method, which returns true, when used in a logical-or expression") {

      fileMock should ((be a ('directory)) or (be ('file)))
      fileMock should (be a ('directory) or (be ('file)))
      fileMock should (be a ('directory) or be ('file))
      isFileMock should ((be a ('directory)) or (be ('file)))
      isFileMock should (be a ('directory) or (be ('file)))
      isFileMock should (be a ('directory) or be ('file))

      fileMock should ((be a ('file)) or (be a ('directory)))
      fileMock should (be a ('file) or (be a ('directory)))
      fileMock should (be a ('file) or be a ('directory))
      isFileMock should ((be a ('file)) or (be a ('directory)))
      isFileMock should (be a ('file) or (be a ('directory)))
      isFileMock should (be a ('file) or be a ('directory))
    }

    it("should do nothing if the object has an appropriately named method, which returns false, when used in a logical-and expression with not") {

      notFileMock should (not (be a ('file)) and not (be a ('file)))
      notFileMock should ((not be a ('file)) and (not be a ('file)))
      notFileMock should (not be a ('file) and not be a ('file))

      isNotFileMock should (not (be a ('file)) and not (be a ('file)))
      isNotFileMock should ((not be a ('file)) and (not be a ('file)))
      isNotFileMock should (not be a ('file) and not be a ('file))
    }

    it("should do nothing if the object has an appropriately named method, which returns false, when used in a logical-or expression with not") {

      notFileMock should (not (be a ('file)) or not (be a ('file)))
      notFileMock should ((not be a ('file)) or (not be a ('file)))
      notFileMock should (not be a ('file) or not be a ('file))

      isNotFileMock should (not (be a ('file)) or not (be a ('file)))
      isNotFileMock should ((not be a ('file)) or (not be a ('file)))
      isNotFileMock should (not be a ('file) or not be a ('file))

      notFileMock should (not (be a ('directory)) or not (be a ('file)))
      notFileMock should ((not be a ('directory)) or (not be a ('file)))
      notFileMock should (not be a ('directory) or not be a ('file))

      isNotFileMock should (not (be a ('directory)) or not (be a ('file)))
      isNotFileMock should ((not be a ('directory)) or (not be a ('file)))
      isNotFileMock should (not be a ('directory) or not be a ('file))
    }

/*
    it("should throw AssertionError if the object has an appropriately named method, which returns false") {
      val caught1 = intercept[AssertionError] {
        notFileMock should be ('file)
      }
      assert(caught1.getMessage === "NotFileMock was not file")
      val caught2 = intercept[AssertionError] {
        isNotFileMock should be ('file)
      }
      assert(caught2.getMessage === "IsNotFileMock was not file")
    }

    it("should throw AssertionError if the object has an appropriately named method, which returns true when used with not") {
      val caught1 = intercept[AssertionError] {
        fileMock should not { be ('file) }
      }
      assert(caught1.getMessage === "FileMock was file")
      val caught2 = intercept[AssertionError] {
        fileMock should not be ('file)
      }
      assert(caught2.getMessage === "FileMock was file")
      val caught3 = intercept[AssertionError] {
        isFileMock should not { be ('file) }
      }
      assert(caught3.getMessage === "IsFileMock was file")
      val caught4 = intercept[AssertionError] {
        isFileMock should not be ('file)
      }
      assert(caught4.getMessage === "IsFileMock was file")
    }

    it("should throw AssertionError if the object has an appropriately named method, which returns false, when used in a logical-and expression") {
      val caught1 = intercept[AssertionError] {
        fileMock should ((be ('file)) and (be ('directory)))
      }
      assert(caught1.getMessage === "FileMock was file, but FileMock was not directory")
      val caught2 = intercept[AssertionError] {
        fileMock should (be ('file) and (be ('directory)))
      }
      assert(caught2.getMessage === "FileMock was file, but FileMock was not directory")
      val caught3 = intercept[AssertionError] {
        fileMock should (be ('file) and be ('directory))
      }
      assert(caught3.getMessage === "FileMock was file, but FileMock was not directory")
      val caught4 = intercept[AssertionError] {
        isFileMock should ((be ('file)) and (be ('directory)))
      }
      assert(caught4.getMessage === "IsFileMock was file, but IsFileMock was not directory")
      val caught5 = intercept[AssertionError] {
        isFileMock should (be ('file) and (be ('directory)))
      }
      assert(caught5.getMessage === "IsFileMock was file, but IsFileMock was not directory")
      val caught6 = intercept[AssertionError] {
        isFileMock should (be ('file) and be ('directory))
      }
      assert(caught6.getMessage === "IsFileMock was file, but IsFileMock was not directory")
    }

    it("should throw AssertionError if the object has an appropriately named method, which returns false, when used in a logical-or expression") {

      val caught1 = intercept[AssertionError] {
        notFileMock should ((be ('file)) or (be ('file)))
      }
      assert(caught1.getMessage === "NotFileMock was not file, and NotFileMock was not file")
      val caught2 = intercept[AssertionError] {
        notFileMock should (be ('file) or (be ('file)))
      }
      assert(caught2.getMessage === "NotFileMock was not file, and NotFileMock was not file")
      val caught3 = intercept[AssertionError] {
        notFileMock should (be ('file) or be ('file))
      }
      assert(caught3.getMessage === "NotFileMock was not file, and NotFileMock was not file")
      val caught4 = intercept[AssertionError] {
        isNotFileMock should ((be ('file)) or (be ('file)))
      }
      assert(caught4.getMessage === "IsNotFileMock was not file, and IsNotFileMock was not file")
      val caught5 = intercept[AssertionError] {
        isNotFileMock should (be ('file) or (be ('file)))
      }
      assert(caught5.getMessage === "IsNotFileMock was not file, and IsNotFileMock was not file")
      val caught6 = intercept[AssertionError] {
        isNotFileMock should (be ('file) or be ('file))
      }
      assert(caught6.getMessage === "IsNotFileMock was not file, and IsNotFileMock was not file")
    }

    it("should throw AssertionError if the object has an appropriately named method, which returns true, when used in a logical-and expression with not") {

      val caught1 = intercept[AssertionError] {
        fileMock should (not (be ('directory)) and not (be ('file)))
      }
      assert(caught1.getMessage === "FileMock was not directory, but FileMock was file")
      val caught2 = intercept[AssertionError] {
        fileMock should ((not be ('directory)) and (not be ('file)))
      }
      assert(caught2.getMessage === "FileMock was not directory, but FileMock was file")
      val caught3 = intercept[AssertionError] {
        fileMock should (not be ('directory) and not be ('file))
      }
      assert(caught3.getMessage === "FileMock was not directory, but FileMock was file")
      val caught4 = intercept[AssertionError] {
        isFileMock should (not (be ('directory)) and not (be ('file)))
      }
      assert(caught4.getMessage === "IsFileMock was not directory, but IsFileMock was file")
      val caught5 = intercept[AssertionError] {
        isFileMock should ((not be ('directory)) and (not be ('file)))
      }
      assert(caught5.getMessage === "IsFileMock was not directory, but IsFileMock was file")
      val caught6 = intercept[AssertionError] {
        isFileMock should (not be ('directory) and not be ('file))
      }
      assert(caught6.getMessage === "IsFileMock was not directory, but IsFileMock was file")
      // Check that the error message "short circuits"
      val caught7 = intercept[AssertionError] {
        fileMock should (not (be ('file)) and not (be ('directory)))
      }
      assert(caught7.getMessage === "FileMock was file")
    }

    it("should throw AssertionError if the object has an appropriately named method, which returns true, when used in a logical-or expression with not") {

      val caught1 = intercept[AssertionError] {
        fileMock should (not (be ('file)) or not (be ('file)))
      }
      assert(caught1.getMessage === "FileMock was file, and FileMock was file")
      val caught2 = intercept[AssertionError] {
        fileMock should ((not be ('file)) or (not be ('file)))
      }
      assert(caught2.getMessage === "FileMock was file, and FileMock was file")
      val caught3 = intercept[AssertionError] {
        fileMock should (not be ('file) or not be ('file))
      }
      assert(caught3.getMessage === "FileMock was file, and FileMock was file")
      val caught4 = intercept[AssertionError] {
        isFileMock should (not (be ('file)) or not (be ('file)))
      }
      assert(caught4.getMessage === "IsFileMock was file, and IsFileMock was file")
      val caught5 = intercept[AssertionError] {
        isFileMock should ((not be ('file)) or (not be ('file)))
      }
      assert(caught5.getMessage === "IsFileMock was file, and IsFileMock was file")
      val caught6 = intercept[AssertionError] {
        isFileMock should (not be ('file) or not be ('file))
      }
      assert(caught6.getMessage === "IsFileMock was file, and IsFileMock was file")
    }
*/
  }
}
