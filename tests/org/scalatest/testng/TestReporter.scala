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
package org.scalatest.testng;


/**
 * This class only exists because I cant get jmock to work with Scala. 
 * Other people seem to do it. Frustrating. 
 */
class TestReporter extends Reporter{

  var report: Report = null;
  var successCount = 0;
  var failureCount = 0;
  
  var ignoreReport: Report = null;
  var ignoreCount = 0;
  
  override def testSucceeded(report: Report){ 
    successCount = successCount + 1 
    this.report = report;
  }
  
  override def testFailed(report: Report){ 
    failureCount = failureCount + 1 
    this.report = report;
  }

  override def testIgnored(report: Report){ 
    ignoreCount = ignoreCount + 1 
    this.report = report;
  }
  
  def errorMessage = report.throwable.get.getMessage
  
}
