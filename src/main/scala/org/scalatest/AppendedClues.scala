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

trait AppendedClues {

  class Clueful(fun: => Any) {
    def withClue(clue: Any) {
      def append(currentMessage: Option[String]) =
        currentMessage match {
          case Some(msg) =>
            if (clue.toString.head.isWhitespace)
              Some(msg + clue.toString)
            else
              Some(msg + " " + clue.toString)
          case None => Some(clue.toString)
        }
      try {
        fun
      }
      catch {
        case e: ModifiableMessage[_] =>
          if (clue != "")
            throw e.modifyMessage(append)
          else
            throw e
      }
    }
  }

  implicit def convertToClueful(fun: => Any) = new Clueful(fun)
}

object AppendedClues extends AppendedClues
