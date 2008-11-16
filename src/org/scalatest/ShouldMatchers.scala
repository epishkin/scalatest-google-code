package org.scalatest

private[scalatest] trait ShouldMatchers extends BaseMatchers {

  protected trait ShouldMethods[T] {
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
    def should(beWord: BeWord): ResultOfBeWord[T] = new ResultOfBeWord(leftOperand, true)
    def shouldNot(beWord: BeWord): ResultOfBeWord[T] = new ResultOfBeWord(leftOperand, false)
/*
    def shouldEqual(rightOperand: Any) {
      if (leftOperand != rightOperand) {
        throw new AssertionError(FailureMessages("didNotEqual", leftOperand, rightOperand))
      }
    }
    def shouldNotEqual(rightOperand: Any) {
      if (leftOperand == rightOperand) {
        throw new AssertionError(FailureMessages("equaled", leftOperand, rightOperand))
      }
    }
*/
    def shouldMatch(rightOperand: PartialFunction[T, Boolean]) {
      if (rightOperand.isDefinedAt(leftOperand)) {
        val result = rightOperand(leftOperand)
        if (!result) {
          throw new AssertionError(FailureMessages("matchResultedInFalse", leftOperand))
        }
      }
      else {
        throw new AssertionError(FailureMessages("didNotMatch", leftOperand))
      }
    }
    def shouldNotMatch(rightOperand: PartialFunction[T, Boolean]) {
      if (rightOperand.isDefinedAt(leftOperand)) {
        val result = rightOperand(leftOperand)
        if (result) {
          throw new AssertionError(FailureMessages("matchResultedInTrue", leftOperand))
        }
      }
    }
  }

  protected class ShouldalizerForBlocks(left: => Any) {
    def shouldThrow[T <: AnyRef](clazz: java.lang.Class[T]): T = { intercept(clazz)(left) }
    def shouldNotThrow(clazz: java.lang.Class[Throwable]) {
      try {
        left
      }
      catch {
        case u: Throwable =>
          val message = FailureMessages("anException", UnquotedString(u.getClass.getName))
          val ae = new AssertionError(message)
          ae.initCause(u)
          throw ae
      }
    }
  }

  protected class Shouldalizer[T](left: T) extends { val leftOperand = left } with ShouldMethods[T]

  protected class StringShouldalizer(left: String) extends { val leftOperand = left } with ShouldMethods[String] {
    def should(haveWord: HaveWord): ResultOfHaveWordForString = {
      new ResultOfHaveWordForString(left, true)
    }
    def shouldNot(haveWord: HaveWord): ResultOfHaveWordForString = {
      new ResultOfHaveWordForString(left, false)
    }
    def should(includeWord: IncludeWord): ResultOfIncludeWordForString = {
      new ResultOfIncludeWordForString(left, true)
    }
    def shouldNot(includeWord: IncludeWord): ResultOfIncludeWordForString = {
      new ResultOfIncludeWordForString(left, false)
    }
    def should(startWithWord: StartWithWord): ResultOfStartWithWordForString = {
      new ResultOfStartWithWordForString(left, true)
    }
    def shouldNot(startWithWord: StartWithWord): ResultOfStartWithWordForString = {
      new ResultOfStartWithWordForString(left, false)
    }
    def should(endWithWord: EndWithWord): ResultOfEndWithWordForString = {
      new ResultOfEndWithWordForString(left, true)
    }
    def shouldNot(endWithWord: EndWithWord): ResultOfEndWithWordForString = {
      new ResultOfEndWithWordForString(left, false)
    }
    def should(fullyMatchWord: FullyMatchWord): ResultOfFullyMatchWordForString = {
      new ResultOfFullyMatchWordForString(left, true)
    }
    def shouldNot(fullyMatchWord: FullyMatchWord): ResultOfFullyMatchWordForString = {
      new ResultOfFullyMatchWordForString(left, false)
    }
  }

  protected class MapShouldalizer[K, V](left: Map[K, V]) extends { val leftOperand = left } with ShouldMethods[Map[K, V]] {
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
  
  protected trait ShouldContainWordForIterableMethods[T] {
    protected val leftOperand: Iterable[T]
    def should(containWord: ContainWord): ResultOfContainWordForIterable[T] = {
      new ResultOfContainWordForIterable(leftOperand, true)
    }
    def shouldNot(containWord: ContainWord): ResultOfContainWordForIterable[T] = {
      new ResultOfContainWordForIterable(leftOperand, false)
    }
  }
  
  protected class IterableShouldalizer[T](left: Iterable[T]) extends { val leftOperand = left } with ShouldMethods[Iterable[T]]
      with ShouldContainWordForIterableMethods[T]
  
  protected trait ShouldHaveWordForCollectionMethods[T] {
    protected val leftOperand: Collection[T]
    def should(haveWord: HaveWord): ResultOfHaveWordForCollection[T] = {
      new ResultOfHaveWordForCollection(leftOperand, true)
    }
    def shouldNot(haveWord: HaveWord): ResultOfHaveWordForCollection[T] = {
      new ResultOfHaveWordForCollection(leftOperand, false)
    }
  }
  
  protected trait ShouldHaveWordForSeqMethods[T] {
    protected val leftOperand: Seq[T]
    def should(haveWord: HaveWord): ResultOfHaveWordForSeq[T] = {
      new ResultOfHaveWordForSeq(leftOperand, true)
    }
    def shouldNot(haveWord: HaveWord): ResultOfHaveWordForSeq[T] = {
      new ResultOfHaveWordForSeq(leftOperand, false)
    }
  }
  
  protected class CollectionShouldalizer[T](left: Collection[T]) extends { val leftOperand = left } with ShouldMethods[Collection[T]]
      with ShouldContainWordForIterableMethods[T] with ShouldHaveWordForCollectionMethods[T]
  
  protected class SeqShouldalizer[T](left: Seq[T]) extends { val leftOperand = left } with ShouldMethods[Seq[T]]
      with ShouldContainWordForIterableMethods[T] with ShouldHaveWordForSeqMethods[T]
  
  protected class ArrayShouldalizer[T](left: Array[T]) extends { val leftOperand = left } with ShouldMethods[Array[T]]
      with ShouldContainWordForIterableMethods[T] with ShouldHaveWordForSeqMethods[T]
  
  protected class ListShouldalizer[T](left: List[T]) extends { val leftOperand = left } with ShouldMethods[List[T]]
      with ShouldContainWordForIterableMethods[T] with ShouldHaveWordForSeqMethods[T]
  
  implicit def shouldify[T](o: T): Shouldalizer[T] = new Shouldalizer(o)
  implicit def shouldifyForMap[K, V](o: Map[K, V]): MapShouldalizer[K, V] = new MapShouldalizer[K, V](o)
  implicit def shouldifyForCollection[T](o: Collection[T]): CollectionShouldalizer[T] = new CollectionShouldalizer[T](o)
  implicit def shouldifyForSeq[T](o: Seq[T]): SeqShouldalizer[T] = new SeqShouldalizer[T](o)
  implicit def shouldifyForArray[T](o: Array[T]): ArrayShouldalizer[T] = new ArrayShouldalizer[T](o)
  implicit def shouldifyForList[T](o: List[T]): ListShouldalizer[T] = new ListShouldalizer[T](o)
  implicit def shouldifyForString[K, V](o: String): StringShouldalizer = new StringShouldalizer(o)
  implicit def theBlock(f: => Any) = new ShouldalizerForBlocks(f)
}
