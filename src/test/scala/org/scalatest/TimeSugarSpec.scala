/*
 * Copyright 2001-2012 Artima, Inc.
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

class TimeSugarSpec extends FunSpec {
  
  import TimeSugar._
 
  describe("The TimeSugar trait") {
    
    it("should provide implicit conversions for Int durations") {
      assert((1 millisecond) === 1)
      assert((2 milliseconds) === 2)
      assert((2 millis) === 2)
      assert((2 seconds) === 2 * 1000)
      assert((1 second) === 1000)
      assert((2 seconds) === 2 * 1000)
      assert((1 minute) === 1000 * 60)
      assert((2 minutes) === 2 * 1000 * 60)
      assert((1 hour) === 1000 * 60 * 60)
      assert((2 hours) === 2 * 1000 * 60 * 60)
      assert((1 day) === 1000 * 60 * 60 * 24)
      assert((2 days) === 2 * 1000 * 60 * 60 * 24)
    }
    
    it("should provide implicit conversions for Long durations") {
      assert((1L millisecond) === 1)
      assert((2L milliseconds) === 2)
      assert((2L millis) === 2)
      assert((2L seconds) === 2 * 1000)
      assert((1L second) === 1000)
      assert((2L seconds) === 2 * 1000)
      assert((1L minute) === 1000 * 60)
      assert((2L minutes) === 2 * 1000 * 60)
      assert((1L hour) === 1000 * 60 * 60)
      assert((2L hours) === 2 * 1000 * 60 * 60)
      assert((1L day) === 1000 * 60 * 60 * 24)
      assert((2L days) === 2 * 1000 * 60 * 60 * 24)
    }
    
    it("should provide an implicit conversion from GrainOfTime to Long") {
      def getALong(aLong: Long) = aLong
      assert(getALong(1 millisecond) === 1)
      assert(getALong(2 milliseconds) === 2)
      assert(getALong(2 millis) === 2)
      assert(getALong(2 seconds) === 2 * 1000)
      assert(getALong(1 second) === 1000)
      assert(getALong(2 seconds) === 2 * 1000)
      assert(getALong(1 minute) === 1000 * 60)
      assert(getALong(2 minutes) === 2 * 1000 * 60)
      assert(getALong(1 hour) === 1000 * 60 * 60)
      assert(getALong(2 hours) === 2 * 1000 * 60 * 60)
      assert(getALong(1 day) === 1000 * 60 * 60 * 24)
      assert(getALong(2 days) === 2 * 1000 * 60 * 60 * 24)
      assert(getALong(1L millisecond) === 1)
      assert(getALong(2L milliseconds) === 2)
      assert(getALong(2L millis) === 2)
      assert(getALong(2L seconds) === 2 * 1000)
      assert(getALong(1L second) === 1000)
      assert(getALong(2L seconds) === 2 * 1000)
      assert(getALong(1L minute) === 1000 * 60)
      assert(getALong(2L minutes) === 2 * 1000 * 60)
      assert(getALong(1L hour) === 1000 * 60 * 60)
      assert(getALong(2L hours) === 2 * 1000 * 60 * 60)
      assert(getALong(1L day) === 1000 * 60 * 60 * 24)
      assert(getALong(2L days) === 2 * 1000 * 60 * 60 * 24)
    }
  }
}