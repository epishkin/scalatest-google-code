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
package org.scalatest.mock

import org.scalatest._
import org.easymock.{IExpectationSetters, EasyMock}
import scala.reflect.Manifest

trait EasyMockSugar {

  def expectCall[T](value: T): IExpectationSetters[T] = EasyMock.expect(value)

  /**
   *
   * <pre>
   * val reporter = createMock(classOf[Reporter])
   * </pre>
   *
   * <pre>
   * val reporter = mock[Reporter]
   * </pre>
   */
  def mock[T <: AnyRef](implicit manifest: Manifest[T]): T = {
    EasyMock.createMock(manifest.erasure.asInstanceOf[Class[T]])
  }

  /**
   * <pre>
   * mockCollaborator.documentAdded("Document")
   * mockCollaborator.documentChanged("Document")
   * expectLastCall().times(3)
   * </pre>
   *
   * <pre>
   * expecting {
   *   mockCollaborator.documentAdded("Document")
   *   mockCollaborator.documentChanged("Document")
   *   expectLastCall().times(3)
   * }
   * </pre>
   */
  def expecting(unused: Any) = ()

  /**
   * <pre>
   * replay(mock)
   * classUnderTest.addDocument("Document", new Array[Byte](0))
   * classUnderTest.addDocument("Document", new Array[Byte](0))
   * classUnderTest.addDocument("Document", new Array[Byte](0))
   * classUnderTest.addDocument("Document", new Array[Byte](0))
   * verify(mock)
   * </pre>
   *
   * <pre>
   * whenExecuting(mockCollaborator) {
   *   classUnderTest.addDocument("Document", new Array[Byte](0))
   *   classUnderTest.addDocument("Document", new Array[Byte](0))
   *   classUnderTest.addDocument("Document", new Array[Byte](0))
   *   classUnderTest.addDocument("Document", new Array[Byte](0))
   * }
   * </pre>
   */
  def whenExecuting(mock: AnyRef)(f: => Unit) = {
    EasyMock.replay(mock)
    f
    EasyMock.verify(mock)
  }

  // TODO: create an overloaded form of whenExecuting that takes varargs
}
