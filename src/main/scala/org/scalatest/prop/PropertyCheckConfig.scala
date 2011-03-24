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
package org.scalatest.prop

/**
 * Configuration object for property checks.
 *
 * <p>
 * The default values for the parameters are:
 * </p>
 *
 * <table>
 * <tr>
 * <td>
 * minSuccessful
 * </td>
 * <td>
 * 100
 * </td>
 * </tr>
 * <tr>
 * <td>
 * maxSkipped
 * </td>
 * <td>
 * 500
 * </td>
 * </tr>
 * <tr>
 * <td>
 * minSize
 * </td>
 * <td>
 * 0
 * </td>
 * </tr>
 * <tr>
 * <td>
 * maxSize
 * </td>
 * <td>
 * 100
 * </td>
 * </tr>
 * <tr>
 * <td>
 * workers
 * </td>
 * <td>
 * 1
 * </td>
 * </tr>
 * </table>
 *
 * @param minSuccessful the minimum number of successful property evaluations required for the property to pass.
 * @param maxSkipped the maximum number of skipped property evaluations allowed during a property check
 * @param minSize the minimum size parameter to provide to ScalaCheck, which it will use when generating objects for which size matters (such as strings or lists).
 * @param maxSize the maximum size parameter to provide to ScalaCheck, which it will use when generating objects for which size matters (such as strings or lists).
 * @param workers specifies the number of worker threads * to use during property evaluation
 * @throw IllegalArgumentException if specified <code>minSuccessful</code> value is less than or equal to zero.
 * @throw IllegalArgumentException if specified <code>maxSkipped</code> value is less than zero.
 * @throw IllegalArgumentException if specified <code>minSize</code> value is less than zero.
 * @throw IllegalArgumentException if specified <code>maxSize</code> value is less than zero.
 * @throw IllegalArgumentException if specified <code>workers</code> value is less than or equal to zero.
 *
 * @author Bill Venners
 */
case class PropertyCheckConfig(
  minSuccessful: Int = 100,
  maxSkipped: Int = 500,
  minSize: Int = 0,
  maxSize: Int = 100,
  workers: Int = 1
) {
  require(minSuccessful > 0, "minSuccessful had value " + minSuccessful + ", but must be greater than zero")
  require(maxSkipped >= 0, "maxSkipped had value " + maxSkipped + ", but must be greater than or equal to zero")
  require(minSize >= 0, "minSize had value " + minSize + ", but must be greater than or equal to zero")
  require(maxSize >= 0, "maxSize had value " + maxSize + ", but must be greater than or equal to zero")
  require(workers > 0, "workers had value " + workers + ", but must be greater than zero")
}

