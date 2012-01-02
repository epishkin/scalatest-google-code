/*
 * Copyright 2001-2011 Artima, Inc.
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

import org.scalatest.StackDepthExceptionHelper.getStackDepthFun

/**
 * Trait containing the <code>inside</code> construct, which allows you to make statements about nested object graphs using pattern matching.
 *
 *
 * <p>
 * For example, given the following case classes:
 *
 * <pre>
 * case class Address(street: String, city: String, state: String, zip: String)
 * case class Name(first: String, middle: String, last: String)
 * case class Record(name: Name, address: Address, age: Int)
 * </pre>
 *
 * You could write 
 * <pre>
     inside (rec) { case Record(name, address, age) =>
       inside (name) { case Name(first, middle, last) =>
         first should be ("Sally")
         middle should startWith ("Ma")
         last should endWith ("nes")
       }
       inside (address) { case Address(street, city, state, zip) =>
         street should be ("123 Main St")
         city should be ("Bluesville")
         state.toLowerCase should be ("ky")
         zip should be ("12345")
       }
       age should be >= 21
     }
 inside (rec) { case Record(name, address, age) =>
   inside(name) { case Name(first, middle, last) =>
     first should be ("Tommy")
     middle should be ("Lee")
     last should be ("Jones")
   }
   inside (address) { case Address(street, city, state, zip) =>
     street should startWith ("25")
     city should endWith ("Angeles")
     state should equal ("CA")
     zip should be ("12345")
   }
   age should be < 99
 }
</pre>

*/
trait Inside {
  def inside[T](value: T)(pf: PartialFunction[T, Unit]) {
    def appendInsideMessage(currentMessage: Option[String]) =
      currentMessage match {
        case Some(msg) => Some(Resources("insidePartialFunctionAppendSomeMsg", msg.trim, value.toString()))
        case None => Some(Resources("insidePartialFunctionAppendNone", value.toString()))
      }
    if (pf.isDefinedAt(value)) {
      try {
        pf(value)
      }
      catch {
        case e: ModifiableMessage[_] =>
          throw e.modifyMessage(appendInsideMessage)
      }
    }
    else
      throw new TestFailedException(sde => Some(Resources("insidePartialFunctionNotDefined", value.toString())), None, getStackDepthFun("Inside.scala", "inside"))
  }
}

object Inside extends Inside
