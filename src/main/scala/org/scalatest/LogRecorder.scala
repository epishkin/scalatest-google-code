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
 * Placeholder for a future enhancement that will allow log messages written
 * during tests that fail to be reported. This placeholder trait was added in
 * 1.0 so that implementing this feature in a future release could be done
 * without breaking code that calls or overrides methods the run methods on
 * Suite that take an optional instance of this type.
 */
trait LogRecorder 