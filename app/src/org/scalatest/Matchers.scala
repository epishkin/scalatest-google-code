package org.scalatest

import java.lang.reflect.Method
import java.lang.reflect.Modifier

private[scalatest] case class MatcherResult(
  matches: Boolean,
  failureMessage: String,
  negativeFailureMessage: String
)

/*
There are a set of implicit conversions that take different static types to Shouldalizers.
The one that gets applied will be the one that matches the static type of left. The result
of the implicit conversion will be a Shouldalizer.

The should methods take different static types, so they are overloaded. These types don't all
inherit from the same supertype. There's a plain-old Matcher for example, but there's also maybe
a BeMatcher, and BeMatcher doesn't extend Matcher. This reduces the number of incorrect static
matches, which can happen if a more specific type is held from a more general variable type.
And reduces the chances for ambiguity, I suspect.

On my jog I thought perhaps that Matcher should be contravariant in T, because
if I have hierarchy Fruit <-- Orange <-- ValenciaOrange, and I have:

val orange = Orange

"orange should" will give me a Shouldalizer[Orange], which has an apply method that takes a Matcher[Orange].
If I have a Matcher[ValenciaOrange], that shouldn't compile, but if I have a Matcher[Fruit], it should compile.
Thus I should be able to pass a Matcher[Fruit] to a should method that expects a Matcher[Orange], which is
contravariance. Then the type of the "left" parameter of the apply method can just be T, because in the case
of Matcher[Fruit], for example, T is Fruit, and you can pass an Orange to an apply method that expects a Fruit.

So it should be:

trait Matcher[-T] { leftMatcher => ...

Yay, that worked, so long as I do the upper bound thing in add. All makes sense. If I do
matcherOfOrange and matcherOfValencia, then the type of the resulting matcher needs to be
matcherOfValencia. But if I do "matcherOfOrange and matcherOfFruit", the type stays at
matcherOfOrange. And the right operand is considered a matcher of orange, because of contravariance.

Made it extend Function1 for the heck of it. Can pass it as a Function1 now.
*/

private[scalatest] object Helper {
  def not[S <: Any](matcher: Matcher[S]) =
    new Matcher[S] {
      def apply(left: S) =
        matcher(left) match {
          case MatcherResult(bool, s1, s2) => MatcherResult(!bool, s2, s1)
        }
    }

  def equalAndBeAnyMatcher(right: Any, equaledResourceName: String, didNotEqualResourceName: String) = {

      def toStringElseNull(o: Any) = if (o != null) o.toString else "null"

      new Matcher[Any] {
        def apply(left: Any) =
          left match {
            case leftArray: Array[_] => 
              MatcherResult(
                leftArray.deepEquals(right),
                Resources(didNotEqualResourceName, left.toString, right.toString),
                Resources(equaledResourceName, left.toString, right.toString)
              )
            case _ => 
              MatcherResult(
                left == right,
                Resources(didNotEqualResourceName, toStringElseNull(left), toStringElseNull(right)),
                Resources(equaledResourceName, toStringElseNull(left), toStringElseNull(right))
/*
                Resources(didNotEqualResourceName, toStringOrElse(left) if (left != null) left.toString else "null", if (right != null) right.toString else "null"),
                Resources(equaledResourceName, if (left != null) left.toString else "null", if (right != null) right.toString else "null")
*/
              )
        }
      }
  }
}

private[scalatest] trait Matcher[-T] extends Function1[T, MatcherResult] { leftMatcher =>

  // left is generally the object on which should is invoked.
  def apply(left: T): MatcherResult

  // left is generally the object on which should is invoked. leftMatcher
  // is the left operand to and. For example, in:
  // cat should { haveLives (9) and landOn (feet) }
  // left is 'cat' and leftMatcher is the matcher produced by 'haveLives (9)'.
  // rightMatcher, by the way, is the matcher produced by 'landOn (feet)'
  def and[U <: T](rightMatcher: => Matcher[U]): Matcher[U] =
    new Matcher[U] {
      def apply(left: U) = {
        val leftMatcherResult = leftMatcher(left)
        if (!leftMatcherResult.matches)
          MatcherResult(
            false,
            leftMatcherResult.failureMessage,
            leftMatcherResult.negativeFailureMessage
          )
        else {
          val rightMatcherResult = rightMatcher(left)
          MatcherResult(
            rightMatcherResult.matches,
            Resources("commaBut", leftMatcherResult.negativeFailureMessage, rightMatcherResult.failureMessage),
            Resources("commaBut", leftMatcherResult.negativeFailureMessage, rightMatcherResult.negativeFailureMessage)
          )
        }
      }
    }

  def andNot[U <: T](rightMatcher: => Matcher[U]): Matcher[U] = leftMatcher and Helper.not { rightMatcher }

  def or[U <: T](rightMatcher: => Matcher[U]): Matcher[U] =
    new Matcher[U] {
      def apply(left: U) = {
        val leftMatcherResult = leftMatcher(left)
        if (leftMatcherResult.matches)
          MatcherResult(
            true,
            leftMatcherResult.negativeFailureMessage,
            leftMatcherResult.failureMessage
          )
        else {
          val rightMatcherResult = rightMatcher(left)
          MatcherResult(
            rightMatcherResult.matches,
            Resources("commaAnd", leftMatcherResult.failureMessage, rightMatcherResult.failureMessage),
            Resources("commaAnd", leftMatcherResult.failureMessage, rightMatcherResult.negativeFailureMessage)
          )
        }
      }
    }

  def orNot[U <: T](rightMatcher: => Matcher[U]): Matcher[U] = leftMatcher or Helper.not { rightMatcher }
}

private[scalatest] trait Matchers extends Assertions {

  //
  // This class is used as the return type of the overloaded should method (in MapShouldalizer)
  // that takes a HaveWord. It's key method will be called in situations like this:
  //
  // map should have key 1
  //
  // This gets changed to :
  //
  // shouldifyForMap(map).should(have).key(1)
  //
  // Thus, the map is wrapped in a shouldifyForMap call via an implicit conversion, which results in 
  // a MapShouldalizer. This has a should method that takes a HaveWord. That method returns a
  // ResultOfHaveWordPassedToShould that remembers the map to the left of should. Then this class
  // ha a key method that takes a K type, they key type of the map. It does the assertion thing.
  // 
  private[scalatest] class ResultOfHaveWordForMap[K, V](left: Map[K, V], shouldBeTrue: Boolean) extends ResultOfHaveWordForCollection[Tuple2[K, V]](left, shouldBeTrue) {
    def key(expectedKey: K) =
      if (left.contains(expectedKey) != shouldBeTrue)
        throw new AssertionError(
          Resources(
            if (shouldBeTrue) "didNotHaveKey" else "hadKey",
            left.toString,
            expectedKey.toString)
        )
    def value(expectedValue: V) =
      if (left.values.contains(expectedValue) != shouldBeTrue)
        throw new AssertionError(
          Resources(
            if (shouldBeTrue) "didNotHaveValue" else "hadValue",
            left.toString,
            expectedValue.toString)
        )
  /*
    def size(expectedSize: Int) =
      if ((left.size == expectedSize) != shouldBeTrue)
        throw new AssertionError(
          Resources(
            if (shouldBeTrue) "didNotHaveExpectedSize" else "hadExpectedSize",
            left.toString,
            expectedSize.toString)
        )
  */
  }
  
  private[scalatest] trait ShouldMethods[T] {
    protected val leftOperand: T
    def should(rightMatcher: Matcher[T]) {
      rightMatcher(leftOperand) match {
        case MatcherResult(false, failureMessage, _) => throw new AssertionError(failureMessage)
        case _ => ()
      }
    }
    def shouldNot(rightMatcher: Matcher[T]) {
      rightMatcher(leftOperand) match {
        case MatcherResult(true, _, failureMessage) => throw new AssertionError(failureMessage)
        case _ => ()
      }
    }
    // This one supports it should behave like
    def should(behaveWord: BehaveWord) = new Likifier[T](leftOperand)
    def should(beWord: BeWord): ResultOfBeWord = new ResultOfBeWord(leftOperand, true)
    def shouldNot(beWord: BeWord): ResultOfBeWord = new ResultOfBeWord(leftOperand, false)
    def shouldEqual(rightOperand: Any) { assert(leftOperand === rightOperand) }
    def shouldNotEqual(rightOperand: Any) { assert(leftOperand !== rightOperand) }
  }

  private[scalatest] class Shouldalizer[T](left: T) extends { val leftOperand = left } with ShouldMethods[T]

  private[scalatest] class StringShouldalizer(left: String) extends { val leftOperand = left } with ShouldMethods[String] {
    def should(haveWord: HaveWord): ResultOfHaveWordForString = {
      new ResultOfHaveWordForString(left, true)
    }
    def shouldNot(haveWord: HaveWord): ResultOfHaveWordForString = {
      new ResultOfHaveWordForString(left, false)
    }
  }
  
  private[scalatest] class BehaveWord
  private[scalatest] class ContainWord
  
  private[scalatest] class HaveWord {
    //
    // This key method is called when "have" is used in a logical expression, such as:
    // map should { have key 1 and equal (Map(1 -> "Howdy")) }. It results in a matcher
    // that remembers the key value. By making the value type Any, it causes overloaded shoulds
    // to work, because for example a Matcher[Map[Int, Any]] is a subtype of Matcher[Map[Int, String]],
    // given Map is covariant in its V (the value type stored in the map) parameter and Matcher is
    // contravariant in its lone type parameter. Thus, the type of the Matcher resulting from have key 1
    // is a subtype of the map type that has a known value type parameter because its that of the map
    // to the left of should. This means the should method that takes a map will be selected by Scala's
    // method overloading rules.
    // 
    def key[K](expectedKey: K): Matcher[Map[K, Any]] =
      new Matcher[Map[K, Any]] {
        def apply(left: Map[K, Any]) =
          MatcherResult(
            left.contains(expectedKey), 
            Resources("didNotHaveKey", left.toString, expectedKey.toString),
            Resources("hadKey", left.toString, expectedKey.toString)
          )
      }
  
    // Holy smokes I'm starting to scare myself. I fixed the problem of the compiler not being
    // able to infer the value type in  have value 1 and ... like expressions, because the
    // value type is there, with an existential type. Since I don't know what K is, I decided to
    // try just saying that with an existential type, and it compiled and ran. Pretty darned
    // amazing compiler. The problem could not be fixed like I fixed the key method above, because
    // Maps are nonvariant in their key type parameter, whereas they are covariant in their value
    // type parameter, so the same trick wouldn't work. But this existential type trick seems to
    // work like a charm.
    def value[V](expectedValue: V): Matcher[Map[K, V] forSome { type K }] =
      new Matcher[Map[K, V] forSome { type K }] {
        def apply(left: Map[K, V] forSome { type K }) =
          MatcherResult(
            left.values.contains(expectedValue), 
            Resources("didNotHaveValue", left.toString, expectedValue.toString),
            Resources("hadValue", left.toString, expectedValue.toString)
          )
      }
  
    def size(expectedSize: Int) =
      new Matcher[Collection[Any]] {
        def apply(left: Collection[Any]) =
          MatcherResult(
            left.size == expectedSize, 
            Resources("didNotHaveValue", left.toString, expectedSize.toString),
            Resources("hadValue", left.toString, expectedSize.toString)
          )
      }
  /*
    // Go ahead and use a structural type here too, to make it more general. Can then
    // use this on any type that has a size method. I guess it doesn't matter in structural
    // types if you put the empty parens on there or not.
    def size(expectedSize: Int) =
      new Matcher[{ def size(): Int }] {
        def apply(left: { def size(): Int }) =
          MatcherResult(
            left.size == expectedSize, 
            Resources("didNotHaveExpectedSize", left.toString, expectedSize.toString),
            Resources("hadExpectedSize", left.toString, expectedSize.toString)
          )
      }
  */
  
  /*
    // This should give me { def length(): Int } I don't
    // know the type, but it has a length method. This would work on strings and ints, but
    // I"m not sure what the story is on the parameterless or not. Probably should put parens in there.
    // String is a structural subtype of { def length(): Int }. Thus Matcher[{ def length(): Int }] should
    // be a subtype of Matcher[String], because of contravariance. Yeah, this worked! XXX
    // Darn structural type won't work for both arrays and strings, because one requres a length and the other a length()
    // So they aren't the same structural type. Really want the syntax, so moving to reflection and a runtime error.
    def length(expectedLength: Int) =
      new Matcher[{ def length: Int }] {
        def apply(left: { def length: Int }) =
          MatcherResult(
            left.length == expectedLength, 
            Resources("didNotHaveExpectedLength", left.toString, expectedLength.toString),
            Resources("hadExpectedLength", left.toString, expectedLength.toString)
          )
      }
  */
    def length(expectedLength: Int) =
      new Matcher[AnyRef] {
        def apply(left: AnyRef) =
          left match {
            case leftSeq: Seq[_] =>
              MatcherResult(
                leftSeq.length == expectedLength, 
                Resources("didNotHaveExpectedLength", left.toString, expectedLength.toString),
                Resources("hadExpectedLength", left.toString, expectedLength.toString)
              )
            case leftString: String =>
              MatcherResult(
                leftString.length == expectedLength, 
                Resources("didNotHaveExpectedLength", left.toString, expectedLength.toString),
                Resources("hadExpectedLength", left.toString, expectedLength.toString)
              )
            case _ =>
              val methods = left.getClass.getMethods
              val methodOption = methods.find(_.getName == "length")
              val hasLengthMethod =
                methodOption match {
                  case Some(method) =>
                    method.getParameterTypes.length == 0
                  case None => false
                }
              val fields = left.getClass.getFields
              val fieldOption = fields.find(_.getName == "length")
              val hasLengthField =
                fieldOption match {
                  case Some(_) => true
                  case None => false
                }
              if (hasLengthMethod) {
                MatcherResult(
                  methodOption.get.invoke(left, Array[Object]()) == expectedLength, 
                  Resources("didNotHaveExpectedLength", left.toString, expectedLength.toString),
                  Resources("hadExpectedLength", left.toString, expectedLength.toString)
                )
              }
              else if (hasLengthField) {
                MatcherResult(
                  fieldOption.get.get(left) == expectedLength, 
                  Resources("didNotHaveExpectedLength", left.toString, expectedLength.toString),
                  Resources("hadExpectedLength", left.toString, expectedLength.toString)
                )
              }
              else {
                throw new AssertionError("'have length "+ expectedLength +"' used with an object that had neither a public field or method named 'length'.")
              }
        }
      }
  }
  
  private[scalatest] class MapShouldalizer[K, V](left: Map[K, V]) extends { val leftOperand = left } with ShouldMethods[Map[K, V]] {
    def should(containWord: ContainWord): ResultOfContainWordForIterable[(K, V)] = {
      new ResultOfContainWordForIterable(left, true)
    }
    def shouldNot(containWord: ContainWord): ResultOfContainWordForIterable[(K, V)] = {
      new ResultOfContainWordForIterable(left, false)
    }
    def should(haveWord: HaveWord): ResultOfHaveWordForMap[K, V] = {
      new ResultOfHaveWordForMap(left, true)
    }
    def shouldNot(haveWord: HaveWord): ResultOfHaveWordForMap[K, V] = {
      new ResultOfHaveWordForMap(left, false)
    }
  }
  
  //
  // This class is used as the return type of the overloaded should method (in CollectionShouldalizer)
  // that takes a HaveWord. It's size method will be called in situations like this:
  //
  // list should have size 1
  //
  // This gets changed to :
  //
  // shouldifyForCollection(list).should(have).size(1)
  //
  // Thus, the list is wrapped in a shouldifyForCollection call via an implicit conversion, which results in 
  // a CollectionShouldalizer. This has a should method that takes a HaveWord. That method returns a
  // ResultOfHaveWordForCollectionPassedToShould that remembers the map to the left of should. Then this class
  // has a size method that takes a T type, type parameter of the iterable. It does the assertion thing.
  // 
  private[scalatest] class ResultOfHaveWordForCollection[T](left: Collection[T], shouldBeTrue: Boolean) {
    def size(expectedSize: Int) =
      if ((left.size == expectedSize) != shouldBeTrue)
        throw new AssertionError(
          Resources(
            if (shouldBeTrue) "didNotHaveExpectedSize" else "hadExpectedSize",
            left.toString,
            expectedSize.toString)
        )
  }
  
  private[scalatest] class ResultOfHaveWordForSeq[T](left: Seq[T], shouldBeTrue: Boolean) extends ResultOfHaveWordForCollection[T](left, shouldBeTrue) {
    def length(expectedLength: Int) =
      if ((left.length == expectedLength) != shouldBeTrue)
        throw new AssertionError(
          Resources(
            if (shouldBeTrue) "didNotHaveExpectedLength" else "hadExpectedLength",
            left.toString,
            expectedLength.toString)
        )
  }
  
  // New Malaysia 48 bowery
  private[scalatest] class ResultOfBeWord(left: Any, shouldBeTrue: Boolean) {
    def a[S <: AnyRef](right: Symbol): Matcher[S] = be(right)
    def an[S <: AnyRef](right: Symbol): Matcher[S] = be(right)
    def anInstanceOf[T <: AnyRef](clazz: Class[T]) { 
      left match {
      case leftRef: AnyRef =>
        if (clazz.isAssignableFrom(leftRef.getClass) != shouldBeTrue) {
          throw new AssertionError(
            Resources(
              if (shouldBeTrue) "wasNotAnInstanceOf" else "wasAnInstanceOf",
              leftRef.toString,
              "the specified type"
            )
          )
        }
      case _: AnyVal => throw new AssertionError("NOT SUPPORTED YET")
      }
    }
  }

  private[scalatest] class ResultOfHaveWordForString(left: String, shouldBeTrue: Boolean) {
    def length(expectedLength: Int) =
      if ((left.length == expectedLength) != shouldBeTrue)
        throw new AssertionError(
          Resources(
            if (shouldBeTrue) "didNotHaveExpectedLength" else "hadExpectedLength",
            left.toString,
            expectedLength.toString)
        )
  }
  
  private[scalatest] class ResultOfContainWordForIterable[T](left: Iterable[T], shouldBeTrue: Boolean) {
    def element(expectedElement: T) =
      if ((left.elements.contains(expectedElement)) != shouldBeTrue)
        throw new AssertionError(
          Resources(
            if (shouldBeTrue) "didNotContainExpectedElement" else "containedExpectedElement",
            left.toString,
            expectedElement.toString)
        )
  }
  
  private[scalatest] trait ShouldContainWordForIterableMethods[T] {
    protected val leftOperand: Iterable[T]
    def should(containWord: ContainWord): ResultOfContainWordForIterable[T] = {
      new ResultOfContainWordForIterable(leftOperand, true)
    }
    def shouldNot(containWord: ContainWord): ResultOfContainWordForIterable[T] = {
      new ResultOfContainWordForIterable(leftOperand, false)
    }
  }
  
  private[scalatest] class IterableShouldalizer[T](left: Iterable[T]) extends { val leftOperand = left } with ShouldMethods[Iterable[T]]
      with ShouldContainWordForIterableMethods[T]
  
  private[scalatest] trait ShouldHaveWordForCollectionMethods[T] {
    protected val leftOperand: Collection[T]
    def should(haveWord: HaveWord): ResultOfHaveWordForCollection[T] = {
      new ResultOfHaveWordForCollection(leftOperand, true)
    }
    def shouldNot(haveWord: HaveWord): ResultOfHaveWordForCollection[T] = {
      new ResultOfHaveWordForCollection(leftOperand, false)
    }
  }
  
  private[scalatest] trait ShouldHaveWordForSeqMethods[T] {
    protected val leftOperand: Seq[T]
    def should(haveWord: HaveWord): ResultOfHaveWordForSeq[T] = {
      new ResultOfHaveWordForSeq(leftOperand, true)
    }
    def shouldNot(haveWord: HaveWord): ResultOfHaveWordForSeq[T] = {
      new ResultOfHaveWordForSeq(leftOperand, false)
    }
  }
  
  private[scalatest] class CollectionShouldalizer[T](left: Collection[T]) extends { val leftOperand = left } with ShouldMethods[Collection[T]]
      with ShouldContainWordForIterableMethods[T] with ShouldHaveWordForCollectionMethods[T]
  
  private[scalatest] class SeqShouldalizer[T](left: Seq[T]) extends { val leftOperand = left } with ShouldMethods[Seq[T]]
      with ShouldContainWordForIterableMethods[T] with ShouldHaveWordForSeqMethods[T]
  
  private[scalatest] class ArrayShouldalizer[T](left: Array[T]) extends { val leftOperand = left } with ShouldMethods[Array[T]]
      with ShouldContainWordForIterableMethods[T] with ShouldHaveWordForSeqMethods[T]
  
  private[scalatest] class ListShouldalizer[T](left: List[T]) extends { val leftOperand = left } with ShouldMethods[List[T]]
      with ShouldContainWordForIterableMethods[T] with ShouldHaveWordForSeqMethods[T]
  
  implicit def shouldify[T](o: T): Shouldalizer[T] = new Shouldalizer(o)
  implicit def shouldifyForMap[K, V](o: Map[K, V]): MapShouldalizer[K, V] = new MapShouldalizer[K, V](o)
  implicit def shouldifyForCollection[T](o: Collection[T]): CollectionShouldalizer[T] = new CollectionShouldalizer[T](o)
  implicit def shouldifyForSeq[T](o: Seq[T]): SeqShouldalizer[T] = new SeqShouldalizer[T](o)
  implicit def shouldifyForArray[T](o: Array[T]): ArrayShouldalizer[T] = new ArrayShouldalizer[T](o)
  implicit def shouldifyForList[T](o: List[T]): ListShouldalizer[T] = new ListShouldalizer[T](o)
  implicit def shouldifyForString[K, V](o: String): StringShouldalizer = new StringShouldalizer(o)
  implicit def stringToHasLength(s: AnyRef with String): { def length: Int } = new { def length: Int = s.length() }

  def equal(right: Any): Matcher[Any] =
    Helper.equalAndBeAnyMatcher(right, "equaled", "didNotEqual")
/*
    new Matcher[Any] {
      def apply(left: Any) =
        left match {
          case leftArray: Array[_] => 
            MatcherResult(
              leftArray.deepEquals(right),
              Resources("didNotEqual", left.toString, right.toString),
              Resources("equaled", left.toString, right.toString)
            )
          case _ => 
            MatcherResult(
              left == right,
              Resources("didNotEqual", if (left != null) left.toString else "null", if (right != null) right.toString else "null"),
              Resources("equaled", if (left != null) left.toString else "null", if (right != null) right.toString else "null")
            )
      }
    }
*/

  private[scalatest] class BeWord {

    // These two are used if this shows up in a "x should { be a 'file and ..." type clause
    def a[S <: AnyRef](right: Symbol): Matcher[S] = apply(right)
    def an[S <: AnyRef](right: Symbol): Matcher[S] = apply(right)

    def anInstanceOf[T <: AnyRef](clazz: Class[T]): Matcher[AnyRef] = 
      new Matcher[AnyRef] {
        def apply(left: AnyRef) =
          MatcherResult(
            clazz.isAssignableFrom(left.getClass),
            Resources("wasNotAnInstanceOf", left.toString, "the specified type"),
            Resources("wasAnInstanceOf", left.toString, "the specified type")
          )
      }

    def apply(right: Boolean) = 
      new Matcher[Boolean] {
        def apply(left: Boolean) =
          MatcherResult(
            left == right,
            Resources("wasNot", left.toString, right.toString),
            Resources("was", left.toString, right.toString)
          )
      }

    def apply(o: Null) = 
      new Matcher[AnyRef] {
        def apply(left: AnyRef) = {
          MatcherResult(
            left == null,
            Resources("wasNotNull", left),
            Resources("wasNull", left)
          )
        }
      }

    def apply(o: None.type) = 
      new Matcher[Option[_]] {
        def apply(left: Option[_]) = {
          MatcherResult(
            left == None,
            Resources("wasNotNone", left),
            Resources("wasNone", left)
          )
        }
      }
  
    def apply[S <: AnyRef](right: Symbol): Matcher[S] = {
  
      def matcherUsingReflection(left: S): MatcherResult = {

        // If 'empty passed, rightNoTick would be "empty"
        val rightNoTick = right.toString.substring(1)

        // methodNameToInvoke would also be "empty"
        val methodNameToInvoke = rightNoTick

        // methodNameToInvokeWithIs would be "isEmpty"
        val methodNameToInvokeWithIs = "is"+ rightNoTick(0).toUpperCase + rightNoTick.substring(1)

        val firstChar = rightNoTick(0).toLowerCase
        val methodNameStartsWithVowel = firstChar == 'a' || firstChar == 'e' || firstChar == 'i' ||
          firstChar == 'o' || firstChar == 'u'

        def isMethodToInvoke(m: Method) = {

          val isInstanceMethod = !Modifier.isStatic(m.getModifiers())
          val simpleName = m.getName
          val paramTypes = m.getParameterTypes
          val hasNoParams = paramTypes.length == 0
          val resultType = m.getReturnType

          isInstanceMethod && hasNoParams &&
          (simpleName == methodNameToInvoke || simpleName == methodNameToInvokeWithIs) &&
          resultType == classOf[Boolean]
        }

        // Store in an array, because may have both isEmpty and empty, in which case I
        // will throw an exception.
        val methodArray =
          for (m <- left.getClass.getMethods; if isMethodToInvoke(m))
            yield m
        
        methodArray.length match {
          case 0 =>
            throw new IllegalArgumentException(
              Resources(
                if (methodNameStartsWithVowel) "hasNeitherAnOrAnMethod" else "hasNeitherAOrAnMethod",
                left,
                methodNameToInvoke,
                methodNameToInvokeWithIs
              )
            )
          case 1 =>
            val result = methodArray(0).invoke(left, Array[AnyRef]()).asInstanceOf[Boolean]
            MatcherResult(
              result,
              Resources("wasNot", left.toString, rightNoTick.toString),
              Resources("was", left.toString, rightNoTick.toString)
            )
          case _ => // Should only ever be 2, but just in case
            throw new IllegalArgumentException(
              Resources(
                if (methodNameStartsWithVowel) "hasBothAnAndAnMethod" else "hasBothAAndAnMethod",
                left,
                methodNameToInvoke,
                methodNameToInvokeWithIs
              )
            )
        }
      }

      new Matcher[S] {
        def apply(left: S) = {
  
          left match {
            case leftString: String => 
              MatcherResult(
                leftString.length == 0,
                Resources("wasNotEmpty", left.toString),
                Resources("wasEmpty", left.toString)
              )
            case _ => matcherUsingReflection(left)
          }
        }
      }
    }

    def apply(right: Nil.type): Matcher[List[_]] = equal(right)

    def apply(right: Any): Matcher[Any] =
      Helper.equalAndBeAnyMatcher(right, "was", "wasNot")
  }

  def not[S <: Any](matcher: Matcher[S]) = Helper.not { matcher }

  def endWith[T <: String](right: T) =
    new Matcher[T] {
      def apply(left: T) =
        MatcherResult(
          left endsWith right,
          Resources("didNotEndWith", left, right),
          Resources("endedWith", left, right)
        )
    }

  def startWith[T <: String](right: T) =
    new Matcher[T] {
      def apply(left: T) =
        MatcherResult(
          left startsWith right,
          Resources("didNotStartWith", left, right),
          Resources("startedWith", left, right)
        )
    }

  val behave = new BehaveWord
  val be = new BeWord

  def importSharedBehavior(behavior: Behavior)

  class Likifier[T](left: T) {
    def like(fun: (T) => Behavior) {
      importSharedBehavior(fun(left))
    }
  }

  def beNil: Matcher[List[_]] = Helper.equalAndBeAnyMatcher(Nil, "was", "wasNot")

  def beNull: Matcher[Any] = Helper.equalAndBeAnyMatcher(null, "was", "wasNot")

  def beEmpty: Matcher[AnyRef] = be.apply('empty)

  def beNone: Matcher[Option[_]] = be.apply(None)

  def beDefined: Matcher[AnyRef] = be.apply('defined)

  def beTrue: Matcher[Boolean] =
    new Matcher[Boolean] {
      def apply(left: Boolean) =
        MatcherResult(
          left,
          Resources("booleanExpressionWasNot", "true"),
          Resources("booleanExpressionWas", "true")
        )
    }

  def beFalse: Matcher[Boolean] =
    new Matcher[Boolean] {
      def apply(left: Boolean) =
        MatcherResult(
          !left,
          Resources("booleanExpressionWasNot", "false"),
          Resources("booleanExpressionWas", "false")
        )
    }

  def beSome[S](payload: S): Matcher[Option[S]] =
      new Matcher[Option[S]] {
        def apply(left: Option[S]) = {
          if (left.isEmpty) 
            MatcherResult(
              false,
              Resources("wasNone", left),
              Resources("wasSome", left)
            )
          else
            MatcherResult(
              left.get == payload,
              Resources("wasSomeWrongValue", left, left.get.toString),
              Resources("wasSomeRightValue", left, left.get.toString)
            )
        }
      }

/*
    In HaveWord's methods key, value, length, and size, I can give type parameters.
    The type HaveWord can contain a key method that takes a S or what not, and returns a matcher, which
    stores the key value in a val and whose apply method checks the passed map for the remembered key. This
    one would be used in things like:

    map should { have key 9 and have value "bob" }

    There's an overloaded should method on Shouldifier that takes a HaveWord. This method results in
    a different type that also has a key method that takes an S. So when you say:

    map should have key 9

    what happens is that this alternate should method gets invoked. The result is this other class that
    has a key method, and its constructor takes the map and stores it in a val. So this time when key is
    invoked, it checks to make sure the passed key is in the remembered map, and does the assertion.

    length and size can probably use structural types, because I want to use length on string and array for
    starters, and other people may create classes that have length methods. Would be nice to be able to use them.
  */
  def have = new HaveWord
  def contain = new ContainWord
}
