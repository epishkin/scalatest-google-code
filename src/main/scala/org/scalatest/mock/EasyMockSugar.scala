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

/**
 * Trait that provides some basic syntax sugar for <a href="http://easymock.org/">EasyMock</a>.
 *
 * <p>
 * Using the EasyMock API directly, you create a mock with:
 * </p>
 *
 * <pre>
 * val mockCollaborator = createMock(classOf[Collaborator])
 * </pre>
 *
 * <p>
 * With this trait, you can shorten that to:
 * </p>
 *
 * <pre>
 * val mockCollaborator = mock[Collaborator]
 * </pre>
 *
 * <p>
 * After creating mocks, you set expectations on them, using syntax like this:
 * </p>
 *
 * <pre>
 * mockCollaborator.documentAdded("Document")
 * mockCollaborator.documentChanged("Document")
 * expectLastCall().times(3)
 * </pre>
 *
 * <p>
 * If you wish to highlight which statements are setting expectations on the mock (versus
 * which ones are actually using the mock), you can place them in an <code>expecting</code>
 * clause, provided by this trait, like this:
 * </p>
 *
 * <pre>
 * expecting {
 *   mockCollaborator.documentAdded("Document")
 *   mockCollaborator.documentChanged("Document")
 *   expectLastCall().times(3)
 * }
 * </pre>
 *
 * <p>
 * Using an <code>expecting</code> clause is optional, because it does nothing but visually indicate
 * which statements are setting expectations on mocks.
 * </p>
 *
 * <p>
 * Once you've set expectations on the mock objects, you must invoke <code>replay</code> on
 * the mocks to indicate you are done setting expectations, and will start using the mock.
 * After using the mock, you must invoke <code>verify</code> to check to make sure the mock
 * was used in accordance with the expectations you set on it. Here's how that looks when you
 * use the EasyMock API directly:
 * </p>
 *
 * <pre>
 * replay(mockCollaborator)
 * classUnderTest.addDocument("Document", new Array[Byte](0))
 * classUnderTest.addDocument("Document", new Array[Byte](0))
 * classUnderTest.addDocument("Document", new Array[Byte](0))
 * classUnderTest.addDocument("Document", new Array[Byte](0))
 * verify(mockCollaborator)
 * </pre>
 *
 * <p>
 * This trait enables you to use the following, more concise syntax instead:
 * </p>
 *
 * <pre>
 * whenExecuting(mockCollaborator) {
 *   classUnderTest.addDocument("Document", new Array[Byte](0))
 *   classUnderTest.addDocument("Document", new Array[Byte](0))
 *   classUnderTest.addDocument("Document", new Array[Byte](0))
 *   classUnderTest.addDocument("Document", new Array[Byte](0))
 * }
 * </pre>
 *
 * <p>
 * The <code>whenExecuting</code> method will pass the <code>mockCollaborator</code> to
 * <code>replay</code>, execute the passed function (your code that uses the mock), and
 * call <code>verify</code>, passing in the <code>mockCollaborator</code>. If you want to
 * use multiple mocks, you can pass multiple mocks to <code>whenExecuting</code>.
 * </p>
 *
 * <p>
 * To summarize, here's what a typical test using <code>EasyMockSugar</code> looks like:
 * </p>
 *
 * <pre>
 * val mockCollaborator = mock[Collaborator]
 *
 * expecting {
 *   mockCollaborator.documentAdded("Document")
 *   mockCollaborator.documentChanged("Document")
 *   expectLastCall().times(3)
 * }
 *
 * whenExecuting(mockCollaborator) {
 *   classUnderTest.addDocument("Document", new Array[Byte](0))
 *   classUnderTest.addDocument("Document", new Array[Byte](0))
 *   classUnderTest.addDocument("Document", new Array[Byte](0))
 *   classUnderTest.addDocument("Document", new Array[Byte](0))
 * }
 * </pre>
 */
trait EasyMockSugar {

  def expectCall[T](value: T): IExpectationSetters[T] = EasyMock.expect(value)

  /**
   *
   * <pre>
   * val mockCollaborator = createMock(classOf[Collaborator])
   * </pre>
   *
   * <pre>
   * val mockCollaborator = mock[Collaborator]
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
   *

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
