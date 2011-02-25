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
package org.scalatest.prop

/**
 * Exception that indicates a condition required by a property was not met by
 * a set of input parameters.
 *
 * This exception is thrown by the <code>whenever</code> method defined in trait <code>TableDrivenPropertyChecks</code>
 * when the given condition is false. The <code>forAll</code> methods defined in trait <code>TableDrivenPropertyChecks</code>
 * catch the <code>UnmetConditionException</code> and ignore it, moving on to try the next row in the table it is checking
 * a property against.
 */
class UnmetConditionException extends RuntimeException

