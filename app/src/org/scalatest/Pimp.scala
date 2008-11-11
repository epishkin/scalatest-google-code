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

import java.lang.reflect.Method
import java.lang.reflect.Modifier

/**
 * Trait that grants objects access to the private members of other objects. If you wish to test
 * a private method, mix in trait <code>Pimp</code>. You must then create a <code>PrivateMethod</code>
 * object, like this: 
 *
 * <pre>
 * 
 * </pre>
 *
 * @author Bill Venners
 */
trait Pimp {
  
  case class PrivateMethod[T](methodName: Symbol) {
    def apply(args: Any*) = Invocation[T](methodName, args: _*)
  }
  case class Invocation[T](methodName: Symbol, args: Any*)

  class Invoker(target: AnyRef) {

    def invokePrivate[T](invocation: Invocation[T]): T = {
      import invocation._

      // If 'getMessage passed as methodName, methodNameToInvoke would be "getMessage"
      val methodNameToInvoke = methodName.toString.substring(1)
  
      def isMethodToInvoke(m: Method) = {
  
        val isInstanceMethod = !Modifier.isStatic(m.getModifiers())
        val simpleName = m.getName
        val paramTypes = m.getParameterTypes
        val candidateResultType = m.getReturnType
        val isPrivate = Modifier.isPrivate(m.getModifiers())
  
        // I think until people complain, the result type should be exactly the same type as the
        // passed class. This makes it easier to match the invokePrivate call with the actual private
        // method. It is arguable I should allow the actual return type to be a subtype, but now that
        // I think more about it, maybe that's not even knowable for sure because of erasure. So the result
        // type must match exactly. 
  
        // The AnyVals must go in as Java wrapper types. But the type is still Any, so this needs to be converted
        // to AnyRef for the compiler to be happy. Implicit conversions are ambiguous, and really all that's needed
        // is a type cast, so I use isInstanceOf.
        def argsHaveValidTypes: Boolean = {
  
          // First, the arrays must have the same length:
          if (args.length == paramTypes.length) {
            val zipped = args.toList zip paramTypes.toList
  
            // The args classes need only be assignable to the parameter type. So therefore the parameter type
            // must be assignable *from* the corresponding arg class type.
            val invalidArgs =
              for ((arg, paramType) <- zipped if !paramType.isAssignableFrom(arg.asInstanceOf[AnyRef].getClass)) yield arg
            invalidArgs.length == 0
          }
          else false
        }
  
  /*
  The rules may be that private mehods in standalone objects current get name mangled and made public,
  perhaps because there are two versions of each private method, one in the actual singleton and one int
  the class that also has static methods, and one calls the other. So if this is true, then I may change this
  to say if simpleName matches exactly and its private, or if ends with simpleName prepended by two dollar signs,
  then let it be public, but look for whatever the Scala compiler puts in there to mark it as private at the Scala source level.
  
        // org$scalatest$FailureMessages$$decorateToStringValue
  // 0 org$scalatest$FailureMessages$$decorateToStringValue
       [java] 1 true
       [java] 2 false
       [java] false
       [java] false
       [java] ^&^&^&^&^&^& invalidArgs.length is: 0
       [java] 5 true
  
        println("0 "+ simpleName)
        println("1 "+ isInstanceMethod)
        println("2 "+ isPrivate)
        println("3 "+ simpleName == methodNameToInvoke)
        println("4 "+ candidateResultType == resultType)
        println("5 "+ argsHaveValidTypes)
    This ugliness. I'll ignore the result type for now. Sheesh. Investigate that one. And I'll
    have to ignore private too for now, because in the bytecodes it isn't even private. And I'll
    also allow methods that end with $$<simpleName> if the simpleName doesn't match
  */
  
        // isInstanceMethod && isPrivate && simpleName == methodNameToInvoke && candidateResultType == resultType  && argsHaveValidTypes
        isInstanceMethod && (simpleName == methodNameToInvoke || simpleName.endsWith("$$"+ methodNameToInvoke)) && argsHaveValidTypes
      }
  
      // Store in an array, because may have both isEmpty and empty, in which case I
      // will throw an exception.
      val methodArray =
        for (m <- target.getClass.getDeclaredMethods; if isMethodToInvoke(m))
          yield m
  
      if (methodArray.length == 0)
        throw new IllegalArgumentException("Can't find the method")
      else if (methodArray.length > 1)
        throw new IllegalArgumentException("Found two methods")
      else {
        val anyRefArgs = // Need to box these myself, because that's invoke is expecting an Array[Object], which maps to an Array[AnyRef]
          for (arg <- args) yield arg match {
            case anyVal: AnyVal => anyVal.asInstanceOf[AnyRef]
            case anyRef: AnyRef => anyRef
          }
        val privateMethodToInvoke = methodArray(0)
        privateMethodToInvoke.setAccessible(true)
        privateMethodToInvoke.invoke(target, anyRefArgs.toArray).asInstanceOf[T]
      }
    }
  }
  implicit def anyRefToInvoker(anyRef: AnyRef): Invoker = new Invoker(anyRef)
}

