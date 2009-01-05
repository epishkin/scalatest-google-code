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

import scala.reflect.Manifest

/**
 * <p>
 * The next release of ScalaTest will include two new traits, <code>ShouldMatchers</code> and <code>MustMatchers</code>. These two traits are basically identical except where <code>ShouldMatchers</code> says <code>should</code>, <code>MustMatchers</code> says <code>must</code>. None of the suite traits will mix either of these in by default, for two reasons. One is that some people prefer "should," and others "must," and this way everyone can select the verb they prefer. Also, both matchers traits involve a lot of implicit conversions, and I prefer that people invite these conversion into their test code explicitly. In this blog post, I'll show <code>ShouldMatchers</code> examples.
 * </p>
 * 
 * <p>
 * Matchers represent a kind of domain specific language (DSL) for assertions, which read a bit more like natural language in the source code, and provide more detailed error messages in assertions when they fail. Some examples of matchers in other test frameworks are <a href="http://code.google.com/p/hamcrest/wiki/Tutorial">Hamcrest matchers</a> (Java), <a href="http://rspec.rubyforge.org/rspec/1.1.11/classes/Spec/Matchers.html">RSpec matchers</a> (Ruby), <a href="http://easyb.org/dsls.html">easyb's ensure syntax</a> (Groovy), and <a href="http://code.google.com/p/specs/wiki/MatchersGuide">specs matchers</a> (Scala).
 * </p>
 * 
 * <p>
 * For example, for a basic equality comparison with ScalaTest matchers, you can say:
 * </p>
 * <pre class="indent">
 * object should equal (3)
 * </pre>
 * <p>
 * Here <code>object</code> is a variable, and can be of any type. If the object is an <code>Int</code> with the value 3, nothing will happen. Otherwise, an assertion error will be thrown with the detail message, such as <code>"7 did not equal 3"</code>.
 * </p>
 * 
 * <h2>Checking size and length</h2>
 * 
 * <p>
 * You can check for a size of length of just about any type of object for which it would make sense. Here's how checking for length looks:
 * </p>
 * <pre class="indent">
 * object should have length (3)
 * </pre>
 * 
 * <p>
 * This syntax can be used with any object that has a field or method named <code>length</code> or a method named <code>getLength</code>. (The Scala compiler will check for this at compile time.) Size is similar:
 * </p>
 * 
 * <pre class="indent">
 * object should have size (10)
 * </pre>
 * 
 * <h2>Checking strings</h2>
 * 
 * <p>
 * You can check for whether a string starts with, ends with, or includes a substring or regular expression, like this:
 * </p>
 * 
 * <pre class="indent">
 * string should startWith substring ("Hello")
 * string should startWith regex ("Hel*o")
 * string should endWith substring ("world")
 * string should endWith regex ("wo.ld")
 * string should include substring ("seven")
 * string should include regex ("wo.ld")
 * </pre>
 * 
 * <p>
 * You can check whether a string fully matches a regular expression, like this:
 * </p>
 * 
 * <pre class="indent">
 * string should fullyMatch regex (decimal)
 * </pre>
 * 
 * <h2>Greater and less than</h2>
 * <p>
 * You can check whether any type that either is, or can be implicitly converted to,
 * an <code>Ordered[T]</code> is greater than, less than, greater than or equal, or less
 * than or equal to a value of type <code>T</code>, like this: 
 * </p>
 * <pre class="indent">
 * one should be < (7)
 * one should be > (0)
 * one should be <= (7)
 * one should be >= (0)
 * </pre>
 * 
 * <h2>Checking predicate methods</h2>
 * 
 * <p>
 * If an object has a method that takes no parameters and returns boolean, you can check it by placing a <code>Symbol</code> (after <code>be</code>) that specifies the name of the method (excluding an optional prefix of "<code>is</code>"). A symbol literal in Scala begins with a tick mark and ends at the first non-identifier character. Thus, <code>'empty</code> results in a <code>Symbol</code> object at runtime, as does <code>'defined</code> and <code>'file</code>. Here's an example:
 * </p>
 * 
 * <pre class="indent">
 * emptySet should be ('empty)
 * </pre>
 * 
 * Given this code, ScalaTest will use reflection to look on the object referenced from <code>emptySet</code> for a method that takes no parameters and results in <code>Boolean</code>, with either the name <code>empty</code> or <code>isEmpty</code>. If found, it invokes that method. If the method returns <code>true</code>, nothing happens. But if it returns <code>false</code>, an assertion error will be thrown that will contain a detail message like:
 * 
 * <pre class="indent">
 * Set(1, 2, 3) was not empty</code>
 * </pre>
 * 
 * <p>
 * This <code>be</code> syntax can be used with any type, as there's no way in Scala's type system to restrict it just to types that have an appropriate method. If the object does not have an appropriately named predicate method, you'll get an <code>IllegalArgumentException</code> at runtime with a detail message that explains the problem. (Such errors could be caught at compile time, however, with a compiler plug-in.)  
 * </p>
 * 
 * <p>
 * If you think it reads better, you can optionally put <code>a</code> or <code>an</code> after <code>be</code>. For example, <code>java.util.File</code> has two predicate methods, <code>isFile</code> and <code>isDirectory</code>. Thus with a <code>File</code> object named <code>temp</code>, you could write:
 * </p>
 * 
 * <pre class="indent">
 * temp should be a ('file)
 * </pre>
 * 
 * <h2>Checking object identity</h2>
 * 
 * <p>
 * If you need to check that two references refer to the exact same object, you can write:
 * </p>
 * 
 * <pre class="indent">
 * ref1 should be theSameInstanceAs (ref2)
 * </pre>
 * 
 * <h2>Working with floating point numbers</h2>
 * 
 * <p>
 * To check wether a floating point number has a value that exactly matches another, you can use <code>should equal</code>: 
 * </p>
 * 
 * <pre class="indent">
 * sevenDotOh should equal (7.0)
 * </pre>
 * 
 * <p>
 * Sometimes, however, you may want to check whether a floating point number is within a range. You can do that using <code>be</code> and <code>plusOrMinus</code>, like this:
 * </p>
 * 
 * <pre class="indent">
 * sevenDotOh should be (6.9 plusOrMinus 0.2)
 * </pre>
 * 
 * <p>
 * This expression will cause an assertion error to be thrown if the floating point value, <code>sevenDotOh</code> is outside the range <code>6.7</code> to <code>7.1</code>.
 * </p>
 * 
 * <h2>Iterables, collections, sequences, and maps</h2>
 * 
 * <p>
 * You can use some of the syntax shown previously with <code>Iterable</code> and its subtypes. For example, you can check whether an <code>Iterable</code> is <code>empty</code>, like this:
 * </p>
 * 
 * <pre class="indent">
 * iterable should be ('empty)
 * </pre>
 * 
 * <p>
 * You can check the length of an <code>Seq</code> (<code>Array</code>, <code>List</code>, etc.), like this:
 * </p>
 * 
 * <pre class="indent">
 * array should have length (3)
 * list should have length (9)
 * </pre>
 * 
 * <p>
 * You can check the size of any <code>Collection</code>, like this:
 * </p>
 * 
 * <pre class="indent">
 * map should have size (20)
 * set should have size (90)
 * </pre>
 * 
 * <p>
 * In addition, you can check whether an <code>Iterable</code> contains a particular
 * element, like this:
 * </p>
 * 
 * <pre class="indent">
 * iterable should contain element ("five")
 * </pre>
 * 
 * <p>
 * You can also check whether a <code>Map</code> contains a particular key, or value, like this:
 * </p>
 * 
 * <pre class="indent">
 * map should contain key (1)
 * map should contain value ("Howdy")
 * </pre>
 * 
 * <h2>Be as an equality comparison</h2>
 * 
 * <p>
 * All uses of <code>be</code> other than those shown previously work the same as if <code>be</code> were replaced by <code>equals</code>. This will be the only redundancy
 * in the first release of ScalaTest matchers. It is there because it enables syntax
 * that sounds more natural. For example, instead of writing: 
 * </p>
 * 
 * <pre class="indent">
 * result should equal (null)
 * </pre>
 * 
 * <p>
 * You can write:
 * </p>
 * 
 * <pre class="indent">
 * result should be (null)
 * </pre>
 * 
 * <p>
 * (Hopefully you won't write that too much given <code>null</code> is error prone, and <code>Option</code> is usually a better, well, option.) Here are some other examples of <code>be</code> used for equality comparison:
 * </p>
 * 
 * <pre class="indent">
 * sum should be (7.0)
 * boring should be (false)
 * fun should be (true)
 * list should be (Nil)
 * </pre>
 * 
 * <h2>Being negative</h2>
 * 
 * <p>
 * If you wish to check the opposite of some condition, you can use <code>not</code>. However, when you use <code>not</code>, you must enclose the expression being negated in parentheses or curly braces. Here are a few examples:
 * </p>
 * 
 * <pre class="indent">
 * object should not (be (null))
 * sum should not { be <= 10 }
 * mylist should not (equal (yourList))
 * string should not { startWith substring ("Hello") }
 * </pre>
 * 
 * <h2>Combining matchers with <code>and</code> and/or <code>or</code></h2>
 * 
 * <p>
 * You can also combine matcher expressions with <code>and</code> and <code>or</code>, however, you must usually place parentheses or curly braces around the larger
 * (<code>and</code> or <code>or</code>) expression. Here are a few examples:
 * </p>
 * 
 * <pre class="indent">
 * ten should { equal (2 * 5) and equal (12 - 2) }
 * one should { equal (999) or equal (2 - 1) }
 * one should { not (be >= 7) and equal (2 - 1) }
 * </pre>
 * 
 * <h2>Working with <code>Option</code>s</h2>
 * 
 * <p>
 * ScalaTest matchers has no special support for <code>Option</code>s, but you can 
 * work with them quite easily using syntax shown previously. For example, if you wish to check
 * whether an option is <code>None</code>, you can write any of:
 * </p>
 * 
 * <pre class="indent">
 * option should equal (None)
 * option should be (None)
 * option should not { be ('defined) }
 * </pre>
 * 
 * <p>
 * If you wish to check an option is defined, and holds a specific value, you can write either of:
 * </p>
 * 
 * <pre class="indent">
 * option should equal (Some("hi"))
 * option should be (Some("ho"))
 * </pre>
 * 
 * <p>
 * If you only wish to check that an option is defined, but don't care what it's value is, you can write:
 * </p>
 * 
 * <pre class="indent">
 * option should be ('defined)
 * </pre>
 * 
 * <h2>Those pesky parens</h2>
 * 
 * <p>
 * You may have noticed that I always put parentheses on the last token in the expressions I've shown. This not always required, but the rule is a bit subtle. If the number of tokens in the expression is odd, the parentheses are not needed. But if the number of tokens is even, the parentheses are required. As a result, I usually include them, because then there's no subtle rule to remember. In addition, although ScalaTest matchers doesn't define which value is "actual" and which "expected," I usually put the expected value last and I think wrapping it in parentheses emphasizes the expected value nicely. Nevertheless, you're free to leave them off in many cases, and you may feel it makes the code more readable. Here are some expressions that work without parentheses:
 * </p>
 * 
 * <pre class="indent">
 * object should have length 3
 * object should have size 10
 * string should startWith substring "Hello"
 * string should startWith regex "Hel*o"
 * string should endWith substring "world"
 * string should endWith regex "wo.ld"
 * string should include substring "seven"
 * string should include regex "wo.ld"
 * string should fullyMatch regex decimal
 * one should be < 7
 * one should be > 0
 * one should be <= 7
 * one should be >= 0
 * temp should be a 'file
 * object1 should be theSameInstanceAs object2
 * iterable should contain element "five"
 * map should contain key 1
 * map should contain value "Howdy"
 * </pre>
 * 
 * 
 *
 */
trait ShouldMatchers extends Matchers {

  // TODO: In the tests, make sure they can create their own matcher and use it.
  protected trait ShouldMethods[T] {
    protected val leftOperand: T
    def should(rightMatcher: Matcher[T]) {
      rightMatcher(leftOperand) match {
        case MatcherResult(false, failureMessage, _) => throw new AssertionError(failureMessage)
        case _ => ()
      }
    }

    // This one supports it should behave like
    def should(behaveWord: BehaveWord) = new ResultOfBehaveWord[T](leftOperand)
    def should(beWord: BeWord): ResultOfBeWord[T] = new ResultOfBeWord(leftOperand, true)
  }

  protected class ShouldWrapper[T](left: T) extends { val leftOperand = left } with ShouldMethods[T]

  // I think the type hasn't been converted yet here. It is just a pass-through. It finally gets
  // converted in ResultOfHaveWordForLengthWrapper, at which point the actual implicit conversions
  // from String, Array, and the structural types get applied.
  protected class LengthShouldWrapper[A <% LengthWrapper](left: A) extends { val leftOperand = left } with ShouldMethods[A] {
    def should(haveWord: HaveWord): ResultOfHaveWordForLengthWrapper[A] = {
      new ResultOfHaveWordForLengthWrapper(left, true)
    }
  }

  protected class SizeShouldWrapper[A <% SizeWrapper](left: A) extends { val leftOperand = left } with ShouldMethods[A] {
    def should(haveWord: HaveWord): ResultOfHaveWordForSizeWrapper[A] = {
      new ResultOfHaveWordForSizeWrapper(left, true)
    }
  }

  protected class StringShouldWrapper(left: String) extends { val leftOperand = left } with ShouldMethods[String] {
    def should(haveWord: HaveWord): ResultOfHaveWordForString = {
      new ResultOfHaveWordForString(left, true)
    }
    def should(includeWord: IncludeWord): ResultOfIncludeWordForString = {
      new ResultOfIncludeWordForString(left, true)
    }
    def should(startWithWord: StartWithWord): ResultOfStartWithWordForString = {
      new ResultOfStartWithWordForString(left, true)
    }
    def should(endWithWord: EndWithWord): ResultOfEndWithWordForString = {
      new ResultOfEndWithWordForString(left, true)
    }
    def should(fullyMatchWord: FullyMatchWord): ResultOfFullyMatchWordForString = {
      new ResultOfFullyMatchWordForString(left, true)
    }
  }

  protected class MapShouldWrapper[K, V](left: scala.collection.Map[K, V]) extends { val leftOperand = left } with ShouldMethods[scala.collection.Map[K, V]]
      with ShouldHaveWordForCollectionMethods[(K, V)] {

    def should(containWord: ContainWord): ResultOfContainWordForMap[K, V] = {
      new ResultOfContainWordForMap(left, true)
    }
  }
  
  protected trait ShouldContainWordForIterableMethods[T] {
    protected val leftOperand: Iterable[T]
    def should(containWord: ContainWord): ResultOfContainWordForIterable[T] = {
      new ResultOfContainWordForIterable(leftOperand, true)
    }
  }

  protected trait ShouldHaveWordForCollectionMethods[T] {
    protected val leftOperand: Collection[T]
    def should(haveWord: HaveWord): ResultOfHaveWordForCollection[T] = {
      new ResultOfHaveWordForCollection(leftOperand, true)
    }
  }
  
  protected trait ShouldHaveWordForJavaCollectionMethods[T] {
    protected val leftOperand: java.util.Collection[T]
    def should(haveWord: HaveWord): ResultOfHaveWordForJavaCollection[T] = {
      new ResultOfHaveWordForJavaCollection(leftOperand, true)
    }
  }

  protected trait ShouldHaveWordForSeqMethods[T] {
    protected val leftOperand: Seq[T]
    def should(haveWord: HaveWord): ResultOfHaveWordForSeq[T] = {
      new ResultOfHaveWordForSeq(leftOperand, true)
    }
  }
  
  protected trait ShouldHaveWordForJavaListMethods[T] {
    protected val leftOperand: java.util.List[T]
    def should(haveWord: HaveWord): ResultOfHaveWordForJavaList[T] = {
      new ResultOfHaveWordForJavaList(leftOperand, true)
    }
  }

  protected class CollectionShouldWrapper[T](left: Collection[T]) extends { val leftOperand = left } with ShouldMethods[Collection[T]]
      with ShouldContainWordForIterableMethods[T] with ShouldHaveWordForCollectionMethods[T]
  
  protected class JavaCollectionShouldWrapper[T](left: java.util.Collection[T]) extends { val leftOperand = left } with ShouldMethods[java.util.Collection[T]]
      with ShouldHaveWordForJavaCollectionMethods[T]

  protected class SeqShouldWrapper[T](left: Seq[T]) extends { val leftOperand = left } with ShouldMethods[Seq[T]]
      with ShouldContainWordForIterableMethods[T] with ShouldHaveWordForSeqMethods[T]
  
  protected class ArrayShouldWrapper[T](left: Array[T]) extends { val leftOperand = left } with ShouldMethods[Array[T]]
      with ShouldContainWordForIterableMethods[T] with ShouldHaveWordForSeqMethods[T]
  
  protected class ListShouldWrapper[T](left: List[T]) extends { val leftOperand = left } with ShouldMethods[List[T]]
      with ShouldContainWordForIterableMethods[T] with ShouldHaveWordForSeqMethods[T]

  protected class JavaListShouldWrapper[T](left: java.util.List[T]) extends { val leftOperand = left } with ShouldMethods[java.util.List[T]]
      with ShouldHaveWordForJavaListMethods[T]

  // TODO:  Test some java.util.maps, etc.
  implicit def convertToShouldWrapper[T](o: T): ShouldWrapper[T] = new ShouldWrapper(o)
  implicit def convertToCollectionShouldWrapper[T](o: Collection[T]): CollectionShouldWrapper[T] = new CollectionShouldWrapper[T](o)
  implicit def convertToSeqShouldWrapper[T](o: Seq[T]): SeqShouldWrapper[T] = new SeqShouldWrapper[T](o)
  implicit def convertToArrayShouldWrapper[T](o: Array[T]): ArrayShouldWrapper[T] = new ArrayShouldWrapper[T](o)
  implicit def convertToListShouldWrapper[T](o: List[T]): ListShouldWrapper[T] = new ListShouldWrapper[T](o)
  implicit def convertToMapShouldWrapper[K, V](o: scala.collection.Map[K, V]): MapShouldWrapper[K, V] = new MapShouldWrapper[K, V](o)
  implicit def convertToStringShouldWrapper[K, V](o: String): StringShouldWrapper = new StringShouldWrapper(o)

  // One problem, though, is java.List doesn't have a length field, method, or getLength method, but I'd kind
  // of like to have it work with should have length too, so I have to do one for it explicitly here.
  implicit def convertToJavaCollectionShouldWrapper[T](o: java.util.Collection[T]): JavaCollectionShouldWrapper[T] = new JavaCollectionShouldWrapper[T](o)
  implicit def convertToJavaListShouldWrapper[T](o: java.util.List[T]): JavaListShouldWrapper[T] = new JavaListShouldWrapper[T](o)

  // This implicit conversion is just used to trigger the addition of the should method. The LengthShouldWrapper
  // doesn't actually convert them, just passes it through. The conversion that happens here is to LengthShouldWrapper,
  // and later, inside ResultOfHaveWordForLengthWrapper, the implicit conversion from T to LengthWrapper takes place. So
  // weirdly enough, here strings are treated structurally for the implicit that adds the should, but later they are
  // treated nominally by the implicit conversion from plain old String to StringLengthWrapper. So when length is
  // ultimately invoked up in ResultOfHaveWordForLengthWrapper, it is done directly, not with reflection. That's my
  // theory anyway.
  implicit def convertHasIntGetLengthMethodToLengthShouldWrapper[T <:{ def getLength(): Int}](o: T): LengthShouldWrapper[T] = new LengthShouldWrapper[T](o)
  implicit def convertHasIntGetLengthFieldToLengthShouldWrapper[T <:{ val getLength: Int}](o: T): LengthShouldWrapper[T] = new LengthShouldWrapper[T](o)
  implicit def convertHasIntLengthFieldToLengthShouldWrapper[T <:{ val length: Int}](o: T): LengthShouldWrapper[T] = new LengthShouldWrapper[T](o)
  implicit def convertHasIntLengthMethodToLengthShouldWrapper[T <:{ def length(): Int}](o: T): LengthShouldWrapper[T] = new LengthShouldWrapper[T](o)

  implicit def convertHasLongGetLengthMethodToLengthShouldWrapper[T <:{ def getLength(): Long}](o: T): LengthShouldWrapper[T] = new LengthShouldWrapper[T](o)
  implicit def convertHasLongGetLengthFieldToLengthShouldWrapper[T <:{ val getLength: Long}](o: T): LengthShouldWrapper[T] = new LengthShouldWrapper[T](o)
  implicit def convertHasLongLengthFieldToLengthShouldWrapper[T <:{ val length: Long}](o: T): LengthShouldWrapper[T] = new LengthShouldWrapper[T](o)
  implicit def convertHasLongLengthMethodToLengthShouldWrapper[T <:{ def length(): Long}](o: T): LengthShouldWrapper[T] = new LengthShouldWrapper[T](o)

  implicit def convertHasIntGetSizeMethodToSizeShouldWrapper[T <:{ def getSize(): Int}](o: T): SizeShouldWrapper[T] = new SizeShouldWrapper[T](o)
  implicit def convertHasIntGetSizeFieldToSizeShouldWrapper[T <:{ val getSize: Int}](o: T): SizeShouldWrapper[T] = new SizeShouldWrapper[T](o)
  implicit def convertHasIntSizeFieldToSizeShouldWrapper[T <:{ val size: Int}](o: T): SizeShouldWrapper[T] = new SizeShouldWrapper[T](o)
  implicit def convertHasIntSizeMethodToSizeShouldWrapper[T <:{ def size(): Int}](o: T): SizeShouldWrapper[T] = new SizeShouldWrapper[T](o)

  implicit def convertHasLongGetSizeMethodToSizeShouldWrapper[T <:{ def getSize(): Long}](o: T): SizeShouldWrapper[T] = new SizeShouldWrapper[T](o)
  implicit def convertHasLongGetSizeFieldToSizeShouldWrapper[T <:{ val getSize: Long}](o: T): SizeShouldWrapper[T] = new SizeShouldWrapper[T](o)
  implicit def convertHasLongSizeFieldToSizeShouldWrapper[T <:{ val size: Long}](o: T): SizeShouldWrapper[T] = new SizeShouldWrapper[T](o)
  implicit def convertHasLongSizeMethodToSizeShouldWrapper[T <:{ def size(): Long}](o: T): SizeShouldWrapper[T] = new SizeShouldWrapper[T](o)
}
/*
When Scala must chose between an implicit with a structural type and one with a nominal one,
the nominal one wins.

scala> set.size
res0: Int = 3

scala> class SetWrapper(payload: Set[Int]) { def prove() { println("SetWrapper") }}
defined class SetWrapper

scala> class SizeWrapper(payload: { def size: Int }) { def prove() { println("SizeWrapper") }}
defined class SizeWrapper

scala> new SizeWrapper(set)
res1: SizeWrapper = SizeWrapper@39ce9b

scala> res1.prove
SizeWrapper

scala> new SetWrapper(set)
res3: SetWrapper = SetWrapper@9fc9fe

scala> res3.prove
SetWrapper

scala> implicit def convertToSetWrapper(setParam: Set[Int]): SetWrapper = new SetWrapper(setParam)
convertToSetWrapper: (Set[Int])SetWrapper

scala> implicit def convertToSizeWrapper(setParam: { def size: Int }): SizeWrapper = new SizeWrapper(setParam)
convertToSizeWrapper: (AnyRef{def size: Int})SizeWrapper

scala> convertToSetWrapper(set)
res5: SetWrapper = SetWrapper@598095

scala> convertToSizeWrapper(set)
res6: SizeWrapper = SizeWrapper@660ff1

scala> set.prove
SetWrapper
 */