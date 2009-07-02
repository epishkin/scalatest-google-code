package org.scalatest

import events.NameInfo
import java.util.concurrent.atomic.AtomicBoolean

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
class ConcurrentInformerSpec extends Spec {
  describe("A ConcurrentInformer") {
    val nameInfo = NameInfo("suite name", Some("suite.class.Name"), Some("test name"))
    val informer =
      new ConcurrentInformer(nameInfo) {
        def apply(message: String) = ()
      }
    it("should return the passed NameInfo in a Some when the constructing thread calls nameInfoForCurrentThread") {
      assert(informer.nameInfoForCurrentThread.isDefined)
      assert(informer.nameInfoForCurrentThread.get === nameInfo)
    }
    it("should return None when a thread other than the constructing thread calls nameInfoForCurrentThread") {
      val nameInfoWasNone = new AtomicBoolean
      class MyThread extends Thread {
        override def run() {
          nameInfoWasNone.set(!informer.nameInfoForCurrentThread.isDefined)
        }
      }
      val thread = new MyThread
      thread.start()
      thread.join()
      assert(nameInfoWasNone.get)
    }
  }
}