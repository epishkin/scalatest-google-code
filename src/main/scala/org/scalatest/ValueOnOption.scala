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

import java.util.NoSuchElementException
import org.scalatest.StackDepthExceptionHelper.getStackDepthFun

/**
 * Trait that provides an implicit conversion that adds a <code>value</code> method
 * to <code>Option</code>, which will return the value of the option if it is defined,
 * or throw <code>TestFailedException</code> if it is not defined.
 *
 * <p>
 * 
 * </p>
 */
trait ValueOnOption {

  implicit def convertOptionToValuable[T](opt: Option[T]) = new Valuable(opt)

  class Valuable[T](opt: Option[T]) {
    def value: T = {
      try {
        opt.get
      }
      catch {
        case cause: NoSuchElementException => 
          throw new TestFailedException(sde => Some(Resources("optionValueNotDefined")), Some(cause), getStackDepthFun("ValueOnOption.scala", "value"))
      }
    }
  }
}

object ValueOnOption extends ValueOnOption
