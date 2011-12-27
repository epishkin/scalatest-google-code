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

/**
 * <strong>Spec has been deprecated and will be used for a different purpose in a future
 * version of ScalaTest. Please change any uses of <code>org.scalatest.Spec</code>
 * to a corresponding use of <a href="FunSpec.html"><code>org.scalatest.FunSpec</code></a>.
 * This is just a name change, so all you need to do is add <code>Fun</code> in front of <code>Spec</code>.</strong>
 *
 * <p>
 * The purpose of this change is to allow the name <code>Spec</code> to be later used
 * for a behavior-driven development style in which tests are methods, which eliminates
 * one generated class file per test.
 * </p>
 * 
 */
@deprecated("Please use org.scalatest.FunSpec instead.")
trait Spec extends FunSpec {
  override protected[scalatest] val fileName = "Spec.scala"
}
