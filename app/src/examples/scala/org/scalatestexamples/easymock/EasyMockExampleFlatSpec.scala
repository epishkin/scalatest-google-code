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

import org.easymock.EasyMock._
import org.junit.Assert._

import java.util.ArrayList
import java.util.List

import org.easymock.IAnswer
import org.junit.Before
import org.junit.Test
import org.scalatest.verb.ShouldVerb
import scalatest.mock.EasyMockSugar
import scalatest.{BeforeAndAfterEach, FlatSpec}
class EasyMockExampleFlatSpec extends FlatSpec with ShouldVerb with BeforeAndAfterEach with EasyMockSugar {

  // Sorry about the nulls and vars, this was ported from Java from an EasyMock example
  private var classUnderTest: ClassTested = _

  private var mockCollaborator: Collaborator = _

  override def beforeEach() {
    mockCollaborator = mock[Collaborator]
    classUnderTest = new ClassTested()
    classUnderTest.addListener(mockCollaborator)
  }

  "ClassTested" should "not call the collaborator when removing a non-existing document" in {
    replay(mockCollaborator)
    classUnderTest.removeDocument("Does not exist")
    ()
  }

  it should "call documentAdded on the Collaborator when a new document is added" in {
    expecting {
      mockCollaborator.documentAdded("New Document")
    }
    whenExecuting(mockCollaborator) {
      classUnderTest.addDocument("New Document", new Array[Byte](0))
    }
  }

  it should "call documentChanged on the Collaborator when a document is changed" in {

    expecting {
      mockCollaborator.documentAdded("Document")
      mockCollaborator.documentChanged("Document")
      expectLastCall().times(3)
    }
    
    whenExecuting(mockCollaborator) {
      classUnderTest.addDocument("Document", new Array[Byte](0))
      classUnderTest.addDocument("Document", new Array[Byte](0))
      classUnderTest.addDocument("Document", new Array[Byte](0))
      classUnderTest.addDocument("Document", new Array[Byte](0))
    }
  }

  it should "call voteForRemoval on Collaborator when removeDocument is called on ClassTested, and " +
          "if a POSITIVE number is returned (i.e., a vote FOR removal), documentRemoved " +
          "should be called on Collaborator" in {

    expecting {
      // expect document addition
      mockCollaborator.documentAdded("Document");
      // expect to be asked to vote, and vote for it
      expectCall(mockCollaborator.voteForRemoval("Document")).andReturn((42).asInstanceOf[Byte]);
      // expect document removal
      mockCollaborator.documentRemoved("Document");
    }

    whenExecuting(mockCollaborator) {
      classUnderTest.addDocument("Document", new Array[Byte](0));
      assertTrue(classUnderTest.removeDocument("Document"));
    }
  }

  it should "call voteForRemoval on Collaborator when removeDocument is called on ClassTested, and " +
          "if a NEGATIVE number is returned (i.e., a vote AGAINST removal), documentRemoved " +
          "should NOT be called on Collaborator" in {

    expecting {
      // expect document addition
      mockCollaborator.documentAdded("Document");
      // expect to be asked to vote, and vote against it
      expectCall(mockCollaborator.voteForRemoval("Document")).andReturn((-42).asInstanceOf[Byte]); //
      // document removal is *not* expected
    }

    whenExecuting(mockCollaborator) {
      classUnderTest.addDocument("Document", new Array[Byte](0));
      assertFalse(classUnderTest.removeDocument("Document"));
    }
  }

  it should "call voteForRemoval on Collaborator when removeDocument is called on ClassTested " +
          "to remove multiple documents, and if a POSITIVE number is returned (i.e., a vote " +
          "FOR removal), documentRemoved should be called on Collaborator" in {

    expecting {
      mockCollaborator.documentAdded("Document 1");
      mockCollaborator.documentAdded("Document 2");
      val documents = Array("Document 1", "Document 2")
      expectCall(mockCollaborator.voteForRemovals(aryEq(documents))).andReturn((42).asInstanceOf[Byte]);
      mockCollaborator.documentRemoved("Document 1");
      mockCollaborator.documentRemoved("Document 2");
    }

    whenExecuting(mockCollaborator) {
      classUnderTest.addDocument("Document 1", new Array[Byte](0));
      classUnderTest.addDocument("Document 2", new Array[Byte](0));
      assertTrue(classUnderTest.removeDocuments(Array("Document 1",
              "Document 2")));
    }
  }

  it should "call voteForRemoval on Collaborator when removeDocument is called on ClassTested " +
          "to remove multiple documents, and if a NEGATIVE number is returned (i.e., a vote " +
          "AGAINST removal), documentRemoved should NOT be called on Collaborator" in {

    expecting {
      mockCollaborator.documentAdded("Document 1");
      mockCollaborator.documentAdded("Document 2");
      val documents = Array("Document 1", "Document 2")
      expectCall(mockCollaborator.voteForRemovals(aryEq(documents))).andReturn((-42).asInstanceOf[Byte]);
    }

    whenExecuting(mockCollaborator) {
      classUnderTest.addDocument("Document 1", new Array[Byte](0));
      classUnderTest.addDocument("Document 2", new Array[Byte](0));
      assertFalse(classUnderTest.removeDocuments(Array("Document 1",
              "Document 2")));
    }
  }

  "EasyMock" should "work with both andAnswer and andDelegateTo styles" in {
    
    val list = mock[List[String]]

    expecting {
      // andAnswer style
      expectCall(list.remove(10)).andAnswer(new IAnswer[String]() {
        def answer(): String = {
          return getCurrentArguments()(0).toString();
        }
      });

      // andDelegateTo style
      expectCall(list.remove(10)).andDelegateTo(new ArrayList[String]() {
        // private static final long serialVersionUID = 1L;

        override def remove(index: Int): String = {
          return Integer.toString(index);
        }
      });
    }
        
    whenExecuting(list) {
      assertEquals("10", list.remove(10));
      assertEquals("10", list.remove(10));
    }
  }
}
