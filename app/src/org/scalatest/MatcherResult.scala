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
 * The result of the <code>Matcher</code> <code>apply</code> method.
 *
 * @param matches indicates whether or not the compared values matched
 * @param failureMessage if a match was intended (x should match), but did not match
 * @param negativeFailureMessage if a match was not indented (x should not { match }), but matched
 */
case class MatchResult(
  matches: Boolean,
  failureMessage: String,
  negativeFailureMessage: String
)

