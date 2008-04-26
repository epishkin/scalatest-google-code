package org.scalatest.scalacheck

import org.scalatest.Suite
import org.scalacheck.Arbitrary
import org.scalacheck.Arb
import org.scalacheck.Prop
import org.scalacheck.Test.Params
import org.scalacheck.Test
import org.scalacheck.Test._

trait CheckSuite extends Suite {

  /**
   * Convert the passed 1-arg function into a property, and check it.
   */
  def checkProperty[A1,P](f:  A1 => P)
    (implicit
      p: P => Prop,
      a1: Arb[A1] => Arbitrary[A1]
    ) {
    checkProperty(Prop.property(f)(p, a1))
  }

  /**
   * Convert the passed 2-arg function into a property, and check it.
   */
  def checkProperty[A1,A2,P](f: (A1,A2) => P)
    (implicit
      p: P => Prop,
      a1: Arb[A1] => Arbitrary[A1],
      a2: Arb[A2] => Arbitrary[A2]
    ) {
    checkProperty(Prop.property(f)(p, a1, a2))
  }

  /**
   * Convert the passed 3-arg function into a property, and check it.
   */
  def checkProperty[A1,A2,A3,P](f: (A1,A2,A3) => P)
    (implicit
      p: P => Prop,
      a1: Arb[A1] => Arbitrary[A1],
      a2: Arb[A2] => Arbitrary[A2],
      a3: Arb[A3] => Arbitrary[A3]
    ) {
    checkProperty(Prop.property(f)(p, a1, a2, a3))
  }

  /**
   * Convert the passed 4-arg function into a property, and check it.
   */
  def checkProperty[A1,A2,A3,A4,P](f: (A1,A2,A3,A4) => P)
    (implicit
      p: P => Prop,
      a1: Arb[A1] => Arbitrary[A1],
      a2: Arb[A2] => Arbitrary[A2],
      a3: Arb[A3] => Arbitrary[A3],
      a4: Arb[A4] => Arbitrary[A4]
    ) {
    checkProperty(Prop.property(f)(p, a1, a2, a3, a4))
  }

  /**
   * Convert the passed 5-arg function into a property, and check it.
   */
  def checkProperty[A1,A2,A3,A4,A5,P](f: (A1,A2,A3,A4,A5) => P)
    (implicit
      p: P => Prop,
      a1: Arb[A1] => Arbitrary[A1],
      a2: Arb[A2] => Arbitrary[A2],
      a3: Arb[A3] => Arbitrary[A3],
      a4: Arb[A4] => Arbitrary[A4],
      a5: Arb[A5] => Arbitrary[A5]
    ) {
    checkProperty(Prop.property(f)(p, a1, a2, a3, a4, a5))
  }

  /**
   * Convert the passed 6-arg function into a property, and check it.
   */
  def checkProperty[A1,A2,A3,A4,A5,A6,P](f: (A1,A2,A3,A4,A5,A6) => P)
    (implicit
      p: P => Prop,
      a1: Arb[A1] => Arbitrary[A1],
      a2: Arb[A2] => Arbitrary[A2],
      a3: Arb[A3] => Arbitrary[A3],
      a4: Arb[A4] => Arbitrary[A4],
      a5: Arb[A5] => Arbitrary[A5],
      a6: Arb[A6] => Arbitrary[A6]
    ) {
    checkProperty(Prop.property(f)(p, a1, a2, a3, a4, a5, a6))
  }

  /**
   * Checks a property with the given testing parameters.
   */
  def checkProperty(p: Prop, prms: Params) {
    val stats = Test.check(prms, p, (r,s,d) => ())
    val result = stats.result
    result match {
      case r: Passed => 
      case r: Failed => fail()
      case r: Exhausted => fail()
      case r: PropException => fail()
      case r: GenException => fail()
    }
    println("********" + result)
  }

  /**
   * Checks a property.
   */
  def checkProperty(p: Prop) {
    checkProperty(p, Test.defaultParams)
  }
}
