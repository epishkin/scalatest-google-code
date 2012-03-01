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
package org.scalatest

/**
 * Trait providing two implicit conversions that allow you to specify <code>Long</code> durations of time
 * with units such as <code>millis</code>, <code>seconds</code>, and <code>minutes</code>.
 * 
 * <p>
 * This trait enables you to specify units of time when you need a <code>Long</code> number of milliseconds. This
 * can be used, for example, with the <code>failAfter</code> method of trait <code>Timeouts</code> or the
 * <code>timeLimit</code> field of trait <code>TimeLimitedTests</code>. Here are examples of each unit enabled
 * by this trait: 
 * </p>
 * 
 * <pre>
 * Thread.sleep(1 millisecond)
 * Thread.sleep(2 milliseconds)
 * Thread.sleep(2 millis)
 * Thread.sleep(1 second)
 * Thread.sleep(2 seconds)
 * Thread.sleep(1 minute)
 * Thread.sleep(2 minutes)
 * Thread.sleep(1 hour)
 * Thread.sleep(2 hours)
 * Thread.sleep(1 day)
 * Thread.sleep(2 days) // A nice nap indeed
 * </pre>
 * 
 * <p>
 * Because the result of these expressions is simply a <code>Long</code> number of milliseconds, you can also 
 * make arithmetic expressions out of them (so long as you use needed parentheses). For example:
 * </p>
 * 
 * <pre>
 * scala&gt; import org.scalatest.TimeSugar._
 * import org.scalatest.TimeSugar._
 *
 * scala&gt; (1 second) + 88 milliseconds
 * res0: Long = 1088
 * </pre>
 */
trait TimeSugar {

  // Not calling this Duration because everyone else does, so avoids name clash
  /**
   * Class containing methods that return a <code>Long</code> time value calculated from the
   * value passed to the <code>GrainOfTime</code> constructor.
   * 
   * @param value the value to be converted
   */
  class GrainOfTime(value: Long) {
    
    /**
     * A units method for one millisecond. 
     * 
     * @return the value passed to the constructor
     */
    def millisecond: Long = value
    
    /**
     * A units method for milliseconds. 
     * 
     * @return the value passed to the constructor
     */
    def milliseconds: Long = value
    
    /**
     * A shorter units method for milliseconds. 
     * 
     * @return the value passed to the constructor
     */
    def millis: Long = value
    
    /**
     * A units method for one second. 
     * 
     * @return the value passed to the constructor multiplied by 1000
     */
    def second: Long = value * 1000    
    
    /**
     * A units method for seconds. 
     * 
     * @return the value passed to the constructor multiplied by 1000
     */
    def seconds: Long = value * 1000    
        
    /**
     * A units method for one minute. 
     * 
     * @return the value passed to the constructor multiplied by 1000 * 60
     */
    def minute: Long = value * 1000 * 60   

    /**
     * A units method for minutes. 
     * 
     * @return the value passed to the constructor multiplied by 1000 * 60
     */
    def minutes: Long = value * 1000 * 60
    
    /**
     * A units method for one hour. 
     * 
     * @return the value passed to the constructor multiplied by 1000 * 60 * 60
     */
    def hour: Long = value * 1000 * 60 * 60  

    /**
     * A units method for hours. 
     * 
     * @return the value passed to the constructor multiplied by 1000 * 60 * 60
     */
    def hours: Long = value * 1000 * 60 * 60
    
    /**
     * A units method for one day. 
     * 
     * @return the value passed to the constructor multiplied by 1000 * 60 * 60 * 24
     */
    def day: Long = value * 1000 * 60 * 60 * 24 

    /**
     * A units method for days. 
     * 
     * @return the value passed to the constructor multiplied by 1000 * 60 * 60 * 24
     */
    def days: Long = value * 1000 * 60 * 60 * 24
  }
  
  /**
   * Implicit conversion that adds time units methods to <code>Int</code>s.
   * 
   * @param i: the <code>Int</code> to which to add time units methods
   * @return a <code>GrainOfTime</code> wrapping the passed <code>Int</code>
   */
  implicit def convertIntToGrainOfTime(i: Int) = new GrainOfTime(i)
  
  /**
   * Implicit conversion that adds time units methods to <code>Long</code>s.
   * 
   * @param i: the <code>Long</code> to which to add time units methods
   * @return a <code>GrainOfTime</code> wrapping the passed <code>Long</code>
   */
  implicit def convertLongToGrainOfTime(i: Long) = new GrainOfTime(i)
}

/**
 * Companion object that facilitates the importing of <code>TimeSugar</code> members as 
 * an alternative to mixing it in. One use case is to import <code>TimeSugar</code> members so you can use
 * them in the Scala interpreter:
 *
 * <pre class="stREPL">
 * $scala -classpath scalatest.jar
 * Welcome to Scala version 2.9.1.final (Java HotSpot(TM) 64-Bit Server VM, Java 1.6.0_29).
 * Type in expressions to have them evaluated.
 * Type :help for more information.
 *
 * scala&gt; import org.scalatest.TimeSugar._
 * import org.scalatest.TimeSugar._
 *
 * scala&gt; Thread.sleep(2 seconds)
 * </pre>
 */
object TimeSugar extends TimeSugar
