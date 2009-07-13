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
package org.scalatest.concurrent

private[concurrent] trait PrintlnLogger extends Logger {
  override def log(a: Any):Unit = {
    println(a)
    super.log(a)
  }
}

private[concurrent] trait Logger {

  def log(a: Any): Unit = {}
  var logLevel: LogLevel = nothing
  def logger = this

  case class LogLevel(level: Int) {
    def apply(a: Any) {
      if (this.level <= logLevel.level) log(a)
    }

    def around[T](a: => Any)(f: => T): T = {
      if (this.level <= logLevel.level) {
        log("|starting: " + a)
        val t = f
        log("|done with: " + a)
        t
      }
      else f
    }
  }

  case object everything extends LogLevel(10)
  case object trace      extends LogLevel(6)
  case object debug      extends LogLevel(5)
  case object warn       extends LogLevel(4)
  case object serious    extends LogLevel(3)
  case object error      extends LogLevel(2)
  case object critical   extends LogLevel(1)
  case object nothing    extends LogLevel(0)
}
