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

  describe("The be (X plusOrMinus Y) syntax") {

    val sevenDotOh = 7.0
    val minusSevenDotOh = -7.0
    val sevenDotOhFloat = 7.0f
    val minusSevenDotOhFloat = -7.0f
    val sevenLong = 7L
    val minusSevenLong = -7L
    val sevenInt = 7
    val minusSevenInt = -7
    val sevenShort: Short = 7
    val minusSevenShort: Short = -7
    val sevenByte: Byte = 7
    val minusSevenByte: Byte = -7

    /*
      I decided that for X plusOrMinus Y, Y can be any numeric type that's implicitly
      convertible to X. So if X is Double, Y could be Double, Float, Long, Int, Short, Byte.
      If X is Long, Y could be Long, Int, Short, Byte. If X is Short, Y could be Short or Byte.
      And if X is Byte, Y must be Byte.
      minusSevenDotOhFloat should be (-6.8f plusOrMinus 0.2d)
    */
/*
    it("should do nothing if the number is within the specified range") {

      // Double plusOrMinus Double
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

      // Double plusOrMinus Float
      sevenDotOh should be (7.1 plusOrMinus 0.2f)
      sevenDotOh should be (6.9 plusOrMinus 0.2f)
      sevenDotOh should be (7.0 plusOrMinus 0.2f)
      sevenDotOh should be (7.2 plusOrMinus 0.2f)
      sevenDotOh should be (6.8 plusOrMinus 0.2f)
      minusSevenDotOh should be (-7.1 plusOrMinus 0.2f)
      minusSevenDotOh should be (-6.9 plusOrMinus 0.2f)
      minusSevenDotOh should be (-7.0 plusOrMinus 0.2f)
      minusSevenDotOh should be (-7.2 plusOrMinus 0.2f)
      minusSevenDotOh should be (-6.8 plusOrMinus 0.2f)

      // Double plusOrMinus Long
      sevenDotOh should be (7.1 plusOrMinus 2L)
      sevenDotOh should be (6.9 plusOrMinus 2L)
      sevenDotOh should be (7.0 plusOrMinus 2L)
      sevenDotOh should be (7.2 plusOrMinus 2L)
      sevenDotOh should be (6.8 plusOrMinus 2L)
      minusSevenDotOh should be (-7.1 plusOrMinus 2L)
      minusSevenDotOh should be (-6.9 plusOrMinus 2L)
      minusSevenDotOh should be (-7.0 plusOrMinus 2L)
      minusSevenDotOh should be (-7.2 plusOrMinus 2L)
      minusSevenDotOh should be (-6.8 plusOrMinus 2L)

      // Double plusOrMinus Int
      sevenDotOh should be (7.1 plusOrMinus 2)
      sevenDotOh should be (6.9 plusOrMinus 2)
      sevenDotOh should be (7.0 plusOrMinus 2)
      sevenDotOh should be (7.2 plusOrMinus 2)
      sevenDotOh should be (6.8 plusOrMinus 2)
      minusSevenDotOh should be (-7.1 plusOrMinus 2)
      minusSevenDotOh should be (-6.9 plusOrMinus 2)
      minusSevenDotOh should be (-7.0 plusOrMinus 2)
      minusSevenDotOh should be (-7.2 plusOrMinus 2)
      minusSevenDotOh should be (-6.8 plusOrMinus 2)

      // Double plusOrMinus Short
      sevenDotOh should be (7.1 plusOrMinus 2.toShort)
      sevenDotOh should be (6.9 plusOrMinus 2.toShort)
      sevenDotOh should be (7.0 plusOrMinus 2.toShort)
      sevenDotOh should be (7.2 plusOrMinus 2.toShort)
      sevenDotOh should be (6.8 plusOrMinus 2.toShort)
      minusSevenDotOh should be (-7.1 plusOrMinus 2.toShort)
      minusSevenDotOh should be (-6.9 plusOrMinus 2.toShort)
      minusSevenDotOh should be (-7.0 plusOrMinus 2.toShort)
      minusSevenDotOh should be (-7.2 plusOrMinus 2.toShort)
      minusSevenDotOh should be (-6.8 plusOrMinus 2.toShort)

      // Double plusOrMinus Byte
      sevenDotOh should be (7.1 plusOrMinus 2.toByte)
      sevenDotOh should be (6.9 plusOrMinus 2.toByte)
      sevenDotOh should be (7.0 plusOrMinus 2.toByte)
      sevenDotOh should be (7.2 plusOrMinus 2.toByte)
      sevenDotOh should be (6.8 plusOrMinus 2.toByte)
      minusSevenDotOh should be (-7.1 plusOrMinus 2.toByte)
      minusSevenDotOh should be (-6.9 plusOrMinus 2.toByte)
      minusSevenDotOh should be (-7.0 plusOrMinus 2.toByte)
      minusSevenDotOh should be (-7.2 plusOrMinus 2.toByte)
      minusSevenDotOh should be (-6.8 plusOrMinus 2.toByte)

      // Float plusOrMinus Float
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

      // Float plusOrMinus Long
      sevenDotOhFloat should be (7.1f plusOrMinus 2L)
      sevenDotOhFloat should be (6.9f plusOrMinus 2L)
      sevenDotOhFloat should be (7.0f plusOrMinus 2L)
      sevenDotOhFloat should be (7.2f plusOrMinus 2L)
      sevenDotOhFloat should be (6.8f plusOrMinus 2L)
      minusSevenDotOhFloat should be (-7.1f plusOrMinus 2L)
      minusSevenDotOhFloat should be (-6.9f plusOrMinus 2L)
      minusSevenDotOhFloat should be (-7.0f plusOrMinus 2L)
      minusSevenDotOhFloat should be (-7.2f plusOrMinus 2L)
      minusSevenDotOhFloat should be (-6.8f plusOrMinus 2L)

      // Float plusOrMinus Int
      sevenDotOhFloat should be (7.1f plusOrMinus 2)
      sevenDotOhFloat should be (6.9f plusOrMinus 2)
      sevenDotOhFloat should be (7.0f plusOrMinus 2)
      sevenDotOhFloat should be (7.2f plusOrMinus 2)
      sevenDotOhFloat should be (6.8f plusOrMinus 2)
      minusSevenDotOhFloat should be (-7.1f plusOrMinus 2)
      minusSevenDotOhFloat should be (-6.9f plusOrMinus 2)
      minusSevenDotOhFloat should be (-7.0f plusOrMinus 2)
      minusSevenDotOhFloat should be (-7.2f plusOrMinus 2)
      minusSevenDotOhFloat should be (-6.8f plusOrMinus 2)

      // Float plusOrMinus Short
      sevenDotOhFloat should be (7.1f plusOrMinus 2.toShort)
      sevenDotOhFloat should be (6.9f plusOrMinus 2.toShort)
      sevenDotOhFloat should be (7.0f plusOrMinus 2.toShort)
      sevenDotOhFloat should be (7.2f plusOrMinus 2.toShort)
      sevenDotOhFloat should be (6.8f plusOrMinus 2.toShort)
      minusSevenDotOhFloat should be (-7.1f plusOrMinus 2.toShort)
      minusSevenDotOhFloat should be (-6.9f plusOrMinus 2.toShort)
      minusSevenDotOhFloat should be (-7.0f plusOrMinus 2.toShort)
      minusSevenDotOhFloat should be (-7.2f plusOrMinus 2.toShort)
      minusSevenDotOhFloat should be (-6.8f plusOrMinus 2.toShort)

      // Float plusOrMinus Byte
      sevenDotOhFloat should be (7.1f plusOrMinus 2.toByte)
      sevenDotOhFloat should be (6.9f plusOrMinus 2.toByte)
      sevenDotOhFloat should be (7.0f plusOrMinus 2.toByte)
      sevenDotOhFloat should be (7.2f plusOrMinus 2.toByte)
      sevenDotOhFloat should be (6.8f plusOrMinus 2.toByte)
      minusSevenDotOhFloat should be (-7.1f plusOrMinus 2.toByte)
      minusSevenDotOhFloat should be (-6.9f plusOrMinus 2.toByte)
      minusSevenDotOhFloat should be (-7.0f plusOrMinus 2.toByte)
      minusSevenDotOhFloat should be (-7.2f plusOrMinus 2.toByte)
      minusSevenDotOhFloat should be (-6.8f plusOrMinus 2.toByte)

      // Long plusOrMinus Long
      sevenLong should be (9L plusOrMinus 2L)
      sevenLong should be (8L plusOrMinus 2L)
      sevenLong should be (7L plusOrMinus 2L)
      sevenLong should be (6L plusOrMinus 2L)
      sevenLong should be (5L plusOrMinus 2L)
      minusSevenLong should be (-9L plusOrMinus 2L)
      minusSevenLong should be (-8L plusOrMinus 2L)
      minusSevenLong should be (-7L plusOrMinus 2L)
      minusSevenLong should be (-6L plusOrMinus 2L)
      minusSevenLong should be (-5L plusOrMinus 2L)

      // Long plusOrMinus Int
      sevenLong should be (9L plusOrMinus 2)
      sevenLong should be (8L plusOrMinus 2)
      sevenLong should be (7L plusOrMinus 2)
      sevenLong should be (6L plusOrMinus 2)
      sevenLong should be (5L plusOrMinus 2)
      minusSevenLong should be (-9L plusOrMinus 2)
      minusSevenLong should be (-8L plusOrMinus 2)
      minusSevenLong should be (-7L plusOrMinus 2)
      minusSevenLong should be (-6L plusOrMinus 2)
      minusSevenLong should be (-5L plusOrMinus 2)

      // Long plusOrMinus Short
      sevenLong should be (9L plusOrMinus 2.toShort)
      sevenLong should be (8L plusOrMinus 2.toShort)
      sevenLong should be (7L plusOrMinus 2.toShort)
      sevenLong should be (6L plusOrMinus 2.toShort)
      sevenLong should be (5L plusOrMinus 2.toShort)
      minusSevenLong should be (-9L plusOrMinus 2.toShort)
      minusSevenLong should be (-8L plusOrMinus 2.toShort)
      minusSevenLong should be (-7L plusOrMinus 2.toShort)
      minusSevenLong should be (-6L plusOrMinus 2.toShort)
      minusSevenLong should be (-5L plusOrMinus 2.toShort)

      // Long plusOrMinus Byte
      sevenLong should be (9L plusOrMinus 2.toByte)
      sevenLong should be (8L plusOrMinus 2.toByte)
      sevenLong should be (7L plusOrMinus 2.toByte)
      sevenLong should be (6L plusOrMinus 2.toByte)
      sevenLong should be (5L plusOrMinus 2.toByte)
      minusSevenLong should be (-9L plusOrMinus 2.toByte)
      minusSevenLong should be (-8L plusOrMinus 2.toByte)
      minusSevenLong should be (-7L plusOrMinus 2.toByte)
      minusSevenLong should be (-6L plusOrMinus 2.toByte)
      minusSevenLong should be (-5L plusOrMinus 2.toByte)

      // Int plusOrMinus Int
      sevenInt should be (9 plusOrMinus 2)
      sevenInt should be (8 plusOrMinus 2)
      sevenInt should be (7 plusOrMinus 2)
      sevenInt should be (6 plusOrMinus 2)
      sevenInt should be (5 plusOrMinus 2)
      minusSevenInt should be (-9 plusOrMinus 2)
      minusSevenInt should be (-8 plusOrMinus 2)
      minusSevenInt should be (-7 plusOrMinus 2)
      minusSevenInt should be (-6 plusOrMinus 2)
      minusSevenInt should be (-5 plusOrMinus 2)

      // Int plusOrMinus Short
      sevenInt should be (9 plusOrMinus 2.toShort)
      sevenInt should be (8 plusOrMinus 2.toShort)
      sevenInt should be (7 plusOrMinus 2.toShort)
      sevenInt should be (6 plusOrMinus 2.toShort)
      sevenInt should be (5 plusOrMinus 2.toShort)
      minusSevenInt should be (-9 plusOrMinus 2.toShort)
      minusSevenInt should be (-8 plusOrMinus 2.toShort)
      minusSevenInt should be (-7 plusOrMinus 2.toShort)
      minusSevenInt should be (-6 plusOrMinus 2.toShort)
      minusSevenInt should be (-5 plusOrMinus 2.toShort)

      // Int plusOrMinus Byte
      sevenInt should be (9 plusOrMinus 2.toByte)
      sevenInt should be (8 plusOrMinus 2.toByte)
      sevenInt should be (7 plusOrMinus 2.toByte)
      sevenInt should be (6 plusOrMinus 2.toByte)
      sevenInt should be (5 plusOrMinus 2.toByte)
      minusSevenInt should be (-9 plusOrMinus 2.toByte)
      minusSevenInt should be (-8 plusOrMinus 2.toByte)
      minusSevenInt should be (-7 plusOrMinus 2.toByte)
      minusSevenInt should be (-6 plusOrMinus 2.toByte)
      minusSevenInt should be (-5 plusOrMinus 2.toByte)

      // Short plusOrMinus Short
      sevenShort should be (9.toShort plusOrMinus 2.toShort)
      sevenShort should be (8.toShort plusOrMinus 2.toShort)
      sevenShort should be (7.toShort plusOrMinus 2.toShort)
      sevenShort should be (6.toShort plusOrMinus 2.toShort)
      sevenShort should be (5.toShort plusOrMinus 2.toShort)
      minusSevenShort should be ((-9).toShort plusOrMinus 2.toShort)
      minusSevenShort should be ((-8).toShort plusOrMinus 2.toShort)
      minusSevenShort should be ((-7).toShort plusOrMinus 2.toShort)
      minusSevenShort should be ((-6).toShort plusOrMinus 2.toShort)
      minusSevenShort should be ((-5).toShort plusOrMinus 2.toShort)

      // Short plusOrMinus Byte
      sevenShort should be (9.toShort plusOrMinus 2.toByte)
      sevenShort should be (8.toShort plusOrMinus 2.toByte)
      sevenShort should be (7.toShort plusOrMinus 2.toByte)
      sevenShort should be (6.toShort plusOrMinus 2.toByte)
      sevenShort should be (5.toShort plusOrMinus 2.toByte)
      minusSevenShort should be ((-9).toShort plusOrMinus 2.toByte)
      minusSevenShort should be ((-8).toShort plusOrMinus 2.toByte)
      minusSevenShort should be ((-7).toShort plusOrMinus 2.toByte)
      minusSevenShort should be ((-6).toShort plusOrMinus 2.toByte)
      minusSevenShort should be ((-5).toShort plusOrMinus 2.toByte)

      // Byte plusOrMinus Byte
      sevenByte should be (9.toByte plusOrMinus 2.toByte)
      sevenByte should be (8.toByte plusOrMinus 2.toByte)
      sevenByte should be (7.toByte plusOrMinus 2.toByte)
      sevenByte should be (6.toByte plusOrMinus 2.toByte)
      sevenByte should be (5.toByte plusOrMinus 2.toByte)
      minusSevenByte should be ((-9).toByte plusOrMinus 2.toByte)
      minusSevenByte should be ((-8).toByte plusOrMinus 2.toByte)
      minusSevenByte should be ((-7).toByte plusOrMinus 2.toByte)
      minusSevenByte should be ((-6).toByte plusOrMinus 2.toByte)
      minusSevenByte should be ((-5).toByte plusOrMinus 2.toByte)
    }

    it("should do nothing if the number is within the specified range, when used with not") {

      // Double plusOrMinus Double
      sevenDotOh should not { be (7.5 plusOrMinus 0.2) }
      sevenDotOh should not be (7.5 plusOrMinus 0.2)
      sevenDotOh should not be (6.5 plusOrMinus 0.2)
      minusSevenDotOh should not { be (-7.5 plusOrMinus 0.2) }
      minusSevenDotOh should not be (-7.5 plusOrMinus 0.2)
      minusSevenDotOh should not be (-6.5 plusOrMinus 0.2)

      // Double plusOrMinus Float
      sevenDotOh should not { be (7.5 plusOrMinus 0.2f) }
      sevenDotOh should not be (7.5 plusOrMinus 0.2f)
      sevenDotOh should not be (6.5 plusOrMinus 0.2f)
      minusSevenDotOh should not { be (-7.5 plusOrMinus 0.2f) }
      minusSevenDotOh should not be (-7.5 plusOrMinus 0.2f)
      minusSevenDotOh should not be (-6.5 plusOrMinus 0.2f)

      // Double plusOrMinus Long
      sevenDotOh should not { be (10.0 plusOrMinus 2L) }
      sevenDotOh should not be (4.0 plusOrMinus 2L)
      sevenDotOh should not be (9.1 plusOrMinus 2L)
      minusSevenDotOh should not { be (-10.0 plusOrMinus 2L) }
      minusSevenDotOh should not be (-4.0 plusOrMinus 2L)
      minusSevenDotOh should not be (-9.1 plusOrMinus 2L)

      // Double plusOrMinus Int
      sevenDotOh should not { be (10.0 plusOrMinus 2) }
      sevenDotOh should not be (4.0 plusOrMinus 2)
      sevenDotOh should not be (9.1 plusOrMinus 2)
      minusSevenDotOh should not { be (-10.0 plusOrMinus 2) }
      minusSevenDotOh should not be (-4.0 plusOrMinus 2)
      minusSevenDotOh should not be (-9.1 plusOrMinus 2)

      // Double plusOrMinus Short
      sevenDotOh should not { be (10.0 plusOrMinus 2.toShort) }
      sevenDotOh should not be (4.0 plusOrMinus 2.toShort)
      sevenDotOh should not be (9.1 plusOrMinus 2.toShort)
      minusSevenDotOh should not { be (-10.0 plusOrMinus 2.toShort) }
      minusSevenDotOh should not be (-4.0 plusOrMinus 2.toShort)
      minusSevenDotOh should not be (-9.1 plusOrMinus 2.toShort)

      // Double plusOrMinus Byte
      sevenDotOh should not { be (10.0 plusOrMinus 2.toByte) }
      sevenDotOh should not be (4.0 plusOrMinus 2.toByte)
      sevenDotOh should not be (9.1 plusOrMinus 2.toByte)
      minusSevenDotOh should not { be (-10.0 plusOrMinus 2.toByte) }
      minusSevenDotOh should not be (-4.0 plusOrMinus 2.toByte)
      minusSevenDotOh should not be (-9.1 plusOrMinus 2.toByte)

      // Float plusOrMinus Float
      sevenDotOhFloat should not { be (7.5f plusOrMinus 0.2f) }
      sevenDotOhFloat should not be (7.5f plusOrMinus 0.2f)
      sevenDotOhFloat should not be (6.5f plusOrMinus 0.2f)
      minusSevenDotOhFloat should not { be (-7.5f plusOrMinus 0.2f) }
      minusSevenDotOhFloat should not be (-7.5f plusOrMinus 0.2f)
      minusSevenDotOhFloat should not be (-6.5f plusOrMinus 0.2f)

      // Float plusOrMinus Long
      sevenDotOhFloat should not { be (10.0f plusOrMinus 2L) }
      sevenDotOhFloat should not be (4.0f plusOrMinus 2L)
      sevenDotOhFloat should not be (9.1f plusOrMinus 2L)
      minusSevenDotOhFloat should not { be (-10.0f plusOrMinus 2L) }
      minusSevenDotOhFloat should not be (-4.0f plusOrMinus 2L)
      minusSevenDotOhFloat should not be (-9.1f plusOrMinus 2L)

      // Float plusOrMinus Int
      sevenDotOhFloat should not { be (10.0f plusOrMinus 2) }
      sevenDotOhFloat should not be (4.0f plusOrMinus 2)
      sevenDotOhFloat should not be (9.1f plusOrMinus 2)
      minusSevenDotOhFloat should not { be (-10.0f plusOrMinus 2) }
      minusSevenDotOhFloat should not be (-4.0f plusOrMinus 2)
      minusSevenDotOhFloat should not be (-9.1f plusOrMinus 2)

      // Float plusOrMinus Short
      sevenDotOhFloat should not { be (10.0f plusOrMinus 2.toShort) }
      sevenDotOhFloat should not be (4.0f plusOrMinus 2.toShort)
      sevenDotOhFloat should not be (9.1f plusOrMinus 2.toShort)
      minusSevenDotOhFloat should not { be (-10.0f plusOrMinus 2.toShort) }
      minusSevenDotOhFloat should not be (-4.0f plusOrMinus 2.toShort)
      minusSevenDotOhFloat should not be (-9.1f plusOrMinus 2.toShort)

      // Float plusOrMinus Byte
      sevenDotOhFloat should not { be (10.0f plusOrMinus 2.toByte) }
      sevenDotOhFloat should not be (4.0f plusOrMinus 2.toByte)
      sevenDotOhFloat should not be (9.1f plusOrMinus 2.toByte)
      minusSevenDotOhFloat should not { be (-10.0f plusOrMinus 2.toByte) }
      minusSevenDotOhFloat should not be (-4.0f plusOrMinus 2.toByte)
      minusSevenDotOhFloat should not be (-9.1f plusOrMinus 2.toByte)

      // Long plusOrMinus Long
      sevenLong should not { be (10L plusOrMinus 2L) }
      sevenLong should not be (4L plusOrMinus 2L)
      sevenLong should not be (10L plusOrMinus 2L)
      minusSevenLong should not { be (-10L plusOrMinus 2L) }
      minusSevenLong should not be (-4L plusOrMinus 2L)
      minusSevenLong should not be (-10L plusOrMinus 2L)

      // Long plusOrMinus Int
      sevenLong should not { be (10L plusOrMinus 2) }
      sevenLong should not be (4L plusOrMinus 2)
      sevenLong should not be (10L plusOrMinus 2)
      minusSevenLong should not { be (-10L plusOrMinus 2) }
      minusSevenLong should not be (-4L plusOrMinus 2)
      minusSevenLong should not be (-10L plusOrMinus 2)

      // Long plusOrMinus Short
      sevenLong should not { be (10L plusOrMinus 2.toShort) }
      sevenLong should not be (4L plusOrMinus 2.toShort)
      sevenLong should not be (10L plusOrMinus 2.toShort)
      minusSevenLong should not { be (-10L plusOrMinus 2.toShort) }
      minusSevenLong should not be (-4L plusOrMinus 2.toShort)
      minusSevenLong should not be (-10L plusOrMinus 2.toShort)

      // Long plusOrMinus Byte
      sevenLong should not { be (10L plusOrMinus 2.toByte) }
      sevenLong should not be (4L plusOrMinus 2.toByte)
      sevenLong should not be (10L plusOrMinus 2.toByte)
      minusSevenLong should not { be (-10L plusOrMinus 2.toByte) }
      minusSevenLong should not be (-4L plusOrMinus 2.toByte)
      minusSevenLong should not be (-10L plusOrMinus 2.toByte)

      // Int plusOrMinus Int
      sevenInt should not { be (10 plusOrMinus 2) }
      sevenInt should not be (4 plusOrMinus 2)
      sevenInt should not be (10 plusOrMinus 2)
      minusSevenInt should not { be (-10 plusOrMinus 2) }
      minusSevenInt should not be (-4 plusOrMinus 2)
      minusSevenInt should not be (-10 plusOrMinus 2)

      // Int plusOrMinus Short
      sevenInt should not { be (10 plusOrMinus 2.toShort) }
      sevenInt should not be (4 plusOrMinus 2.toShort)
      sevenInt should not be (10 plusOrMinus 2.toShort)
      minusSevenInt should not { be (-10 plusOrMinus 2.toShort) }
      minusSevenInt should not be (-4 plusOrMinus 2.toShort)
      minusSevenInt should not be (-10 plusOrMinus 2.toShort)

      // Int plusOrMinus Byte
      sevenInt should not { be (10 plusOrMinus 2.toByte) }
      sevenInt should not be (4 plusOrMinus 2.toByte)
      sevenInt should not be (10 plusOrMinus 2.toByte)
      minusSevenInt should not { be (-10 plusOrMinus 2.toByte) }
      minusSevenInt should not be (-4 plusOrMinus 2.toByte)
      minusSevenInt should not be (-10 plusOrMinus 2.toByte)

      // Short plusOrMinus Short
      sevenShort should not { be (10.toShort plusOrMinus 2.toShort) }
      sevenShort should not be (4.toShort plusOrMinus 2.toShort)
      sevenShort should not be (10.toShort plusOrMinus 2.toShort)
      minusSevenShort should not { be ((-10).toShort plusOrMinus 2.toShort) }
      minusSevenShort should not be ((-4).toShort plusOrMinus 2.toShort)
      minusSevenShort should not be ((-10).toShort plusOrMinus 2.toShort)

      // Short plusOrMinus Byte
      sevenShort should not { be (10.toShort plusOrMinus 2.toByte) }
      sevenShort should not be (4.toShort plusOrMinus 2.toByte)
      sevenShort should not be (10.toShort plusOrMinus 2.toByte)
      minusSevenShort should not { be ((-10).toShort plusOrMinus 2.toByte) }
      minusSevenShort should not be ((-4).toShort plusOrMinus 2.toByte)
      minusSevenShort should not be ((-10).toShort plusOrMinus 2.toByte)

      // Byte plusOrMinus Byte
      sevenByte should not { be (10.toByte plusOrMinus 2.toByte) }
      sevenByte should not be (4.toByte plusOrMinus 2.toByte)
      sevenByte should not be (10.toByte plusOrMinus 2.toByte)
      minusSevenByte should not { be ((-10).toByte plusOrMinus 2.toByte) }
      minusSevenByte should not be ((-4).toByte plusOrMinus 2.toByte)
      minusSevenByte should not be ((-10).toByte plusOrMinus 2.toByte)
    }

    it("should do nothing if the number is within the specified range, when used in a logical-and expression") {

      // Double plusOrMinus Double
      sevenDotOh should ((be (7.1 plusOrMinus 0.2)) and (be (7.1 plusOrMinus 0.2)))
      sevenDotOh should (be (6.9 plusOrMinus 0.2) and (be (7.1 plusOrMinus 0.2)))
      sevenDotOh should (be (7.0 plusOrMinus 0.2) and be (7.0 plusOrMinus 0.2))

      // Double plusOrMinus Float
      sevenDotOh should ((be (7.1 plusOrMinus 0.2f)) and (be (7.1 plusOrMinus 0.2f)))
      sevenDotOh should (be (6.9 plusOrMinus 0.2f) and (be (7.1 plusOrMinus 0.2f)))
      sevenDotOh should (be (7.0 plusOrMinus 0.2f) and be (7.0 plusOrMinus 0.2f))

      // Double plusOrMinus Long
      sevenDotOh should ((be (7.1 plusOrMinus 2L)) and (be (7.1 plusOrMinus 2L)))
      sevenDotOh should (be (6.9 plusOrMinus 2L) and (be (7.1 plusOrMinus 2L)))
      sevenDotOh should (be (7.0 plusOrMinus 2L) and be (7.0 plusOrMinus 2L))

      // Double plusOrMinus Int
      sevenDotOh should ((be (7.1 plusOrMinus 2)) and (be (7.1 plusOrMinus 2)))
      sevenDotOh should (be (6.9 plusOrMinus 2) and (be (7.1 plusOrMinus 2)))
      sevenDotOh should (be (7.0 plusOrMinus 2) and be (7.0 plusOrMinus 2))

      // Double plusOrMinus Short
      sevenDotOh should ((be (7.1 plusOrMinus 2.toShort)) and (be (7.1 plusOrMinus 2.toShort)))
      sevenDotOh should (be (6.9 plusOrMinus 2.toShort) and (be (7.1 plusOrMinus 2.toShort)))
      sevenDotOh should (be (7.0 plusOrMinus 2.toShort) and be (7.0 plusOrMinus 2.toShort))

      // Double plusOrMinus Byte
      sevenDotOh should ((be (7.1 plusOrMinus 2.toByte)) and (be (7.1 plusOrMinus 2.toByte)))
      sevenDotOh should (be (6.9 plusOrMinus 2.toByte) and (be (7.1 plusOrMinus 2.toByte)))
      sevenDotOh should (be (7.0 plusOrMinus 2.toByte) and be (7.0 plusOrMinus 2.toByte))

      // Float plusOrMinus Float
      sevenDotOhFloat should ((be (7.1f plusOrMinus 0.2f)) and (be (7.1f plusOrMinus 0.2f)))
      sevenDotOhFloat should (be (6.9f plusOrMinus 0.2f) and (be (7.1f plusOrMinus 0.2f)))
      sevenDotOhFloat should (be (7.0f plusOrMinus 0.2f) and be (7.0f plusOrMinus 0.2f))

      // Float plusOrMinus Long
      sevenDotOhFloat should ((be (7.1f plusOrMinus 2L)) and (be (7.1f plusOrMinus 2L)))
      sevenDotOhFloat should (be (6.9f plusOrMinus 2L) and (be (7.1f plusOrMinus 2L)))
      sevenDotOhFloat should (be (7.0f plusOrMinus 2L) and be (7.0f plusOrMinus 2L))

      // Float plusOrMinus Int
      sevenDotOhFloat should ((be (7.1f plusOrMinus 2)) and (be (7.1f plusOrMinus 2)))
      sevenDotOhFloat should (be (6.9f plusOrMinus 2) and (be (7.1f plusOrMinus 2)))
      sevenDotOhFloat should (be (7.0f plusOrMinus 2) and be (7.0f plusOrMinus 2))

      // Float plusOrMinus Short
      sevenDotOhFloat should ((be (7.1f plusOrMinus 2.toShort)) and (be (7.1f plusOrMinus 2.toShort)))
      sevenDotOhFloat should (be (6.9f plusOrMinus 2.toShort) and (be (7.1f plusOrMinus 2.toShort)))
      sevenDotOhFloat should (be (7.0f plusOrMinus 2.toShort) and be (7.0f plusOrMinus 2.toShort))

      // Float plusOrMinus Byte
      sevenDotOhFloat should ((be (7.1f plusOrMinus 2.toByte)) and (be (7.1f plusOrMinus 2.toByte)))
      sevenDotOhFloat should (be (6.9f plusOrMinus 2.toByte) and (be (7.1f plusOrMinus 2.toByte)))
      sevenDotOhFloat should (be (7.0f plusOrMinus 2.toByte) and be (7.0f plusOrMinus 2.toByte))

      // Long plusOrMinus Long
      sevenLong should ((be (9L plusOrMinus 2L)) and (be (9L plusOrMinus 2L)))
      sevenLong should (be (8L plusOrMinus 2L) and (be (9L plusOrMinus 2L)))
      sevenLong should (be (7L plusOrMinus 2L) and be (7L plusOrMinus 2L))

      // Long plusOrMinus Int
      sevenLong should ((be (9L plusOrMinus 2)) and (be (9L plusOrMinus 2)))
      sevenLong should (be (8L plusOrMinus 2) and (be (9L plusOrMinus 2)))
      sevenLong should (be (7L plusOrMinus 2) and be (7L plusOrMinus 2))

      // Long plusOrMinus Short
      sevenLong should ((be (9L plusOrMinus 2.toShort)) and (be (9L plusOrMinus 2.toShort)))
      sevenLong should (be (8L plusOrMinus 2.toShort) and (be (9L plusOrMinus 2.toShort)))
      sevenLong should (be (7L plusOrMinus 2.toShort) and be (7L plusOrMinus 2.toShort))

      // Long plusOrMinus Byte
      sevenLong should ((be (9L plusOrMinus 2.toByte)) and (be (9L plusOrMinus 2.toByte)))
      sevenLong should (be (8L plusOrMinus 2.toByte) and (be (9L plusOrMinus 2.toByte)))
      sevenLong should (be (7L plusOrMinus 2.toByte) and be (7L plusOrMinus 2.toByte))

      // Int plusOrMinus Int
      sevenInt should ((be (9 plusOrMinus 2)) and (be (9 plusOrMinus 2)))
      sevenInt should (be (8 plusOrMinus 2) and (be (9 plusOrMinus 2)))
      sevenInt should (be (7 plusOrMinus 2) and be (7 plusOrMinus 2))

      // Int plusOrMinus Short
      sevenInt should ((be (9 plusOrMinus 2.toShort)) and (be (9 plusOrMinus 2.toShort)))
      sevenInt should (be (8 plusOrMinus 2.toShort) and (be (9 plusOrMinus 2.toShort)))
      sevenInt should (be (7 plusOrMinus 2.toShort) and be (7 plusOrMinus 2.toShort))

      // Int plusOrMinus Byte
      sevenInt should ((be (9 plusOrMinus 2.toByte)) and (be (9 plusOrMinus 2.toByte)))
      sevenInt should (be (8 plusOrMinus 2.toByte) and (be (9 plusOrMinus 2.toByte)))
      sevenInt should (be (7 plusOrMinus 2.toByte) and be (7 plusOrMinus 2.toByte))

      // Short plusOrMinus Short
      sevenShort should ((be (9.toShort plusOrMinus 2.toShort)) and (be (9.toShort plusOrMinus 2.toShort)))
      sevenShort should (be (8.toShort plusOrMinus 2.toShort) and (be (9.toShort plusOrMinus 2.toShort)))
      sevenShort should (be (7.toShort plusOrMinus 2.toShort) and be (7.toShort plusOrMinus 2.toShort))

      // Short plusOrMinus Byte
      sevenShort should ((be (9.toShort plusOrMinus 2.toByte)) and (be (9.toShort plusOrMinus 2.toByte)))
      sevenShort should (be (8.toShort plusOrMinus 2.toByte) and (be (9.toShort plusOrMinus 2.toByte)))
      sevenShort should (be (7.toShort plusOrMinus 2.toByte) and be (7.toShort plusOrMinus 2.toByte))

      // Byte plusOrMinus Byte
      sevenByte should ((be (9.toByte plusOrMinus 2.toByte)) and (be (9.toByte plusOrMinus 2.toByte)))
      sevenByte should (be (8.toByte plusOrMinus 2.toByte) and (be (9.toByte plusOrMinus 2.toByte)))
      sevenByte should (be (7.toByte plusOrMinus 2.toByte) and be (7.toByte plusOrMinus 2.toByte))
    }

    it("should do nothing if the number is within the specified range, when used in a logical-or expression") {

      // Double plusOrMinus Double
      sevenDotOh should ((be (7.1 plusOrMinus 0.2)) or (be (7.1 plusOrMinus 0.2)))
      sevenDotOh should (be (6.9 plusOrMinus 0.2) or (be (7.1 plusOrMinus 0.2)))
      sevenDotOh should (be (7.0 plusOrMinus 0.2) or be (7.0 plusOrMinus 0.2))

      // Double plusOrMinus Float
      sevenDotOh should ((be (7.1 plusOrMinus 0.2f)) or (be (7.1 plusOrMinus 0.2f)))
      sevenDotOh should (be (6.9 plusOrMinus 0.2f) or (be (7.1 plusOrMinus 0.2f)))
      sevenDotOh should (be (7.0 plusOrMinus 0.2f) or be (7.0 plusOrMinus 0.2f))

      // Double plusOrMinus Long
      sevenDotOh should ((be (7.1 plusOrMinus 2L)) or (be (7.1 plusOrMinus 2L)))
      sevenDotOh should (be (6.9 plusOrMinus 2L) or (be (7.1 plusOrMinus 2L)))
      sevenDotOh should (be (7.0 plusOrMinus 2L) or be (7.0 plusOrMinus 2L))

      // Double plusOrMinus Int
      sevenDotOh should ((be (7.1 plusOrMinus 2)) or (be (7.1 plusOrMinus 2)))
      sevenDotOh should (be (6.9 plusOrMinus 2) or (be (7.1 plusOrMinus 2)))
      sevenDotOh should (be (7.0 plusOrMinus 2) or be (7.0 plusOrMinus 2))

      // Double plusOrMinus Short
      sevenDotOh should ((be (7.1 plusOrMinus 2.toShort)) or (be (7.1 plusOrMinus 2.toShort)))
      sevenDotOh should (be (6.9 plusOrMinus 2.toShort) or (be (7.1 plusOrMinus 2.toShort)))
      sevenDotOh should (be (7.0 plusOrMinus 2.toShort) or be (7.0 plusOrMinus 2.toShort))

      // Double plusOrMinus Byte
      sevenDotOh should ((be (7.1 plusOrMinus 2.toByte)) or (be (7.1 plusOrMinus 2.toByte)))
      sevenDotOh should (be (6.9 plusOrMinus 2.toByte) or (be (7.1 plusOrMinus 2.toByte)))
      sevenDotOh should (be (7.0 plusOrMinus 2.toByte) or be (7.0 plusOrMinus 2.toByte))

      // Float plusOrMinus Float
      sevenDotOhFloat should ((be (7.1f plusOrMinus 0.2f)) or (be (7.1f plusOrMinus 0.2f)))
      sevenDotOhFloat should (be (6.9f plusOrMinus 0.2f) or (be (7.1f plusOrMinus 0.2f)))
      sevenDotOhFloat should (be (7.0f plusOrMinus 0.2f) or be (7.0f plusOrMinus 0.2f))

      // Float plusOrMinus Long
      sevenDotOhFloat should ((be (7.1f plusOrMinus 2L)) or (be (7.1f plusOrMinus 2L)))
      sevenDotOhFloat should (be (6.9f plusOrMinus 2L) or (be (7.1f plusOrMinus 2L)))
      sevenDotOhFloat should (be (7.0f plusOrMinus 2L) or be (7.0f plusOrMinus 2L))

      // Float plusOrMinus Int
      sevenDotOhFloat should ((be (7.1f plusOrMinus 2)) or (be (7.1f plusOrMinus 2)))
      sevenDotOhFloat should (be (6.9f plusOrMinus 2) or (be (7.1f plusOrMinus 2)))
      sevenDotOhFloat should (be (7.0f plusOrMinus 2) or be (7.0f plusOrMinus 2))

      // Float plusOrMinus Short
      sevenDotOhFloat should ((be (7.1f plusOrMinus 2.toShort)) or (be (7.1f plusOrMinus 2.toShort)))
      sevenDotOhFloat should (be (6.9f plusOrMinus 2.toShort) or (be (7.1f plusOrMinus 2.toShort)))
      sevenDotOhFloat should (be (7.0f plusOrMinus 2.toShort) or be (7.0f plusOrMinus 2.toShort))

      // Float plusOrMinus Byte
      sevenDotOhFloat should ((be (7.1f plusOrMinus 2.toByte)) or (be (7.1f plusOrMinus 2.toByte)))
      sevenDotOhFloat should (be (6.9f plusOrMinus 2.toByte) or (be (7.1f plusOrMinus 2.toByte)))
      sevenDotOhFloat should (be (7.0f plusOrMinus 2.toByte) or be (7.0f plusOrMinus 2.toByte))

      // Long plusOrMinus Long
      sevenLong should ((be (9L plusOrMinus 2L)) or (be (9L plusOrMinus 2L)))
      sevenLong should (be (8L plusOrMinus 2L) or (be (9L plusOrMinus 2L)))
      sevenLong should (be (7L plusOrMinus 2L) or be (7L plusOrMinus 2L))

      // Long plusOrMinus Int
      sevenLong should ((be (9L plusOrMinus 2)) or (be (9L plusOrMinus 2)))
      sevenLong should (be (8L plusOrMinus 2) or (be (9L plusOrMinus 2)))
      sevenLong should (be (7L plusOrMinus 2) or be (7L plusOrMinus 2))

      // Long plusOrMinus Short
      sevenLong should ((be (9L plusOrMinus 2.toShort)) or (be (9L plusOrMinus 2.toShort)))
      sevenLong should (be (8L plusOrMinus 2.toShort) or (be (9L plusOrMinus 2.toShort)))
      sevenLong should (be (7L plusOrMinus 2.toShort) or be (7L plusOrMinus 2.toShort))

      // Long plusOrMinus Byte
      sevenLong should ((be (9L plusOrMinus 2.toByte)) or (be (9L plusOrMinus 2.toByte)))
      sevenLong should (be (8L plusOrMinus 2.toByte) or (be (9L plusOrMinus 2.toByte)))
      sevenLong should (be (7L plusOrMinus 2.toByte) or be (7L plusOrMinus 2.toByte))

      // Int plusOrMinus Int
      sevenInt should ((be (9 plusOrMinus 2)) or (be (9 plusOrMinus 2)))
      sevenInt should (be (8 plusOrMinus 2) or (be (9 plusOrMinus 2)))
      sevenInt should (be (7 plusOrMinus 2) or be (7 plusOrMinus 2))

      // Int plusOrMinus Short
      sevenInt should ((be (9 plusOrMinus 2.toShort)) or (be (9 plusOrMinus 2.toShort)))
      sevenInt should (be (8 plusOrMinus 2.toShort) or (be (9 plusOrMinus 2.toShort)))
      sevenInt should (be (7 plusOrMinus 2.toShort) or be (7 plusOrMinus 2.toShort))

      // Int plusOrMinus Byte
      sevenInt should ((be (9 plusOrMinus 2.toByte)) or (be (9 plusOrMinus 2.toByte)))
      sevenInt should (be (8 plusOrMinus 2.toByte) or (be (9 plusOrMinus 2.toByte)))
      sevenInt should (be (7 plusOrMinus 2.toByte) or be (7 plusOrMinus 2.toByte))

      // Short plusOrMinus Short
      sevenShort should ((be (9.toShort plusOrMinus 2.toShort)) or (be (9.toShort plusOrMinus 2.toShort)))
      sevenShort should (be (8.toShort plusOrMinus 2.toShort) or (be (9.toShort plusOrMinus 2.toShort)))
      sevenShort should (be (7.toShort plusOrMinus 2.toShort) or be (7.toShort plusOrMinus 2.toShort))

      // Short plusOrMinus Byte
      sevenShort should ((be (9.toShort plusOrMinus 2.toByte)) or (be (9.toShort plusOrMinus 2.toByte)))
      sevenShort should (be (8.toShort plusOrMinus 2.toByte) or (be (9.toShort plusOrMinus 2.toByte)))
      sevenShort should (be (7.toShort plusOrMinus 2.toByte) or be (7.toShort plusOrMinus 2.toByte))

      // Byte plusOrMinus Byte
      sevenByte should ((be (9.toByte plusOrMinus 2.toByte)) or (be (9.toByte plusOrMinus 2.toByte)))
      sevenByte should (be (8.toByte plusOrMinus 2.toByte) or (be (9.toByte plusOrMinus 2.toByte)))
      sevenByte should (be (7.toByte plusOrMinus 2.toByte) or be (7.toByte plusOrMinus 2.toByte))
    }

    it("should do nothing if the number is not within the specified range, when used in a logical-and expression with not") {

      // Double plusOrMinus Double
      sevenDotOh should ((not be (17.1 plusOrMinus 0.2)) and (not be (17.1 plusOrMinus 0.2)))
      sevenDotOh should (not (be (16.9 plusOrMinus 0.2)) and not (be (17.1 plusOrMinus 0.2)))
      sevenDotOh should (not be (17.0 plusOrMinus 0.2) and not be (17.0 plusOrMinus 0.2))

      // Double plusOrMinus Float
      sevenDotOh should ((not be (17.1 plusOrMinus 0.2f)) and (not be (17.1 plusOrMinus 0.2f)))
      sevenDotOh should (not (be (16.9 plusOrMinus 0.2f)) and not (be (17.1 plusOrMinus 0.2f)))
      sevenDotOh should (not be (17.0 plusOrMinus 0.2f) and not be (17.0 plusOrMinus 0.2f))

      // Double plusOrMinus Long
      sevenDotOh should ((not be (17.1 plusOrMinus 2L)) and (not be (17.1 plusOrMinus 2L)))
      sevenDotOh should (not (be (16.9 plusOrMinus 2L)) and not (be (17.1 plusOrMinus 2L)))
      sevenDotOh should (not be (17.0 plusOrMinus 2L) and not be (17.0 plusOrMinus 2L))

      // Double plusOrMinus Int
      sevenDotOh should ((not be (17.1 plusOrMinus 2)) and (not be (17.1 plusOrMinus 2)))
      sevenDotOh should (not (be (16.9 plusOrMinus 2)) and not (be (17.1 plusOrMinus 2)))
      sevenDotOh should (not be (17.0 plusOrMinus 2) and not be (17.0 plusOrMinus 2))

      // Double plusOrMinus Short
      sevenDotOh should ((not be (17.1 plusOrMinus 2.toShort)) and (not be (17.1 plusOrMinus 2.toShort)))
      sevenDotOh should (not (be (16.9 plusOrMinus 2.toShort)) and not (be (17.1 plusOrMinus 2.toShort)))
      sevenDotOh should (not be (17.0 plusOrMinus 2.toShort) and not be (17.0 plusOrMinus 2.toShort))

      // Double plusOrMinus Byte
      sevenDotOh should ((not be (17.1 plusOrMinus 2.toByte)) and (not be (17.1 plusOrMinus 2.toByte)))
      sevenDotOh should (not (be (16.9 plusOrMinus 2.toByte)) and not (be (17.1 plusOrMinus 2.toByte)))
      sevenDotOh should (not be (17.0 plusOrMinus 2.toByte) and not be (17.0 plusOrMinus 2.toByte))

      // Float plusOrMinus Float
      sevenDotOhFloat should ((not be (17.1f plusOrMinus 0.2f)) and (not be (17.1f plusOrMinus 0.2f)))
      sevenDotOhFloat should (not (be (16.9f plusOrMinus 0.2f)) and not (be (17.1f plusOrMinus 0.2f)))
      sevenDotOhFloat should (not be (17.0f plusOrMinus 0.2f) and not be (17.0f plusOrMinus 0.2f))

      // Float plusOrMinus Long
      sevenDotOhFloat should ((not be (17.1f plusOrMinus 2L)) and (not be (17.1f plusOrMinus 2L)))
      sevenDotOhFloat should (not (be (16.9f plusOrMinus 2L)) and not (be (17.1f plusOrMinus 2L)))
      sevenDotOhFloat should (not be (17.0f plusOrMinus 2L) and not be (17.0f plusOrMinus 2L))

      // Float plusOrMinus Int
      sevenDotOhFloat should ((not be (17.1f plusOrMinus 2)) and (not be (17.1f plusOrMinus 2)))
      sevenDotOhFloat should (not (be (16.9f plusOrMinus 2)) and not (be (17.1f plusOrMinus 2)))
      sevenDotOhFloat should (not be (17.0f plusOrMinus 2) and not be (17.0f plusOrMinus 2))

      // Float plusOrMinus Short
      sevenDotOhFloat should ((not be (17.1f plusOrMinus 2.toShort)) and (not be (17.1f plusOrMinus 2.toShort)))
      sevenDotOhFloat should (not (be (16.9f plusOrMinus 2.toShort)) and not (be (17.1f plusOrMinus 2.toShort)))
      sevenDotOhFloat should (not be (17.0f plusOrMinus 2.toShort) and not be (17.0f plusOrMinus 2.toShort))

      // Float plusOrMinus Byte
      sevenDotOhFloat should ((not be (17.1f plusOrMinus 2.toByte)) and (not be (17.1f plusOrMinus 2.toByte)))
      sevenDotOhFloat should (not (be (16.9f plusOrMinus 2.toByte)) and not (be (17.1f plusOrMinus 2.toByte)))
      sevenDotOhFloat should (not be (17.0f plusOrMinus 2.toByte) and not be (17.0f plusOrMinus 2.toByte))

      // Long plusOrMinus Long
      sevenLong should ((not be (19L plusOrMinus 2L)) and (not be (19L plusOrMinus 2L)))
      sevenLong should (not (be (18L plusOrMinus 2L)) and not (be (19L plusOrMinus 2L)))
      sevenLong should (not be (17L plusOrMinus 2L) and not be (17L plusOrMinus 2L))

      // Long plusOrMinus Int
      sevenLong should ((not be (19L plusOrMinus 2)) and (not be (19L plusOrMinus 2)))
      sevenLong should (not (be (18L plusOrMinus 2)) and not (be (19L plusOrMinus 2)))
      sevenLong should (not be (17L plusOrMinus 2) and not be (17L plusOrMinus 2))

      // Long plusOrMinus Short
      sevenLong should ((not be (19L plusOrMinus 2.toShort)) and (not be (19L plusOrMinus 2.toShort)))
      sevenLong should (not (be (18L plusOrMinus 2.toShort)) and not (be (19L plusOrMinus 2.toShort)))
      sevenLong should (not be (17L plusOrMinus 2.toShort) and not be (17L plusOrMinus 2.toShort))

      // Long plusOrMinus Byte
      sevenLong should ((not be (19L plusOrMinus 2.toByte)) and (not be (19L plusOrMinus 2.toByte)))
      sevenLong should (not (be (18L plusOrMinus 2.toByte)) and not (be (19L plusOrMinus 2.toByte)))
      sevenLong should (not be (17L plusOrMinus 2.toByte) and not be (17L plusOrMinus 2.toByte))

      // Int plusOrMinus Int
      sevenInt should ((not be (19 plusOrMinus 2)) and (not be (19 plusOrMinus 2)))
      sevenInt should (not (be (18 plusOrMinus 2)) and not (be (19 plusOrMinus 2)))
      sevenInt should (not be (17 plusOrMinus 2) and not be (17 plusOrMinus 2))

      // Int plusOrMinus Short
      sevenInt should ((not be (19 plusOrMinus 2.toShort)) and (not be (19 plusOrMinus 2.toShort)))
      sevenInt should (not (be (18 plusOrMinus 2.toShort)) and not (be (19 plusOrMinus 2.toShort)))
      sevenInt should (not be (17 plusOrMinus 2.toShort) and not be (17 plusOrMinus 2.toShort))

      // Int plusOrMinus Byte
      sevenInt should ((not be (19 plusOrMinus 2.toByte)) and (not be (19 plusOrMinus 2.toByte)))
      sevenInt should (not (be (18 plusOrMinus 2.toByte)) and not (be (19 plusOrMinus 2.toByte)))
      sevenInt should (not be (17 plusOrMinus 2.toByte) and not be (17 plusOrMinus 2.toByte))

      // Short plusOrMinus Short
      sevenShort should ((not be (19.toShort plusOrMinus 2.toShort)) and (not be (19.toShort plusOrMinus 2.toShort)))
      sevenShort should (not (be (18.toShort plusOrMinus 2.toShort)) and not (be (19.toShort plusOrMinus 2.toShort)))
      sevenShort should (not be (17.toShort plusOrMinus 2.toShort) and not be (17.toShort plusOrMinus 2.toShort))

      // Short plusOrMinus Byte
      sevenShort should ((not be (19.toShort plusOrMinus 2.toByte)) and (not be (19.toShort plusOrMinus 2.toByte)))
      sevenShort should (not (be (18.toShort plusOrMinus 2.toByte)) and not (be (19.toShort plusOrMinus 2.toByte)))
      sevenShort should (not be (17.toShort plusOrMinus 2.toByte) and not be (17.toShort plusOrMinus 2.toByte))

      // Byte plusOrMinus Byte
      sevenByte should ((not be (19.toByte plusOrMinus 2.toByte)) and (not be (19.toByte plusOrMinus 2.toByte)))
      sevenByte should (not (be (18.toByte plusOrMinus 2.toByte)) and not (be (19.toByte plusOrMinus 2.toByte)))
      sevenByte should (not be (17.toByte plusOrMinus 2.toByte) and not be (17.toByte plusOrMinus 2.toByte))
    }

    it("should do nothing if the number is not within the specified range, when used in a logical-or expression with not") {

      // Double plusOrMinus Double
      sevenDotOh should ((not be (17.1 plusOrMinus 0.2)) or (not be (17.1 plusOrMinus 0.2)))
      sevenDotOh should (not (be (16.9 plusOrMinus 0.2)) or not (be (17.1 plusOrMinus 0.2)))
      sevenDotOh should (not be (17.0 plusOrMinus 0.2) or not be (17.0 plusOrMinus 0.2))

      // Double plusOrMinus Float
      sevenDotOh should ((not be (17.1 plusOrMinus 0.2f)) or (not be (17.1 plusOrMinus 0.2f)))
      sevenDotOh should (not (be (16.9 plusOrMinus 0.2f)) or not (be (17.1 plusOrMinus 0.2f)))
      sevenDotOh should (not be (17.0 plusOrMinus 0.2f) or not be (17.0 plusOrMinus 0.2f))

      // Double plusOrMinus Long
      sevenDotOh should ((not be (17.1 plusOrMinus 2L)) or (not be (17.1 plusOrMinus 2L)))
      sevenDotOh should (not (be (16.9 plusOrMinus 2L)) or not (be (17.1 plusOrMinus 2L)))
      sevenDotOh should (not be (17.0 plusOrMinus 2L) or not be (17.0 plusOrMinus 2L))

      // Double plusOrMinus Int
      sevenDotOh should ((not be (17.1 plusOrMinus 2)) or (not be (17.1 plusOrMinus 2)))
      sevenDotOh should (not (be (16.9 plusOrMinus 2)) or not (be (17.1 plusOrMinus 2)))
      sevenDotOh should (not be (17.0 plusOrMinus 2) or not be (17.0 plusOrMinus 2))

      // Double plusOrMinus Short
      sevenDotOh should ((not be (17.1 plusOrMinus 2.toShort)) or (not be (17.1 plusOrMinus 2.toShort)))
      sevenDotOh should (not (be (16.9 plusOrMinus 2.toShort)) or not (be (17.1 plusOrMinus 2.toShort)))
      sevenDotOh should (not be (17.0 plusOrMinus 2.toShort) or not be (17.0 plusOrMinus 2.toShort))

      // Double plusOrMinus Byte
      sevenDotOh should ((not be (17.1 plusOrMinus 2.toByte)) or (not be (17.1 plusOrMinus 2.toByte)))
      sevenDotOh should (not (be (16.9 plusOrMinus 2.toByte)) or not (be (17.1 plusOrMinus 2.toByte)))
      sevenDotOh should (not be (17.0 plusOrMinus 2.toByte) or not be (17.0 plusOrMinus 2.toByte))

      // Float plusOrMinus Float
      sevenDotOhFloat should ((not be (17.1f plusOrMinus 0.2f)) or (not be (17.1f plusOrMinus 0.2f)))
      sevenDotOhFloat should (not (be (16.9f plusOrMinus 0.2f)) or not (be (17.1f plusOrMinus 0.2f)))
      sevenDotOhFloat should (not be (17.0f plusOrMinus 0.2f) or not be (17.0f plusOrMinus 0.2f))

      // Float plusOrMinus Long
      sevenDotOhFloat should ((not be (17.1f plusOrMinus 2L)) or (not be (17.1f plusOrMinus 2L)))
      sevenDotOhFloat should (not (be (16.9f plusOrMinus 2L)) or not (be (17.1f plusOrMinus 2L)))
      sevenDotOhFloat should (not be (17.0f plusOrMinus 2L) or not be (17.0f plusOrMinus 2L))

      // Float plusOrMinus Int
      sevenDotOhFloat should ((not be (17.1f plusOrMinus 2)) or (not be (17.1f plusOrMinus 2)))
      sevenDotOhFloat should (not (be (16.9f plusOrMinus 2)) or not (be (17.1f plusOrMinus 2)))
      sevenDotOhFloat should (not be (17.0f plusOrMinus 2) or not be (17.0f plusOrMinus 2))

      // Float plusOrMinus Short
      sevenDotOhFloat should ((not be (17.1f plusOrMinus 2.toShort)) or (not be (17.1f plusOrMinus 2.toShort)))
      sevenDotOhFloat should (not (be (16.9f plusOrMinus 2.toShort)) or not (be (17.1f plusOrMinus 2.toShort)))
      sevenDotOhFloat should (not be (17.0f plusOrMinus 2.toShort) or not be (17.0f plusOrMinus 2.toShort))

      // Float plusOrMinus Byte
      sevenDotOhFloat should ((not be (17.1f plusOrMinus 2.toByte)) or (not be (17.1f plusOrMinus 2.toByte)))
      sevenDotOhFloat should (not (be (16.9f plusOrMinus 2.toByte)) or not (be (17.1f plusOrMinus 2.toByte)))
      sevenDotOhFloat should (not be (17.0f plusOrMinus 2.toByte) or not be (17.0f plusOrMinus 2.toByte))

      // Long plusOrMinus Long
      sevenLong should ((not be (19L plusOrMinus 2L)) or (not be (19L plusOrMinus 2L)))
      sevenLong should (not (be (18L plusOrMinus 2L)) or not (be (19L plusOrMinus 2L)))
      sevenLong should (not be (17L plusOrMinus 2L) or not be (17L plusOrMinus 2L))

      // Long plusOrMinus Int
      sevenLong should ((not be (19L plusOrMinus 2)) or (not be (19L plusOrMinus 2)))
      sevenLong should (not (be (18L plusOrMinus 2)) or not (be (19L plusOrMinus 2)))
      sevenLong should (not be (17L plusOrMinus 2) or not be (17L plusOrMinus 2))

      // Long plusOrMinus Short
      sevenLong should ((not be (19L plusOrMinus 2.toShort)) or (not be (19L plusOrMinus 2.toShort)))
      sevenLong should (not (be (18L plusOrMinus 2.toShort)) or not (be (19L plusOrMinus 2.toShort)))
      sevenLong should (not be (17L plusOrMinus 2.toShort) or not be (17L plusOrMinus 2.toShort))

      // Long plusOrMinus Byte
      sevenLong should ((not be (19L plusOrMinus 2.toByte)) or (not be (19L plusOrMinus 2.toByte)))
      sevenLong should (not (be (18L plusOrMinus 2.toByte)) or not (be (19L plusOrMinus 2.toByte)))
      sevenLong should (not be (17L plusOrMinus 2.toByte) or not be (17L plusOrMinus 2.toByte))

      // Int plusOrMinus Int
      sevenInt should ((not be (19 plusOrMinus 2)) or (not be (19 plusOrMinus 2)))
      sevenInt should (not (be (18 plusOrMinus 2)) or not (be (19 plusOrMinus 2)))
      sevenInt should (not be (17 plusOrMinus 2) or not be (17 plusOrMinus 2))

      // Int plusOrMinus Short
      sevenInt should ((not be (19 plusOrMinus 2.toShort)) or (not be (19 plusOrMinus 2.toShort)))
      sevenInt should (not (be (18 plusOrMinus 2.toShort)) or not (be (19 plusOrMinus 2.toShort)))
      sevenInt should (not be (17 plusOrMinus 2.toShort) or not be (17 plusOrMinus 2.toShort))

      // Int plusOrMinus Byte
      sevenInt should ((not be (19 plusOrMinus 2.toByte)) or (not be (19 plusOrMinus 2.toByte)))
      sevenInt should (not (be (18 plusOrMinus 2.toByte)) or not (be (19 plusOrMinus 2.toByte)))
      sevenInt should (not be (17 plusOrMinus 2.toByte) or not be (17 plusOrMinus 2.toByte))

      // Short plusOrMinus Short
      sevenShort should ((not be (19.toShort plusOrMinus 2.toShort)) or (not be (19.toShort plusOrMinus 2.toShort)))
      sevenShort should (not (be (18.toShort plusOrMinus 2.toShort)) or not (be (19.toShort plusOrMinus 2.toShort)))
      sevenShort should (not be (17.toShort plusOrMinus 2.toShort) or not be (17.toShort plusOrMinus 2.toShort))

      // Short plusOrMinus Byte
      sevenShort should ((not be (19.toShort plusOrMinus 2.toByte)) or (not be (19.toShort plusOrMinus 2.toByte)))
      sevenShort should (not (be (18.toShort plusOrMinus 2.toByte)) or not (be (19.toShort plusOrMinus 2.toByte)))
      sevenShort should (not be (17.toShort plusOrMinus 2.toByte) or not be (17.toShort plusOrMinus 2.toByte))

      // Byte plusOrMinus Byte
      sevenByte should ((not be (19.toByte plusOrMinus 2.toByte)) or (not be (19.toByte plusOrMinus 2.toByte)))
      sevenByte should (not (be (18.toByte plusOrMinus 2.toByte)) or not (be (19.toByte plusOrMinus 2.toByte)))
      sevenByte should (not be (17.toByte plusOrMinus 2.toByte) or not be (17.toByte plusOrMinus 2.toByte))
    }

    it("should throw AssertionError if the number is not within the specified range") {

      // Double plusOrMinus Double
      val caught1 = intercept[AssertionError] {
        sevenDotOh should be (17.1 plusOrMinus 0.2)
      }
      assert(caught1.getMessage === "7.0 was not 17.1 plus or minus 0.2")

      // Double plusOrMinus Float
      val caught2 = intercept[AssertionError] {
        sevenDotOh should be (17.1 plusOrMinus 0.2f)
      }
      assert(caught2.getMessage === "7.0 was not 17.1 plus or minus 0.20000000298023224")

      // Double plusOrMinus Long
      val caught3 = intercept[AssertionError] {
        sevenDotOh should be (17.1 plusOrMinus 2L)
      }
      assert(caught3.getMessage === "7.0 was not 17.1 plus or minus 2.0")

      // Double plusOrMinus Int
      val caught4 = intercept[AssertionError] {
        sevenDotOh should be (17.1 plusOrMinus 2)
      }
      assert(caught4.getMessage === "7.0 was not 17.1 plus or minus 2.0")

      // Double plusOrMinus Short
      val caught5 = intercept[AssertionError] {
        sevenDotOh should be (17.1 plusOrMinus 2.toShort)
      }
      assert(caught5.getMessage === "7.0 was not 17.1 plus or minus 2.0")

      // Double plusOrMinus Byte
      val caught6 = intercept[AssertionError] {
        sevenDotOh should be (17.1 plusOrMinus 2.toByte)
      }
      assert(caught6.getMessage === "7.0 was not 17.1 plus or minus 2.0")

      // Float plusOrMinus Float
      val caught7 = intercept[AssertionError] {
        sevenDotOhFloat should be (17.1f plusOrMinus 0.2f)
      }
      assert(caught7.getMessage === "7.0 was not 17.1 plus or minus 0.2")

      // Float plusOrMinus Long
      val caught8 = intercept[AssertionError] {
        sevenDotOhFloat should be (17.1f plusOrMinus 2L)
      }
      assert(caught8.getMessage === "7.0 was not 17.1 plus or minus 2.0")

      // Float plusOrMinus Int
      val caught9 = intercept[AssertionError] {
        sevenDotOhFloat should be (17.1f plusOrMinus 2)
      }
      assert(caught9.getMessage === "7.0 was not 17.1 plus or minus 2.0")

      // Float plusOrMinus Short
      val caught10 = intercept[AssertionError] {
        sevenDotOhFloat should be (17.1f plusOrMinus 2.toShort)
      }
      assert(caught10.getMessage === "7.0 was not 17.1 plus or minus 2.0")

      // Float plusOrMinus Byte
      val caught11 = intercept[AssertionError] {
        sevenDotOhFloat should be (17.1f plusOrMinus 2.toByte)
      }
      assert(caught11.getMessage === "7.0 was not 17.1 plus or minus 2.0")

      // Long plusOrMinus Long
      val caught12 = intercept[AssertionError] {
        sevenLong should be (19L plusOrMinus 2L)
      }
      assert(caught12.getMessage === "7 was not 19 plus or minus 2")

      // Long plusOrMinus Int
      val caught13 = intercept[AssertionError] {
        sevenLong should be (19L plusOrMinus 2)
      }
      assert(caught13.getMessage === "7 was not 19 plus or minus 2")

      // Long plusOrMinus Short
      val caught14 = intercept[AssertionError] {
        sevenLong should be (19L plusOrMinus 2.toShort)
      }
      assert(caught14.getMessage === "7 was not 19 plus or minus 2")

      // Long plusOrMinus Byte
      val caught15 = intercept[AssertionError] {
        sevenLong should be (19L plusOrMinus 2.toByte)
      }
      assert(caught15.getMessage === "7 was not 19 plus or minus 2")

      // Int plusOrMinus Int
      val caught16 = intercept[AssertionError] {
        sevenInt should be (19 plusOrMinus 2)
      }
      assert(caught16.getMessage === "7 was not 19 plus or minus 2")

      // Int plusOrMinus Short
      val caught17 = intercept[AssertionError] {
        sevenInt should be (19 plusOrMinus 2.toShort)
      }
      assert(caught17.getMessage === "7 was not 19 plus or minus 2")

      // Int plusOrMinus Byte
      val caught18 = intercept[AssertionError] {
        sevenInt should be (19 plusOrMinus 2.toByte)
      }
      assert(caught18.getMessage === "7 was not 19 plus or minus 2")

      // Short plusOrMinus Short
      val caught19 = intercept[AssertionError] {
        sevenShort should be (19.toShort plusOrMinus 2.toShort)
      }
      assert(caught19.getMessage === "7 was not 19 plus or minus 2")

      // Short plusOrMinus Byte
      val caught20 = intercept[AssertionError] {
        sevenShort should be (19.toShort plusOrMinus 2.toByte)
      }
      assert(caught20.getMessage === "7 was not 19 plus or minus 2")

      // Byte plusOrMinus Byte
      val caught21 = intercept[AssertionError] {
        sevenByte should be (19.toByte plusOrMinus 2.toByte)
      }
      assert(caught21.getMessage === "7 was not 19 plus or minus 2")
    }

    it("should throw AssertionError if the number is within the specified range, when used with not") {

      // Double plusOrMinus Double
      val caught1 = intercept[AssertionError] {
        sevenDotOh should not be (7.1 plusOrMinus 0.2)
      }
      assert(caught1.getMessage === "7.0 was 7.1 plus or minus 0.2")

      // Double plusOrMinus Float
      val caught2 = intercept[AssertionError] {
        sevenDotOh should not be (7.1 plusOrMinus 0.2f)
      }
      assert(caught2.getMessage === "7.0 was 7.1 plus or minus 0.20000000298023224")

      // Double plusOrMinus Long
      val caught3 = intercept[AssertionError] {
        sevenDotOh should not be (7.1 plusOrMinus 2L)
      }
      assert(caught3.getMessage === "7.0 was 7.1 plus or minus 2.0")

      // Double plusOrMinus Int
      val caught4 = intercept[AssertionError] {
        sevenDotOh should not be (7.1 plusOrMinus 2)
      }
      assert(caught4.getMessage === "7.0 was 7.1 plus or minus 2.0")

      // Double plusOrMinus Short
      val caught5 = intercept[AssertionError] {
        sevenDotOh should not be (7.1 plusOrMinus 2.toShort)
      }
      assert(caught5.getMessage === "7.0 was 7.1 plus or minus 2.0")

      // Double plusOrMinus Byte
      val caught6 = intercept[AssertionError] {
        sevenDotOh should not be (7.1 plusOrMinus 2.toByte)
      }
      assert(caught6.getMessage === "7.0 was 7.1 plus or minus 2.0")

      // Float plusOrMinus Float
      val caught7 = intercept[AssertionError] {
        sevenDotOhFloat should not be (7.1f plusOrMinus 0.2f)
      }
      assert(caught7.getMessage === "7.0 was 7.1 plus or minus 0.2")

      // Float plusOrMinus Long
      val caught8 = intercept[AssertionError] {
        sevenDotOhFloat should not be (7.1f plusOrMinus 2L)
      }
      assert(caught8.getMessage === "7.0 was 7.1 plus or minus 2.0")

      // Float plusOrMinus Int
      val caught9 = intercept[AssertionError] {
        sevenDotOhFloat should not be (7.1f plusOrMinus 2)
      }
      assert(caught9.getMessage === "7.0 was 7.1 plus or minus 2.0")

      // Float plusOrMinus Short
      val caught10 = intercept[AssertionError] {
        sevenDotOhFloat should not be (7.1f plusOrMinus 2.toShort)
      }
      assert(caught10.getMessage === "7.0 was 7.1 plus or minus 2.0")

      // Float plusOrMinus Byte
      val caught11 = intercept[AssertionError] {
        sevenDotOhFloat should not be (7.1f plusOrMinus 2.toByte)
      }
      assert(caught11.getMessage === "7.0 was 7.1 plus or minus 2.0")

      // Long plusOrMinus Long
      val caught12 = intercept[AssertionError] {
        sevenLong should not be (9L plusOrMinus 2L)
      }
      assert(caught12.getMessage === "7 was 9 plus or minus 2")

      // Long plusOrMinus Int
      val caught13 = intercept[AssertionError] {
        sevenLong should not be (9L plusOrMinus 2)
      }
      assert(caught13.getMessage === "7 was 9 plus or minus 2")

      // Long plusOrMinus Short
      val caught14 = intercept[AssertionError] {
        sevenLong should not be (9L plusOrMinus 2.toShort)
      }
      assert(caught14.getMessage === "7 was 9 plus or minus 2")

      // Long plusOrMinus Byte
      val caught15 = intercept[AssertionError] {
        sevenLong should not be (9L plusOrMinus 2.toByte)
      }
      assert(caught15.getMessage === "7 was 9 plus or minus 2")

      // Int plusOrMinus Int
      val caught16 = intercept[AssertionError] {
        sevenInt should not be (9 plusOrMinus 2)
      }
      assert(caught16.getMessage === "7 was 9 plus or minus 2")

      // Int plusOrMinus Short
      val caught17 = intercept[AssertionError] {
        sevenInt should not be (9 plusOrMinus 2.toShort)
      }
      assert(caught17.getMessage === "7 was 9 plus or minus 2")

      // Int plusOrMinus Byte
      val caught18 = intercept[AssertionError] {
        sevenInt should not be (9 plusOrMinus 2.toByte)
      }
      assert(caught18.getMessage === "7 was 9 plus or minus 2")

      // Short plusOrMinus Short
      val caught19 = intercept[AssertionError] {
        sevenShort should not be (9.toShort plusOrMinus 2.toShort)
      }
      assert(caught19.getMessage === "7 was 9 plus or minus 2")

      // Short plusOrMinus Byte
      val caught20 = intercept[AssertionError] {
        sevenShort should not be (9.toShort plusOrMinus 2.toByte)
      }
      assert(caught20.getMessage === "7 was 9 plus or minus 2")

      // Byte plusOrMinus Byte
      val caught21 = intercept[AssertionError] {
        sevenByte should not be (9.toByte plusOrMinus 2.toByte)
      }
      assert(caught21.getMessage === "7 was 9 plus or minus 2")
    }
*/
    it("should throw AssertionError if the number is not within the specified range, when used in a logical-and expression") {
/*
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
*/
      // Double plusOrMinus Double
      val caught1 = intercept[AssertionError] {
        sevenDotOh should ((be (17.1 plusOrMinus 0.2)) and (be (17.1 plusOrMinus 0.2)))
      }
      assert(caught1.getMessage === "7.0 was not 17.1 plus or minus 0.2")
      val caught2 = intercept[AssertionError] {
        sevenDotOh should (be (6.9 plusOrMinus 0.2) and (be (17.1 plusOrMinus 0.2)))
      }
      assert(caught2.getMessage === "7.0 was 6.9 plus or minus 0.2, but 7.0 was not 17.1 plus or minus 0.2")
      val caught3 = intercept[AssertionError] {
        sevenDotOh should (be (17.0 plusOrMinus 0.2) and be (7.0 plusOrMinus 0.2))
      }
      assert(caught3.getMessage === "7.0 was not 17.0 plus or minus 0.2")

      // Double plusOrMinus Float
      val caught4 = intercept[AssertionError] {
        sevenDotOh should ((be (17.1 plusOrMinus 0.2f)) and (be (17.1 plusOrMinus 0.2f)))
      }
      assert(caught4.getMessage === "7.0 was not 17.1 plus or minus 0.20000000298023224")
      val caught5 = intercept[AssertionError] {
        sevenDotOh should (be (6.9 plusOrMinus 0.2f) and (be (17.1 plusOrMinus 0.2f)))
      }
      assert(caught5.getMessage === "7.0 was 6.9 plus or minus 0.20000000298023224, but 7.0 was not 17.1 plus or minus 0.20000000298023224")
      val caught6 = intercept[AssertionError] {
        sevenDotOh should (be (17.0 plusOrMinus 0.2f) and be (7.0 plusOrMinus 0.2f))
      }
      assert(caught6.getMessage === "7.0 was not 17.0 plus or minus 0.20000000298023224")

      // Double plusOrMinus Long
      val caught7 = intercept[AssertionError] {
        sevenDotOh should ((be (17.1 plusOrMinus 2L)) and (be (17.1 plusOrMinus 2L)))
      }
      assert(caught7.getMessage === "7.0 was not 17.1 plus or minus 2.0")
      val caught8 = intercept[AssertionError] {
        sevenDotOh should (be (6.9 plusOrMinus 2L) and (be (17.1 plusOrMinus 2L)))
      }
      assert(caught8.getMessage === "7.0 was 6.9 plus or minus 2.0, but 7.0 was not 17.1 plus or minus 2.0")
      val caught9 = intercept[AssertionError] {
        sevenDotOh should (be (17.0 plusOrMinus 2L) and be (7.0 plusOrMinus 2L))
      }
      assert(caught9.getMessage === "7.0 was not 17.0 plus or minus 2.0")

      // Double plusOrMinus Int
      val caught10 = intercept[AssertionError] {
        sevenDotOh should ((be (17.1 plusOrMinus 2)) and (be (17.1 plusOrMinus 2)))
      }
      assert(caught10.getMessage === "7.0 was not 17.1 plus or minus 2.0")
      val caught11 = intercept[AssertionError] {
        sevenDotOh should (be (6.9 plusOrMinus 2) and (be (17.1 plusOrMinus 2)))
      }
      assert(caught2.getMessage === "7.0 was 6.9 plus or minus 0.2, but 7.0 was not 17.1 plus or minus 0.2")
/*
      val caught12 = intercept[AssertionError] {
        sevenDotOh should (be (17.0 plusOrMinus 2) and be (7.0 plusOrMinus 2))
      }
      assert(caught2.getMessage === "")

      // Double plusOrMinus Short
      val caught13 = intercept[AssertionError] {
        sevenDotOh should ((be (17.1 plusOrMinus 2.toShort)) and (be (17.1 plusOrMinus 2.toShort)))
      }
      assert(caught2.getMessage === "")
      val caught14 = intercept[AssertionError] {
        sevenDotOh should (be (6.9 plusOrMinus 2.toShort) and (be (17.1 plusOrMinus 2.toShort)))
      }
      assert(caught2.getMessage === "")
      val caught15 = intercept[AssertionError] {
        sevenDotOh should (be (17.0 plusOrMinus 2.toShort) and be (7.0 plusOrMinus 2.toShort))
      }
      assert(caught2.getMessage === "")

      // Double plusOrMinus Byte
      val caught16 = intercept[AssertionError] {
        sevenDotOh should ((be (17.1 plusOrMinus 2.toByte)) and (be (17.1 plusOrMinus 2.toByte)))
      }
      assert(caught2.getMessage === "")
      val caught17 = intercept[AssertionError] {
        sevenDotOh should (be (6.9 plusOrMinus 2.toByte) and (be (17.1 plusOrMinus 2.toByte)))
      }
      assert(caught2.getMessage === "")
      val caught18 = intercept[AssertionError] {
        sevenDotOh should (be (17.0 plusOrMinus 2.toByte) and be (7.0 plusOrMinus 2.toByte))
      }
      assert(caught2.getMessage === "")

      // Float plusOrMinus Float
      val caught19 = intercept[AssertionError] {
        sevenDotOhFloat should ((be (17.1f plusOrMinus 0.2f)) and (be (17.1f plusOrMinus 0.2f)))
      }
      assert(caught2.getMessage === "")
      val caught20 = intercept[AssertionError] {
        sevenDotOhFloat should (be (6.9f plusOrMinus 0.2f) and (be (17.1f plusOrMinus 0.2f)))
      }
      assert(caught2.getMessage === "")
      val caught21 = intercept[AssertionError] {
        sevenDotOhFloat should (be (17.0f plusOrMinus 0.2f) and be (7.0f plusOrMinus 0.2f))
      }
      assert(caught2.getMessage === "")

      // Float plusOrMinus Long
      val caught22 = intercept[AssertionError] {
        sevenDotOhFloat should ((be (17.1f plusOrMinus 2L)) and (be (17.1f plusOrMinus 2L)))
      }
      assert(caught2.getMessage === "")
      val caught23 = intercept[AssertionError] {
        sevenDotOhFloat should (be (6.9f plusOrMinus 2L) and (be (17.1f plusOrMinus 2L)))
      }
      assert(caught2.getMessage === "")
      val caught24 = intercept[AssertionError] {
        sevenDotOhFloat should (be (17.0f plusOrMinus 2L) and be (7.0f plusOrMinus 2L))
      }
      assert(caught2.getMessage === "")

      // Float plusOrMinus Int
      val caught25 = intercept[AssertionError] {
        sevenDotOhFloat should ((be (17.1f plusOrMinus 2)) and (be (17.1f plusOrMinus 2)))
      }
      assert(caught2.getMessage === "")
      val caught26 = intercept[AssertionError] {
        sevenDotOhFloat should (be (6.9f plusOrMinus 2) and (be (17.1f plusOrMinus 2)))
      }
      assert(caught2.getMessage === "")
      val caught27 = intercept[AssertionError] {
        sevenDotOhFloat should (be (17.0f plusOrMinus 2) and be (7.0f plusOrMinus 2))
      }
      assert(caught2.getMessage === "")

      // Float plusOrMinus Short
      val caught28 = intercept[AssertionError] {
        sevenDotOhFloat should ((be (17.1f plusOrMinus 2.toShort)) and (be (17.1f plusOrMinus 2.toShort)))
      }
      assert(caught2.getMessage === "")
      val caught29 = intercept[AssertionError] {
        sevenDotOhFloat should (be (6.9f plusOrMinus 2.toShort) and (be (17.1f plusOrMinus 2.toShort)))
      }
      assert(caught2.getMessage === "")
      val caught30 = intercept[AssertionError] {
        sevenDotOhFloat should (be (17.0f plusOrMinus 2.toShort) and be (7.0f plusOrMinus 2.toShort))
      }
      assert(caught2.getMessage === "")

      // Float plusOrMinus Byte
      val caught31 = intercept[AssertionError] {
        sevenDotOhFloat should ((be (17.1f plusOrMinus 2.toByte)) and (be (17.1f plusOrMinus 2.toByte)))
      }
      assert(caught2.getMessage === "")
      val caught32 = intercept[AssertionError] {
        sevenDotOhFloat should (be (6.9f plusOrMinus 2.toByte) and (be (17.1f plusOrMinus 2.toByte)))
      }
      assert(caught2.getMessage === "")
      val caught33 = intercept[AssertionError] {
        sevenDotOhFloat should (be (17.0f plusOrMinus 2.toByte) and be (7.0f plusOrMinus 2.toByte))
      }
      assert(caught2.getMessage === "")

      // Long plusOrMinus Long
      val caught34 = intercept[AssertionError] {
        sevenLong should ((be (19L plusOrMinus 2L)) and (be (9L plusOrMinus 2L)))
      }
      assert(caught2.getMessage === "")
      val caught35 = intercept[AssertionError] {
        sevenLong should (be (8L plusOrMinus 2L) and (be (19L plusOrMinus 2L)))
      }
      assert(caught2.getMessage === "")
      val caught36 = intercept[AssertionError] {
        sevenLong should (be (17L plusOrMinus 2L) and be (7L plusOrMinus 2L))
      }
      assert(caught2.getMessage === "")

      // Long plusOrMinus Int
      val caught37 = intercept[AssertionError] {
        sevenLong should ((be (19L plusOrMinus 2)) and (be (9L plusOrMinus 2)))
      }
      assert(caught2.getMessage === "")
      val caught38 = intercept[AssertionError] {
        sevenLong should (be (8L plusOrMinus 2) and (be (19L plusOrMinus 2)))
      }
      assert(caught2.getMessage === "")
      val caught39 = intercept[AssertionError] {
        sevenLong should (be (17L plusOrMinus 2) and be (7L plusOrMinus 2))
      }
      assert(caught2.getMessage === "")

      // Long plusOrMinus Short
      val caught40 = intercept[AssertionError] {
        sevenLong should ((be (19L plusOrMinus 2.toShort)) and (be (9L plusOrMinus 2.toShort)))
      }
      assert(caught2.getMessage === "")
      val caught41 = intercept[AssertionError] {
        sevenLong should (be (8L plusOrMinus 2.toShort) and (be (19L plusOrMinus 2.toShort)))
      }
      assert(caught2.getMessage === "")
      val caught42 = intercept[AssertionError] {
        sevenLong should (be (17L plusOrMinus 2.toShort) and be (7L plusOrMinus 2.toShort))
      }
      assert(caught2.getMessage === "")

      // Long plusOrMinus Byte
      val caught43 = intercept[AssertionError] {
        sevenLong should ((be (19L plusOrMinus 2.toByte)) and (be (9L plusOrMinus 2.toByte)))
      }
      assert(caught2.getMessage === "")
      val caught44 = intercept[AssertionError] {
        sevenLong should (be (8L plusOrMinus 2.toByte) and (be (19L plusOrMinus 2.toByte)))
      }
      assert(caught2.getMessage === "")
      val caught45 = intercept[AssertionError] {
        sevenLong should (be (17L plusOrMinus 2.toByte) and be (7L plusOrMinus 2.toByte))
      }
      assert(caught2.getMessage === "")

      // Int plusOrMinus Int
      val caught46 = intercept[AssertionError] {
        sevenInt should ((be (19 plusOrMinus 2)) and (be (9 plusOrMinus 2)))
      }
      assert(caught2.getMessage === "")
      val caught47 = intercept[AssertionError] {
        sevenInt should (be (8 plusOrMinus 2) and (be (19 plusOrMinus 2)))
      }
      assert(caught2.getMessage === "")
      val caught48 = intercept[AssertionError] {
        sevenInt should (be (17 plusOrMinus 2) and be (7 plusOrMinus 2))
      }
      assert(caught2.getMessage === "")

      // Int plusOrMinus Short
      val caught49 = intercept[AssertionError] {
        sevenInt should ((be (19 plusOrMinus 2.toShort)) and (be (9 plusOrMinus 2.toShort)))
      }
      assert(caught2.getMessage === "")
      val caught50 = intercept[AssertionError] {
        sevenInt should (be (8 plusOrMinus 2.toShort) and (be (19 plusOrMinus 2.toShort)))
      }
      assert(caught2.getMessage === "")
      val caught51 = intercept[AssertionError] {
        sevenInt should (be (17 plusOrMinus 2.toShort) and be (7 plusOrMinus 2.toShort))
      }
      assert(caught2.getMessage === "")

      // Int plusOrMinus Byte
      val caught52 = intercept[AssertionError] {
        sevenInt should ((be (19 plusOrMinus 2.toByte)) and (be (9 plusOrMinus 2.toByte)))
      }
      assert(caught2.getMessage === "")
      val caught53 = intercept[AssertionError] {
        sevenInt should (be (8 plusOrMinus 2.toByte) and (be (19 plusOrMinus 2.toByte)))
      }
      assert(caught2.getMessage === "")
      val caught54 = intercept[AssertionError] {
        sevenInt should (be (17 plusOrMinus 2.toByte) and be (7 plusOrMinus 2.toByte))
      }
      assert(caught2.getMessage === "")

      // Short plusOrMinus Short
      val caught55 = intercept[AssertionError] {
        sevenShort should ((be (19.toShort plusOrMinus 2.toShort)) and (be (9.toShort plusOrMinus 2.toShort)))
      }
      assert(caught2.getMessage === "")
      val caught56 = intercept[AssertionError] {
        sevenShort should (be (8.toShort plusOrMinus 2.toShort) and (be (19.toShort plusOrMinus 2.toShort)))
      }
      assert(caught2.getMessage === "")
      val caught57 = intercept[AssertionError] {
        sevenShort should (be (17.toShort plusOrMinus 2.toShort) and be (7.toShort plusOrMinus 2.toShort))
      }
      assert(caught2.getMessage === "")

      // Short plusOrMinus Byte
      val caught58 = intercept[AssertionError] {
        sevenShort should ((be (19.toShort plusOrMinus 2.toByte)) and (be (9.toShort plusOrMinus 2.toByte)))
      }
      assert(caught2.getMessage === "")
      val caught59 = intercept[AssertionError] {
        sevenShort should (be (8.toShort plusOrMinus 2.toByte) and (be (19.toShort plusOrMinus 2.toByte)))
      }
      assert(caught2.getMessage === "")
      val caught60 = intercept[AssertionError] {
        sevenShort should (be (17.toShort plusOrMinus 2.toByte) and be (7.toShort plusOrMinus 2.toByte))
      }
      assert(caught2.getMessage === "")

      // Byte plusOrMinus Byte
      val caught61 = intercept[AssertionError] {
        sevenByte should ((be (19.toByte plusOrMinus 2.toByte)) and (be (9.toByte plusOrMinus 2.toByte)))
      }
      assert(caught2.getMessage === "")
      val caught62 = intercept[AssertionError] {
        sevenByte should (be (8.toByte plusOrMinus 2.toByte) and (be (19.toByte plusOrMinus 2.toByte)))
      }
      assert(caught2.getMessage === "")
      val caught63 = intercept[AssertionError] {
        sevenByte should (be (17.toByte plusOrMinus 2.toByte) and be (7.toByte plusOrMinus 2.toByte))
      }
      assert(caught2.getMessage === "")
*/
    }

/*
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
