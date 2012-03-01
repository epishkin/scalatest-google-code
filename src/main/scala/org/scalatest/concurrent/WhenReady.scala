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
package org.scalatest.concurrent

import org.scalatest._
import org.scalatest.StackDepthExceptionHelper.getStackDepthFun
import java.util.concurrent.{Future => FutureOfJava}
import java.util.concurrent.TimeUnit
import org.scalatest.Suite.anErrorThatShouldCauseAnAbort
import scala.annotation.tailrec

/**
 * Trait that provides the <code>whenReady</code> construct, which periodically queries the passed
 * future, until it is ready or the configured timeout has been surpassed, and when ready, passes the future's
 * value to the specified function.
 *
 * <p>
 * To make <code>whenReady</code> more broadly applicable, the type of future it accepts is a <code>FutureConcept[T]</code>,
 * where <code>T</code> is the type of value promised by the future. Passing a future to <code>whenReady</code> requires
 * an implicit conversion from the type of future you wish to pass (the <em>modeled type</em>) to
 * <code>FutureConcept[T]</code>. <code>WhenReady</code> provides an implicit conversion from
 * <code>java.util.concurrent.Future[T]</code> to <code>org.scalatest.concurrent.FutureConcept[T]</code>.
 * <strong>Another one for Scala actors future is likely forthcoming prior to the 1.8 release.</strong>
 * </p>
 *
 * <p>
 * For example, the following invocation of <code>whenReady</code> would succeed (not throw an exception):
 * </p>
 *
 * <pre class="stHighlight">
 * import org.scalatest._
 * import matchers.ShouldMatchers._
 * import concurrent.WhenReady._
 * import java.util.concurrent._
 * 
 * val exec = Executors.newSingleThreadExecutor
 * val task = new Callable[String] { def call() = { Thread.sleep(500); "hi" } }
 * whenReady(exec.submit(task)) { s =&gt;
 *   s should be ("hi")
 * }
 * </pre>
 *
 * <p>
 * However, because the default timeout is one second, the following invocation of
 * <code>whenReady</code> would ultimately produce a <code>TestFailedException</code>:
 * </p>
 *
 * <pre class="stHighlight">
 * val task = new Callable[String] { def call() = { Thread.sleep(5000); "hi" } }
 * whenReady(exec.submit(task)) { s =&gt;
 *   s should be ("hi")
 * }
 * </pre>
 *
 * <p>
 * Assuming the default configuration parameters, <code>timeout</code> 1000 milliseconds and <code>interval</code> 10 milliseconds,
 * were passed implicitly to <code>whenReady</code>, the detail message of the thrown
 * <code>TestFailedException</code> would look like:
 * </p>
 *
 * <p>
 * <code>The future passed to whenReady was never ready, so whenReady timed out. Queried 95 times, sleeping 10 milliseconds between each query.</code>
 * </p>
 *
 * <a name="retryConfig"></a><h2>Configuration of <code>whenReady</code></h2>
 *
 * <p>
 * The <code>whenReady</code> methods of this trait can be flexibly configured.
 * The two configuration parameters for <code>whenReady</code> along with their 
 * default values and meanings are described in the following table:
 * </p>
 *
 * <table style="border-collapse: collapse; border: 1px solid black">
 * <tr>
 * <th style="background-color: #CCCCCC; border-width: 1px; padding: 3px; text-align: center; border: 1px solid black">
 * <strong>Configuration Parameter</strong>
 * </th>
 * <th style="background-color: #CCCCCC; border-width: 1px; padding: 3px; text-align: center; border: 1px solid black">
 * <strong>Default Value</strong>
 * </th>
 * <th style="background-color: #CCCCCC; border-width: 1px; padding: 3px; text-align: center; border: 1px solid black">
 * <strong>Meaning</strong>
 * </th>
 * </tr>
 * <tr>
 * <td style="border-width: 1px; padding: 3px; border: 1px solid black; text-align: center">
 * timeout
 * </td>
 * <td style="border-width: 1px; padding: 3px; border: 1px solid black; text-align: center">
 * 1000
 * </td>
 * <td style="border-width: 1px; padding: 3px; border: 1px solid black; text-align: left">
 * the maximum amount of time in milliseconds to allow unsuccessful queries before giving up and throwing <code>TestFailedException</code>
 * </td>
 * </tr>
 * <tr>
 * <td style="border-width: 1px; padding: 3px; border: 1px solid black; text-align: center">
 * interval
 * </td>
 * <td style="border-width: 1px; padding: 3px; border: 1px solid black; text-align: center">
 * 10
 * </td>
 * <td style="border-width: 1px; padding: 3px; border: 1px solid black; text-align: left">
 * the number of milliseconds to sleep between each query
 * </td>
 * </tr>
 * </table>
 *
* <p>
 * The <code>whenReady</code> methods of trait <code>WhenReady</code> each take an <code>RetryConfig</code>
 * object as an implicit parameter. This object provides values for the two configuration parameters. Trait
 * <code>WhenReady</code> provides an implicit <code>val</code> named <code>retryConfig</code> with each
 * configuration parameter set to its default value. 
 * If you want to set one or more configuration parameters to a different value for all invocations of
 * <code>whenReady</code> in a suite you can override this
 * val (or hide it, for example, if you are importing the members of the <code>WhenReady</code> companion object rather
 * than mixing in the trait). For example, if
 * you always want the default <code>timeout</code> to be 2 seconds and the default <code>interval</code> to be 5 milliseconds, you
 * can override <code>retryConfig</code>, like this:
 *
 * <pre class="stHighlight">
 * implicit override val retryConfig =
 *   RetryConfig(timeout = 2000, interval = 5)
 * </pre>
 *
 * <p>
 * Or, hide it by declaring a variable of the same name in whatever scope you want the changed values to be in effect:
 * </p>
 *
 * <pre class="stHighlight">
 * implicit val retryConfig =
 *   RetryConfig(timeout = 2000, interval = 5)
 * </pre>
 *
 * <p>
 * In addition to taking a <code>RetryConfig</code> object as an implicit parameter, the <code>whenReady</code> methods of trait
 * <code>WhenReady</code> include overloaded forms that take one or two <code>RetryConfigParam</code>
 * objects that you can use to override the values provided by the implicit <code>RetryConfig</code> for a single <code>whenReady</code>
 * invocation. For example, if you want to set <code>timeout</code> to 5000 for just one particular <code>whenReady</code> invocation,
 * you can do so like this:
 * </p>
 *
 * <pre class="stHighlight">
 * whenReady (exec.submit(task), timeout(6000)) { s =&gt;
 *   s should be ("hi")
 * }
 * </pre>
 *
 * <p>
 * This invocation of <code>eventually</code> will use 6000 for <code>timeout</code> and whatever value is specified by the 
 * implicitly passed <code>RetryConfig</code> object for the <code>interval</code> configuration parameter.
 * If you want to set both configuration parameters in this way, just list them separated by commas:
 * </p>
 * 
 * <pre class="stHighlight">
 * whenReady (exec.submit(task), timeout(6000), interval(500)) { s =&gt;
 *   s should be ("hi")
 * }
 * </pre>
 *
 * <p>
 * <em>Note: The <code>whenReady</code> construct was in part inspired by the <code>whenDelivered</code> matcher of the 
 * <a href="http://github.com/jdegoes/blueeyes" target="_blank">BlueEyes</a> project, a lightweight, asynchronous web framework for Scala.</em>
 * </p>
 *
 * @author Bill Venners
 */
trait WhenReady extends RetryConfiguration {

  /**
   * Queries the passed future repeatedly until it either is ready, or a configured maximum
   * amount of time has passed, sleeping a configured interval between attempts; and when ready, passes the future's value
   * to the passed function.
   *
   * <p>
   * The maximum amount of time in milliseconds to tolerate unsuccessful queries before giving up and throwing
   * <code>TestFailedException</code> is configured by the value contained in the passed
   * <code>timeout</code> parameter.
   * The interval to sleep between attempts is configured by the value contained in the passed
   * <code>interval</code> parameter.
   * </p>
   *
   * @param future the future to query
   * @param timeout the <code>Timeout</code> configuration parameter
   * @param interval the <code>Interval</code> configuration parameter
   * @param fun the function to which pass the future's value when it is ready
   * @param config an <code>RetryConfig</code> object containing <code>timeout</code> and
   *          <code>interval</code> parameters that are unused by this method
   * @return the result of invoking the <code>fun</code> parameter
   */
  def whenReady[T, U](future: FutureConcept[T], timeout: Timeout, interval: Interval)(fun: T => U)(implicit config: RetryConfig): U =
    whenReady(future)(fun)(RetryConfig(timeout.value, interval.value))

  /**
   * Queries the passed future repeatedly until it either is ready, or a configured maximum
   * amount of time has passed, sleeping a configured interval between attempts; and when ready, passes the future's value
   * to the passed function.
   *
   * <p>
   * The maximum amount of time in milliseconds to tolerate unsuccessful queries before giving up and throwing
   * <code>TestFailedException</code> is configured by the value contained in the passed
   * <code>timeout</code> parameter.
   * The interval to sleep between attempts is configured by the value contained in the passed
   * <code>interval</code> parameter.
   * </p>
   *
   * @param future the future to query
   * @param interval the <code>Interval</code> configuration parameter
   * @param timeout the <code>Timeout</code> configuration parameter
   * @param fun the function to which pass the future's value when it is ready
   * @param config an <code>RetryConfig</code> object containing <code>timeout</code> and
   *          <code>interval</code> parameters that are unused by this method
   * @return the result of invoking the <code>fun</code> parameter
   */
  def whenReady[T, U](future: FutureConcept[T], interval: Interval, timeout: Timeout)(fun: T => U)(implicit config: RetryConfig): U =
    whenReady(future)(fun)(RetryConfig(timeout.value, interval.value))

  /**
   * Queries the passed future repeatedly until it either is ready, or a configured maximum
   * amount of time has passed, sleeping a configured interval between attempts; and when ready, passes the future's value
   * to the passed function.
   *
   * <p>
   * The maximum amount of time in milliseconds to tolerate unsuccessful queries before giving up and throwing
   * <code>TestFailedException</code> is configured by the value contained in the passed
   * <code>timeout</code> parameter.
   * The interval to sleep between attempts is configured by the <code>interval</code> field of
   * the <code>RetryConfig</code> passed implicitly as the last parameter.
   * </p>
   *
   * @param future the future to query
   * @param timeout the <code>Timeout</code> configuration parameter
   * @param fun the function to which pass the future's value when it is ready
   * @param config an <code>RetryConfig</code> object containing <code>timeout</code> and
   *          <code>interval</code> parameters that are unused by this method
   * @return the result of invoking the <code>fun</code> parameter
   */
  def whenReady[T, U](future: FutureConcept[T], timeout: Timeout)(fun: T => U)(implicit config: RetryConfig): U =
    whenReady(future)(fun)(RetryConfig(timeout.value, config.interval))

  /**
   * Queries the passed future repeatedly until it either is ready, or a configured maximum
   * amount of time has passed, sleeping a configured interval between attempts; and when ready, passes the future's value
   * to the passed function.
   *
   * <p>
   * The maximum amount of time in milliseconds to tolerate unsuccessful attempts before giving up is configured by the <code>timeout</code> field of
   * the <code>RetryConfig</code> passed implicitly as the last parameter.
   * The interval to sleep between attempts is configured by the value contained in the passed
   * <code>interval</code> parameter.
   * </p>
   *
   * @param future the future to query
   * @param interval the <code>Interval</code> configuration parameter
   * @param fun the function to which pass the future's value when it is ready
   * @param config an <code>RetryConfig</code> object containing <code>timeout</code> and
   *          <code>interval</code> parameters that are unused by this method
   * @return the result of invoking the <code>fun</code> parameter
   */
  def whenReady[T, U](future: FutureConcept[T], interval: Interval)(fun: T => U)(implicit config: RetryConfig): U =
    whenReady(future)(fun)(RetryConfig(config.timeout, interval.value))

  /**
   * Queries the passed future repeatedly until it either is ready, or a configured maximum
   * amount of time has passed, sleeping a configured interval between attempts; and when ready, passes the future's value
   * to the passed function.
   *
   * <p>
   * The maximum amount of time in milliseconds to tolerate unsuccessful attempts before giving up is configured by the <code>timeout</code> field of
   * the <code>RetryConfig</code> passed implicitly as the last parameter.
   * The interval to sleep between attempts is configured by the <code>interval</code> field of
   * the <code>RetryConfig</code> passed implicitly as the last parameter.
   * </p>
   *
   *
   * @param future the future to query
   * @param fun the function to which pass the future's value when it is ready
   * @param config an <code>RetryConfig</code> object containing <code>timeout</code> and
   *          <code>interval</code> parameters that are unused by this method
   * @return the result of invoking the <code>fun</code> parameter
   */
  def whenReady[T, U](future: FutureConcept[T])(fun: T => U)(implicit config: RetryConfig): U = {

    val startMillis = System.currentTimeMillis

    @tailrec
    def tryTryAgain(attempt: Int): U = {
      val timeout = config.timeout
      val interval = config.interval
      if (future.isCanceled)
        throw new TestFailedException(
          sde => Some(Resources("futureWasCanceled", attempt.toString, interval.toString)),
          None,
          getStackDepthFun("WhenReady.scala", "whenReady")
        )
      if (future.isExpired)
        throw new TestFailedException(
          sde => Some(Resources("futureExpired", attempt.toString, interval.toString)),
          None,
          getStackDepthFun("WhenReady.scala", "whenReady")
        )
      future.value match {
        case Some(Right(v)) => fun(v)
        case Some(Left(tpe: TestPendingException)) => throw tpe // TODO: In 2.0 add TestCanceledException here
        case Some(Left(e)) if anErrorThatShouldCauseAnAbort(e) => throw e
        case Some(Left(e)) =>
          val hasMessage = e.getMessage != null
          throw new TestFailedException(
            sde => Some {
              if (e.getMessage == null)
                Resources("futureReturnedAnException", e.getClass.getName)
              else
                Resources("futureReturnedAnExceptionWithMessage", e.getClass.getName, e.getMessage)
            },
            Some(e),
            getStackDepthFun("WhenReady.scala", "whenReady")
          )
        case None => 
          val duration = System.currentTimeMillis - startMillis
          if (duration < timeout)
            Thread.sleep(interval)
          else {
            throw new TestFailedException(
              sde => Some(Resources("wasNeverReady", attempt.toString, interval.toString)),
              None,
              getStackDepthFun("WhenReady.scala", "whenReady")
            )
          }

          tryTryAgain(attempt + 1)
      }
    }
    tryTryAgain(1)
  }
  
  /**
   * Implicitly converts a <code>java.util.concurrent.Future[T]</code> to
   * <code>org.scalatest.concurrent.FutureConcept[T]</code>, allowing a Java <code>Future</code> to be passed
   * to the <code>whenReady</code> methods of this trait.
   *
   * @param futureOfJava a <code>java.util.concurrent.Future[T]</code> to convert
   */
  implicit def convertFutureOfJava[T](futureOfJava: FutureOfJava[T]) =
    new FutureConcept[T] {
      def value: Option[Either[Throwable, T]] = 
        if (futureOfJava.isDone())
          Some(Right(futureOfJava.get))
        else
          None
      def isExpired: Boolean = false // Java Futures don't support the notion of a timeout
      def isCanceled: Boolean = futureOfJava.isCancelled // Two ll's in Canceled. The verbosity of Java strikes again!
    } 
}

/**
 * Companion object that facilitates the importing of <code>WhenReady</code> members as 
 * an alternative to mixing in the trait. One use case is to import <code>WhenReady</code>'s members so you can use
 * them in the Scala interpreter:
 *
 * <pre class="stREPL">
 * $ scala -cp scalatest-1.8.jar
 * Welcome to Scala version 2.9.1.final (Java HotSpot(TM) 64-Bit Server VM, Java 1.6.0_29).
 * Type in expressions to have them evaluated.
 * Type :help for more information.
 *
 * scala&gt; import org.scalatest._
 * import org.scalatest._
 *
 * scala&gt; import matchers.ShouldMatchers._
 * import matchers.ShouldMatchers._
 *
 * scala&gt; import concurrent.WhenReady._
 * import concurrent.WhenReady._
 *
 * scala&gt; import java.util.concurrent._
 * import java.util.concurrent._
 *
 * scala&gt; val exec = Executors.newSingleThreadExecutor
 * newSingleThreadExecutor   
 * 
 * scala&gt; val task = new Callable[String] { def call() = { Thread.sleep(500); "hi" } }
 * task: java.lang.Object with java.util.concurrent.Callable[String] = $anon$1@e1a973
 * 
 * scala&gt; whenReady(exec.submit(task)) { s =&gt; s shouldBe "hi" }
 * 
 * scala&gt; val task = new Callable[String] { def call() = { Thread.sleep(5000); "hi" } }
 * task: java.lang.Object with java.util.concurrent.Callable[String] = $anon$1@2993dfb0
 * 
 * scala&gt; whenReady(exec.submit(task)) { s =&gt; s shouldBe "hi" }
 * org.scalatest.TestFailedException: The future passed to whenReady was never ready, so whenReady timed out. Queried 95 times, sleeping 10 milliseconds between each query.
 *   at org.scalatest.concurrent.WhenReady$class.tryTryAgain$1(WhenReady.scala:203)
 *   ...
 * </pre>
 */
object WhenReady extends WhenReady
