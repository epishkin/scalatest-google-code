package org.scalatest

import scala.reflect.Manifest

trait MustMatchers extends BaseMatchers {

  protected trait MustMethods[T] {
    protected val leftOperand: T
    def must(rightMatcher: Matcher[T]) {
      rightMatcher(leftOperand) match {
        case MatcherResult(false, failureMessage, _) => throw new AssertionError(failureMessage)
        case _ => ()
      }
    }
    def mustNot(rightMatcher: Matcher[T]) {
      rightMatcher(leftOperand) match {
        case MatcherResult(true, _, failureMessage) => throw new AssertionError(failureMessage)
        case _ => ()
      }
    }
    // This one supports it must behave like
    def must(behaveWord: BehaveWord) = new Likifier[T](leftOperand)
    def must(beWord: BeWord): ResultOfBeWord[T] = new ResultOfBeWord(leftOperand, true)
    def mustNot(beWord: BeWord): ResultOfBeWord[T] = new ResultOfBeWord(leftOperand, false)
/*
    def mustEqual(rightOperand: Any) {
      if (leftOperand != rightOperand) {
        throw new AssertionError(FailureMessages("didNotEqual", leftOperand, rightOperand))
      }
    }
    def mustNotEqual(rightOperand: Any) {
      if (leftOperand == rightOperand) {
        throw new AssertionError(FailureMessages("equaled", leftOperand, rightOperand))
      }
    }
*/
    def mustMatch(rightOperand: PartialFunction[T, Boolean]) {
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
    def mustNotMatch(rightOperand: PartialFunction[T, Boolean]) {
      if (rightOperand.isDefinedAt(leftOperand)) {
        val result = rightOperand(leftOperand)
        if (result) {
          throw new AssertionError(FailureMessages("matchResultedInTrue", leftOperand))
        }
      }
    }
  }

/*
  protected class MustalizerForBlocks(left: => Any) {
    def mustThrow[T <: AnyRef](implicit manifest: Manifest[T]): T = { intercept(left)(manifest) }
    def mustNotThrow(clazz: java.lang.Class[Throwable]) { 
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
*/

  protected class Mustalizer[T](left: T) extends { val leftOperand = left } with MustMethods[T]

  protected class StringMustalizer(left: String) extends { val leftOperand = left } with MustMethods[String] {
    def must(haveWord: HaveWord): ResultOfHaveWordForString = {
      new ResultOfHaveWordForString(left, true)
    }
    def mustNot(haveWord: HaveWord): ResultOfHaveWordForString = {
      new ResultOfHaveWordForString(left, false)
    }
    def must(includeWord: IncludeWord): ResultOfIncludeWordForString = {
      new ResultOfIncludeWordForString(left, true)
    }
    def mustNot(includeWord: IncludeWord): ResultOfIncludeWordForString = {
      new ResultOfIncludeWordForString(left, false)
    }
    def must(startWithWord: StartWithWord): ResultOfStartWithWordForString = {
      new ResultOfStartWithWordForString(left, true)
    }
    def mustNot(startWithWord: StartWithWord): ResultOfStartWithWordForString = {
      new ResultOfStartWithWordForString(left, false)
    }
    def must(endWithWord: EndWithWord): ResultOfEndWithWordForString = {
      new ResultOfEndWithWordForString(left, true)
    }
    def mustNot(endWithWord: EndWithWord): ResultOfEndWithWordForString = {
      new ResultOfEndWithWordForString(left, false)
    }
    def must(fullyMatchWord: FullyMatchWord): ResultOfFullyMatchWordForString = {
      new ResultOfFullyMatchWordForString(left, true)
    }
    def mustNot(fullyMatchWord: FullyMatchWord): ResultOfFullyMatchWordForString = {
      new ResultOfFullyMatchWordForString(left, false)
    }
  }

  protected class MapMustalizer[K, V](left: Map[K, V]) extends { val leftOperand = left } with MustMethods[Map[K, V]] {
    def must(containWord: ContainWord): ResultOfContainWordForIterable[(K, V)] = {
      new ResultOfContainWordForIterable(left, true)
    }
    def mustNot(containWord: ContainWord): ResultOfContainWordForIterable[(K, V)] = {
      new ResultOfContainWordForIterable(left, false)
    }
    def must(haveWord: HaveWord): ResultOfHaveWordForMap[K, V] = {
      new ResultOfHaveWordForMap(left, true)
    }
    def mustNot(haveWord: HaveWord): ResultOfHaveWordForMap[K, V] = {
      new ResultOfHaveWordForMap(left, false)
    }
  }
  
  protected trait MustContainWordForIterableMethods[T] {
    protected val leftOperand: Iterable[T]
    def must(containWord: ContainWord): ResultOfContainWordForIterable[T] = {
      new ResultOfContainWordForIterable(leftOperand, true)
    }
    def mustNot(containWord: ContainWord): ResultOfContainWordForIterable[T] = {
      new ResultOfContainWordForIterable(leftOperand, false)
    }
  }
  
  protected class IterableMustalizer[T](left: Iterable[T]) extends { val leftOperand = left } with MustMethods[Iterable[T]]
      with MustContainWordForIterableMethods[T]
  
  protected trait MustHaveWordForCollectionMethods[T] {
    protected val leftOperand: Collection[T]
    def must(haveWord: HaveWord): ResultOfHaveWordForCollection[T] = {
      new ResultOfHaveWordForCollection(leftOperand, true)
    }
    def mustNot(haveWord: HaveWord): ResultOfHaveWordForCollection[T] = {
      new ResultOfHaveWordForCollection(leftOperand, false)
    }
  }
  
  protected trait MustHaveWordForSeqMethods[T] {
    protected val leftOperand: Seq[T]
    def must(haveWord: HaveWord): ResultOfHaveWordForSeq[T] = {
      new ResultOfHaveWordForSeq(leftOperand, true)
    }
    def mustNot(haveWord: HaveWord): ResultOfHaveWordForSeq[T] = {
      new ResultOfHaveWordForSeq(leftOperand, false)
    }
  }
  
  protected class CollectionMustalizer[T](left: Collection[T]) extends { val leftOperand = left } with MustMethods[Collection[T]]
      with MustContainWordForIterableMethods[T] with MustHaveWordForCollectionMethods[T]
  
  protected class SeqMustalizer[T](left: Seq[T]) extends { val leftOperand = left } with MustMethods[Seq[T]]
      with MustContainWordForIterableMethods[T] with MustHaveWordForSeqMethods[T]
  
  protected class ArrayMustalizer[T](left: Array[T]) extends { val leftOperand = left } with MustMethods[Array[T]]
      with MustContainWordForIterableMethods[T] with MustHaveWordForSeqMethods[T]
  
  protected class ListMustalizer[T](left: List[T]) extends { val leftOperand = left } with MustMethods[List[T]]
      with MustContainWordForIterableMethods[T] with MustHaveWordForSeqMethods[T]
  
  implicit def mustify[T](o: T): Mustalizer[T] = new Mustalizer(o)
  implicit def mustifyForMap[K, V](o: Map[K, V]): MapMustalizer[K, V] = new MapMustalizer[K, V](o)
  implicit def mustifyForCollection[T](o: Collection[T]): CollectionMustalizer[T] = new CollectionMustalizer[T](o)
  implicit def mustifyForSeq[T](o: Seq[T]): SeqMustalizer[T] = new SeqMustalizer[T](o)
  implicit def mustifyForArray[T](o: Array[T]): ArrayMustalizer[T] = new ArrayMustalizer[T](o)
  implicit def mustifyForList[T](o: List[T]): ListMustalizer[T] = new ListMustalizer[T](o)
  implicit def mustifyForString[K, V](o: String): StringMustalizer = new StringMustalizer(o)
  // implicit def theBlock(f: => Any) = new MustalizerForBlocks(f)
}
