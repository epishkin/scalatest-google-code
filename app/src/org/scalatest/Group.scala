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
 * Abstract class whose subclasses can be passed to <code>FunSuite</code> and <code>Spec</code> test
 * registration methods to place tests into groups. For example, if you define:
 * <pre>
 * object SlowTest extends Group("SlowTest")
 * </pre>
 *
 * then you can place a test into the <code>SlowTest</code> group like this:
 * <pre>
 * import org.scalatest.FunSuite
 *
 * class MySuite extends FunSuite {
 *
 *   test("my test", SlowTest) {
 *     Thread.sleep(1000)
 *   }
 * }
 * </pre>
 *
 * <p>
 * or
 * </p>
 *
 * <pre>
 * import org.scalatest.Spec
 *
 * class MySpec extends Spec {
 *
 *   it("should sleep for a second", SlowTest) {
 *     Thread.sleep(1000)
 *   }
 * }
 * </pre>

 * If you have created Java annotation interfaces for use as group names in direct subclasses of <code>org.scalatest.Suite</code>,
 * then you will probably want to use group names on your <code>FunSuite</code>s that match. To do so, simply 
 * pass the fully qualified names of the Java interfaces to the <code>Group</code> constructor. For example, if you've
 * defined a Java annotation interface with fully qualified name, <code>com.mycompany.groups.SlowTest</code>, then you could
 * create a matching group for <code>FunSuite</code>s like this:
 * <pre>
 * object SlowTest extends Group("com.mycompany.groups.SlowTest")
 * </pre>
 *
 * @author Bill Venners
 */
abstract class Group(val name: String)

