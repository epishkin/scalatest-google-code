
package org.scalatest.tools

import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers

class SbtCommandParserSpec extends Spec with ShouldMatchers {

  describe("the cmd terminal?") {
    it("should parse 'st'") {

      val parser = new SbtCommandParser
      val result = parser.parseResult("""st""")
      result match {
        case parser.Success(result, _) => println("success: " + result)
        case ns: parser.NoSuccess => fail(ns.toString)
      }
    }
    it("should parse 'st --'") {

      val parser = new SbtCommandParser
      val result = parser.parseResult("""st --""")
      result match {
        case parser.Success(result, _) => println("success: " + result)
        case ns: parser.NoSuccess => fail(ns.toString)
      }
    }
  }
}

