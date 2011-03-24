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
package org.scalatest.prop

import org.scalacheck.Test.Params

private[prop] object Helper {

  def getParams(
    configParams: Seq[PropertyCheckConfigParam],
    config: PropertyCheckConfig
  ): Params = {

    var minSuccessful = -1
    var maxSkipped = -1
    var minSize = -1
    var maxSize = -1
    var workers = -1

    var minSuccessfulTotalFound = 0
    var maxSkippedTotalFound = 0
    var minSizeTotalFound = 0
    var maxSizeTotalFound = 0
    var workersTotalFound = 0

    for (configParam <- configParams) {
      configParam match {
        case param: MinSuccessful =>
          minSuccessful = param.value
          minSuccessfulTotalFound += 1
        case param: MaxSkipped =>
          maxSkipped = param.value
          maxSkippedTotalFound += 1
        case param: MinSize =>
          minSize = param.value
          minSizeTotalFound += 1
        case param: MaxSize =>
          maxSize = param.value
          maxSizeTotalFound += 1
        case param: Workers =>
          workers = param.value
          workersTotalFound += 1
      }
    }
  
    if (minSuccessfulTotalFound > 1)
      throw new IllegalArgumentException("can pass at most MinSuccessful config parameters, but " + minSuccessfulTotalFound + " were passed")
    if (maxSkippedTotalFound > 1)
      throw new IllegalArgumentException("can pass at most MaxSkipped config parameters, but " + maxSkippedTotalFound + " were passed")
    if (minSizeTotalFound > 1)
      throw new IllegalArgumentException("can pass at most MinSize config parameters, but " + minSizeTotalFound + " were passed")
    if (maxSizeTotalFound > 1)
      throw new IllegalArgumentException("can pass at most MaxSize config parameters, but " + maxSizeTotalFound + " were passed")
    if (workersTotalFound > 1)
      throw new IllegalArgumentException("can pass at most Workers config parameters, but " + workersTotalFound + " were passed")

    Params(
      if (minSuccessful != -1) minSuccessful else config.minSuccessful,
      if (maxSkipped != -1) maxSkipped else config.maxSkipped,
      if (minSize != -1) minSize else config.minSize,
      if (maxSize != -1) maxSize else config.maxSize,
      Params().rng,
      if (workers != -1) workers else config.workers,
      Params().testCallback
    )
  }
}
