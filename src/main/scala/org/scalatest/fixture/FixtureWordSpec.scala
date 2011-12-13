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
package org.scalatest.fixture

import org.scalatest._
import FixtureNodeFamily._
import verb.{CanVerb, ResultOfAfterWordApplication, ShouldVerb, BehaveWord, MustVerb,
  StringVerbBlockRegistration}
import scala.collection.immutable.ListSet
import org.scalatest.StackDepthExceptionHelper.getStackDepth
import java.util.concurrent.atomic.AtomicReference
import java.util.ConcurrentModificationException
import org.scalatest.events._
import Suite.anErrorThatShouldCauseAnAbort

/**
 * <strong>FixtureWordSpec has been deprecated and will be removed in a future
 * release of ScalaTest. Please change any uses of <code>org.scalatest.FixtureWordSpec</code>
 * to a corresponding use of <a href="WordSpec.html"><code>org.scalatest.fixture.WordSpec</code></a> instead.</strong>
 */
@deprecated("Please use org.scalatest.fixture.WordSpec instead.")
trait FixtureWordSpec extends WordSpec with ShouldVerb with MustVerb with CanVerb 
