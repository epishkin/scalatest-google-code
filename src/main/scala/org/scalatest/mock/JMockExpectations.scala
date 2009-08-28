/*
 * Copyright 2001-2009 Artima, Inc.
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
package org.scalatest.mock

import org.jmock.Expectations
import org.hamcrest.Matcher

class JMockExpectations extends Expectations {

  def withArg[T](value: T): T = `with`(value)
  def withArg(value: Int): Int = `with`(value)
  def withArg(value: Short): Short = `with`(value)
  def withArg(value: Byte): Byte = `with`(value)
  def withArg(value: Long): Long = `with`(value)
  def withArg(value: Boolean): Boolean = `with`(value)
  def withArg(value: Float): Float = `with`(value)
  def withArg(value: Double): Double = `with`(value)
  def withArg(value: Char): Char = `with`(value)

  def withArg[T](matcher: Matcher[T]): T = `with`(matcher)
  def withArg(matcher: Matcher[Int]): Int = `with`(matcher)
  def withArg(matcher: Matcher[Short]): Short = `with`(matcher)
  def withArg(matcher: Matcher[Byte]): Byte = `with`(matcher)
  def withArg(matcher: Matcher[Long]): Long = `with`(matcher)
  def withArg(matcher: Matcher[Boolean]): Boolean = `with`(matcher)
  def withArg(matcher: Matcher[Float]): Float = `with`(matcher)
  def withArg(matcher: Matcher[Double]): Double = `with`(matcher)
  def withArg(matcher: Matcher[Char]): Char = `with`(matcher)
}