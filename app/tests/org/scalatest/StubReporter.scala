package org.scalatest

import org.scalatest.events.Event

object StubReporter extends Reporter {
  override def apply(event: Event) {
    super.apply(event)
  }
}
