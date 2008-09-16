package org.scalatest.matchers

import Matchers._

class MatcherSpec extends Spec {
  "The equal matcher" -- {
    "should do nothing when equal" - {
      1 should equal (1)
    }
    "should throw an assertion error when not equal" - {
      intercept(classOf[AssertionError]) {
        1 should equal (2)
      }
    }
  }
  "The be matcher" -- {
    "should do nothing when equal" - {
      false should be (false)
    }
    "should throw an assertion error when not equal" - {
      intercept(classOf[AssertionError]) {
        false should be (true)
      }
    }
  }
  "The not matcher" -- {
    "should do nothing when not true" - {
      1 should not { equal (2) }
    }
    "should throw an assertion error when true" - {
      intercept(classOf[AssertionError]) {
        1 should not { equal (1) }
      }
    }
  }
  "The endsWith matcher" -- {
    "should do nothing when true" - {
      "Hello, world" should endWith ("world")
    }
    "should throw an assertion error when not true" - {
      intercept(classOf[AssertionError]) {
        "Hello, world" should endWith ("planet")
      }
    }
  }

  "The and matcher" -- {
    "should do nothing when both operands are true" - {
      1 should { equal (1) and equal (2 - 1) }
    }
    "should throw AssertionError when first operands is false" - {
      intercept(classOf[AssertionError]) {
        1 should (equal (2) and equal (1))
      }
    }
    "should throw AssertionError when second operands is false" - {
      intercept(classOf[AssertionError]) {
        1 should (equal (1) and equal (2))
      }
    }
  }
}
