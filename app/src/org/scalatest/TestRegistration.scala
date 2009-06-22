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
 * Trait mixed in by <code>Suite</code> traits that register tests as function values during <code>Suite</code> object construction.
 * This trait is used as a self type in the <code>ShouldBehaveLike</code> and <code>MustBehaveLike</code> traits in the
 * <code>org.scalatest.matchers</code> package, to ensure at compile time that these "behave like" traits are being mixed into
 * classes for which they will function correction.
 */
trait TestRegistration

