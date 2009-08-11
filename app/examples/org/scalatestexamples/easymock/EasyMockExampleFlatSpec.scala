/*
 * Copyright 2001-2009 OFFIS, Tammo Freese
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
package org.scalatestexamples.easymock

import org.easymock.EasyMock.{expect => expectCall, _}
import org.junit.Assert._

import java.util.ArrayList
import java.util.List

import org.easymock.IAnswer
import org.junit.Before
import org.junit.Test
import org.scalatest.verb.ShouldVerb
import scalatest.{BeforeAndAfterEach, FlatSpec}
class EasyMockExampleFlatSpec extends FlatSpec with ShouldVerb with BeforeAndAfterEach {

  // Sorry about the nulls and vars, this was ported from Java from an EasyMock example
  private var classUnderTest: ClassTested = _

  private var mock: Collaborator = _

  override def beforeEach() {
    mock = createMock(classOf[Collaborator])
    classUnderTest = new ClassTested()
    classUnderTest.addListener(mock)
  }

  "ClassTested" should "not call the collaborator when removing a non-existing document" in {
    replay(mock)
    classUnderTest.removeDocument("Does not exist")
    ()
  }

  it should "call documentAdded on the Collaborator when a new document is added" in {
    mock.documentAdded("New Document")
    replay(mock)
    classUnderTest.addDocument("New Document", new Array[Byte](0))
    verify(mock)
  }

  it should "call documentChanged on the Collaborator when a document is changed" in {
    mock.documentAdded("Document")
    mock.documentChanged("Document")
    expectLastCall().times(3)
    replay(mock)
    classUnderTest.addDocument("Document", new Array[Byte](0))
    classUnderTest.addDocument("Document", new Array[Byte](0))
    classUnderTest.addDocument("Document", new Array[Byte](0))
    classUnderTest.addDocument("Document", new Array[Byte](0))
    verify(mock)
  }

  it should "call voteForRemoval on Collaborator when removeDocument is called on ClassTested, and" +
          "if a POSITIVE number is returned (i.e., a vote FOR removal), documentRemoved" +
          "should be called on Collaborator" in {
    // expect document addition
    mock.documentAdded("Document");
    // expect to be asked to vote, and vote for it
    expectCall(mock.voteForRemoval("Document")).andReturn((42).asInstanceOf[Byte]);
    // expect document removal
    mock.documentRemoved("Document");

    replay(mock);
    classUnderTest.addDocument("Document", new Array[Byte](0));
    assertTrue(classUnderTest.removeDocument("Document"));
    verify(mock);
  }

  it should "call voteForRemoval on Collaborator when removeDocument is called on ClassTested, and" +
          "if a NEGATIVE number is returned (i.e., a vote AGAINST removal), documentRemoved" +
          "should NOT be called on Collaborator" in {
    // expect document addition
    mock.documentAdded("Document");
    // expect to be asked to vote, and vote against it
    expectCall(mock.voteForRemoval("Document")).andReturn((-42).asInstanceOf[Byte]); //
    // document removal is *not* expected

    replay(mock);
    classUnderTest.addDocument("Document", new Array[Byte](0));
    assertFalse(classUnderTest.removeDocument("Document"));
    verify(mock);
  }

  it should "call voteForRemoval on Collaborator when removeDocument is called on ClassTested" +
          "to remove multiple documents, and if a POSITIVE number is returned (i.e., a vote" +
          "FOR removal), documentRemoved should be called on Collaborator" in {
    mock.documentAdded("Document 1");
    mock.documentAdded("Document 2");
    val documents = Array("Document 1", "Document 2")
    expectCall(mock.voteForRemovals(aryEq(documents))).andReturn((42).asInstanceOf[Byte]);
    mock.documentRemoved("Document 1");
    mock.documentRemoved("Document 2");
    replay(mock);
    classUnderTest.addDocument("Document 1", new Array[Byte](0));
    classUnderTest.addDocument("Document 2", new Array[Byte](0));
    assertTrue(classUnderTest.removeDocuments(Array("Document 1",
            "Document 2")));
    verify(mock);
  }

  it should "call voteForRemoval on Collaborator when removeDocument is called on ClassTested" +
          "to remove multiple documents, and if a NEGATIVE number is returned (i.e., a vote" +
          "AGAINST removal), documentRemoved should NOT be called on Collaborator" in {
    mock.documentAdded("Document 1");
    mock.documentAdded("Document 2");
    val documents = Array("Document 1", "Document 2")
    expectCall(mock.voteForRemovals(aryEq(documents))).andReturn((-42).asInstanceOf[Byte]);
    replay(mock);
    classUnderTest.addDocument("Document 1", new Array[Byte](0));
    classUnderTest.addDocument("Document 2", new Array[Byte](0));
    assertFalse(classUnderTest.removeDocuments(Array("Document 1",
            "Document 2")));
    verify(mock);
  }

  "EasyMock" should "work with both andAnswer and andDelegateTo styles" in {
    
    val l: List[String] = createMock(classOf[List[String]]);

    // andAnswer style
    expectCall(l.remove(10)).andAnswer(new IAnswer[String]() {
      def answer(): String = {
        return getCurrentArguments()(0).toString();
      }
    });

    // andDelegateTo style
    expectCall(l.remove(10)).andDelegateTo(new ArrayList[String]() {
      // private static final long serialVersionUID = 1L;

      override def remove(index: Int): String = {
        return Integer.toString(index);
      }
    });
        
    replay(l);

    assertEquals("10", l.remove(10));
    assertEquals("10", l.remove(10));

    verify(l);

    ()
  }
}
