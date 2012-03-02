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

/**
 * suiteTags is a map from String suiteId to a set of tags for that suite.
 * testTags is a map from String suiteId to a map, whose keys are testnames and values the tags for that test.
 */
case class DynaTags(suiteTags: Map[String, Set[String]], testTags: Map[String, Map[String, Set[String]]])
