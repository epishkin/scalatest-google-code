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

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.prop.TableDrivenPropertyChecks

class StackDepthExceptionSpec extends Spec with ShouldMatchers with TableDrivenPropertyChecks {

  class FunException(
    messageFun: Option[StackDepthException => String],
    cause: Option[Throwable],
    failedCodeStackDepthFun: StackDepthException => Int
  ) extends StackDepthException(messageFun, cause, failedCodeStackDepthFun) {
    def severedAtStackDepth: FunException = {
      val truncated = getStackTrace.drop(failedCodeStackDepth)
      val e = new FunException(messageFun, cause, e => 0)
      e.setStackTrace(truncated)
      e
    }
  }

  class NoFunException(
    message: Option[String],
    cause: Option[Throwable],
    failedCodeStackDepth: Int
  ) extends StackDepthException(message, cause, failedCodeStackDepth) {
    def severedAtStackDepth: NoFunException = {
      val truncated = getStackTrace.drop(failedCodeStackDepth)
      val e = new NoFunException(message, cause, 0)
      e.setStackTrace(truncated)
      e
    }
  }

  describe("A StackDepthException") {
    it("should throw NPE if passed nulls or Some(null)s") {

      val invalidFunCombos =
        Table[Option[StackDepthException => String], Option[Throwable], StackDepthException => Int](
          ("messageFun",     "cause",              "failedCodeStackDepthFun"),
          (null,             Some(new Exception),  e => 17),
          (Some(null),       Some(new Exception),  e => 17),
          (Some(e => "hi"),  null,                 e => 17),
          (Some(e => "hi"),  Some(null),           e => 17),
          (Some(e => "hi"),  Some(new Exception),  null)
        )

      forAll (invalidFunCombos) { (msgFun, cause, fcsdFun) =>
        evaluating {
          new FunException(msgFun, cause, fcsdFun)
        } should produce [NullPointerException]
      }

      val invalidNoFunCombos =
        Table(
          ("message",   "cause"),
          (null,        Some(new Exception)),
          (Some(null),  Some(new Exception)),
          (Some("hi"),  null),
          (Some("hi"),  Some(null))
        )

      forAll (invalidNoFunCombos) { (msg, cause) =>
        evaluating {
          new NoFunException(msg, cause, 17)
        } should produce [NullPointerException]
      }
    }

    it("should produce the Some(message) from getMessage, or null if message was None") {
      
      val eDefined = new NoFunException(Some("howdy!"), None, 17)
      eDefined.getMessage should be ("howdy!")
      
      val eEmpty = new NoFunException(None, None, 17)
      eEmpty.getMessage should be (null)
    }

    it("should produce the Some(cause) from getCause, or null if cause was None") {
      
      val e = new Exception

      val eDefined = new NoFunException(Some("howdy!"), Some(e), 17)
      eDefined.getCause should be (e)
      
      val eEmpty = new NoFunException(Some("howdy!"), None, 17)
      eEmpty.getCause should be (null)
    }

    it("should produce the Some(message) from message, or None if message was None") {
      
      val eDefined = new NoFunException(Some("howdy!"), None, 17)
      eDefined.message should be (Some("howdy!"))
      
      val eEmpty = new NoFunException(None, None, 17)
      eEmpty.message should be (None)
    }

    it("should produce the Some(cause) from cause, or None if cause was None") {
      
      val e = new Exception

      val eDefined = new NoFunException(Some("howdy!"), Some(e), 17)
      eDefined.cause should be (Some(e))
      
      val eEmpty = new NoFunException(Some("howdy!"), None, 17)
      eEmpty.cause should be (None)
    }
  }
}
 
