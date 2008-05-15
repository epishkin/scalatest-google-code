package org.scalatest.junit

import _root_.junit.framework.Assert

/**
 * Trait that provides convenient access to the JUnit 3's assertion methods, which
 * are declared as static methods in <code>junit.framework.Assert</code>. You must
 * include JUnit 3's JAR file on the class or run path to execute suites mixing in this trait.
 * For example, you could use JUnit 3 assertions in a <code>Suite</code> like this:
 * <pre>
 * import org.scalatest.Suite
 * import org.scalatest.junit.JUnit3Asserts
 *
 * class MySuite extends Suite with JUnit3Asserts {
 *
 *   def testAddition() {
 *     val sum = 1 + 1
 *     assertEquals(2, sum)
 *     assertEquals(4, sum + 2)
 *   }
 *
 *   def testSubtraction() {
 *     val diff = 4 - 1
 *     assertEquals(3, diff)
 *     assertEquals(1, diff - 2)
 *   }
 * }
 * </pre>
 *
 */
trait JUnit3Asserts {

  /**
   * Asserts that a condition is true. If it isn't it throws
   * an AssertionFailedError with the given message.
   */
  def assertTrue(message: String, condition: Boolean) {
    Assert.assertTrue(message, condition)
  }

  /**
   * Asserts that a condition is true. If it isn't it throws
   * an AssertionFailedError.
   */
  def assertTrue(condition: Boolean) {
    Assert.assertTrue(condition)
  }

  /**
   * Asserts that a condition is false. If it isn't it throws
   * an AssertionFailedError with the given message.
   */
  def assertFalse(message: String, condition: Boolean) {
    Assert.assertFalse(message, condition)
  }

  /**
   * Asserts that a condition is false. If it isn't it throws
   * an AssertionFailedError.
   */
  def assertFalse(condition: Boolean) {
    Assert.assertFalse(condition)
  }

  /**
   * Asserts that two objects are equal. If they are not
   * an AssertionFailedError is thrown with the given message.
   */
  def assertEquals(message: String, expected: AnyRef, actual: AnyRef) {
    Assert.assertEquals(message, expected, actual)
  }

  /**
   * Asserts that two objects are equal. If they are not
   * an AssertionFailedError is thrown.
   */
  def assertEquals(expected: AnyRef, actual: AnyRef) {
      Assert.assertEquals(expected, actual)
  }

  /**
   * Asserts that two Strings are equal. 
   */
  def assertEquals(message: String, expected: String, actual: String) {
    Assert.assertEquals(message, expected, actual)
  }

  /**
   * Asserts that two Strings are equal. 
   */
  def assertEquals(expected: String, actual: String) {
      Assert.assertEquals(expected, actual)
  }

  /**
   * Asserts that two doubles are equal concerning a delta.  If they are not
   * an AssertionFailedError is thrown with the given message.  If the expected
   * value is infinity then the delta value is ignored.
   */
  def assertEquals(message: String, expected: Double, actual: Double, delta: Double) {
    Assert.assertEquals(message, expected, actual)
  }

  /**
   * Asserts that two doubles are equal concerning a delta. If the expected
   * value is infinity then the delta value is ignored.
   */
  def assertEquals(expected: Double, actual: Double, delta: Double) {
    Assert.assertEquals(expected, actual, delta)
  }

  /**
   * Asserts that two floats are equal concerning a delta. If they are not
   * an AssertionFailedError is thrown with the given message.  If the expected
   * value is infinity then the delta value is ignored.
   */
  def assertEquals(message: String, expected: Float, actual: Float, delta: Float) {
    Assert.assertEquals(message, expected, actual, delta)
  }

  /**
   * Asserts that two floats are equal concerning a delta. If the expected
   * value is infinity then the delta value is ignored.
   */
  def assertEquals(expected: Float, actual: Float, delta: Float) {
    Assert.assertEquals(expected, actual, delta)
  }

  /**
   * Asserts that two longs are equal. If they are not
   * an AssertionFailedError is thrown with the given message.
   */
  def assertEquals(message: String, expected: Long, actual: Long) {
    Assert.assertEquals(message, expected, actual)
  }

  /**
   * Asserts that two longs are equal.
   */
  def assertEquals(expected: Long, actual: Long) {
    Assert.assertEquals(expected, actual)
  }

  /**
   * Asserts that two booleans are equal. If they are not
   * an AssertionFailedError is thrown with the given message.
   */
  def assertEquals(message: String, expected: Boolean, actual: Boolean) {
    Assert.assertEquals(message, expected, actual)
  }

  /**
   * Asserts that two booleans are equal.
   */
  def assertEquals(expected: Boolean, actual: Boolean) {
    Assert.assertEquals(expected, actual)
  }

  /**
   * Asserts that two bytes are equal. If they are not
   * an AssertionFailedError is thrown with the given message.
   */
  def assertEquals(message: String, expected: Byte, actual: Byte) {
    Assert.assertEquals(message, expected, actual)
  }

  /**
   * Asserts that two bytes are equal.
   */
  def assertEquals(expected: Byte, actual: Byte) {
    Assert.assertEquals(expected, actual)
  }

  /**
   * Asserts that two chars are equal. If they are not
   * an AssertionFailedError is thrown with the given message.
   */
  def assertEquals(message: String, expected: Char, actual: Char) {
    Assert.assertEquals(message, expected, actual)
  }

  /**
   * Asserts that two chars are equal.
   */
  def assertEquals(expected: Char, actual: Char) {
    Assert.assertEquals(expected, actual)
  }

  /**
   * Asserts that two shorts are equal. If they are not
   * an AssertionFailedError is thrown with the given message.
   */
  def assertEquals(message: String, expected: Short, actual: Short) {
    Assert.assertEquals(message, expected, actual)
  }

  /**
   * Asserts that two shorts are equal.
   */
  def assertEquals(expected: Short, actual: Short) {
    Assert.assertEquals(expected, actual)
  }

  /**
   * Asserts that two ints are equal. If they are not
   * an AssertionFailedError is thrown with the given message.
   */
  def assertEquals(message: String, expected: Int, actual: Int) {
    Assert.assertEquals(message, expected, actual)
  }

  /**
   * Asserts that two ints are equal.
   */
  def assertEquals(expected: Int, actual: Int) {
    Assert.assertEquals(expected, actual)
  }

  /**
   * Asserts that an object isn't null.
   */
  def assertNotNull(anyRef: AnyRef) {
    Assert.assertNotNull(anyRef)
  }

  /**
   * Asserts that an object isn't null. If it is
   * an AssertionFailedError is thrown with the given message.
   */
  def assertNotNull(message: String, anyRef: AnyRef) {
    Assert.assertNotNull(message, anyRef)
  }

  /**
   * Asserts that an object is null.
   */
  def assertNull(anyRef: AnyRef) {
    Assert.assertNull(anyRef)
  }

  /**
   * Asserts that an object is null.  If it is not
   * an AssertionFailedError is thrown with the given message.
   */
  def assertNull(message: String, anyRef: AnyRef) {
    Assert.assertNull(message, anyRef)
  }

  /**
   * Asserts that two objects refer to the same object. If they are not
   * an AssertionFailedError is thrown with the given message.
   */
  def assertSame(message: String, expected: AnyRef, actual: AnyRef) {
    Assert.assertSame(message, expected, actual)
  }

  /**
   * Asserts that two objects refer to the same object. If they are not
   * the same an AssertionFailedError is thrown.
   */
  def assertSame(expected: AnyRef, actual: AnyRef) {
    Assert.assertSame(expected, actual)
  }

  /**
   * Asserts that two objects refer to the same object. If they are not
   * an AssertionFailedError is thrown with the given message.
   */
  def assertNotSame(message: String, expected: AnyRef, actual: AnyRef) {
    Assert.assertNotSame(message, expected, actual)
  }

  /**
   * Asserts that two objects refer to the same object. If they are not
   * the same an AssertionFailedError is thrown.
   */
  def assertNotSame(expected: AnyRef, actual: AnyRef) {
    Assert.assertNotSame(expected, actual)
  }
}
