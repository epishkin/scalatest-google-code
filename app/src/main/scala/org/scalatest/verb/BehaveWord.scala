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
package org.scalatest.verb

import org.scalatest._

/**
 * This class enables syntax such as the following, which works in a <code>WordSpec</code>:
 *
 * <pre>
 * behave like nonEmptyStack(lastValuePushed)
 * ^
 * </pre>
 *
 * <p>
 * As well as the following syntax, which works in <code>Spec</code> and <code>FlatSpec</code>:
 * </p>
 *
 * <pre>
 * it should behave like nonEmptyStack(lastValuePushed)
 *           ^
 * </pre>
 */
class BehaveWord {

  /**
   * This method enables syntax such as the following, which works in a <code>WordSpec</code>:
   *
   * <pre>
   * behave like nonEmptyStack(lastValuePushed)
   *        ^
   * </pre>
   */
  def like(unit: Unit) = ()
}
