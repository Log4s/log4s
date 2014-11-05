package org.log4s

import scala.collection.mutable

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase

/** A custom appender for Logback that captures the events.
  *
  * The test suite can use this to make sure all log events are recorded as expected.
  *
  * @author Sarah Gerweck <sarah@atscale.com>
  */
class TestAppender extends AppenderBase[ILoggingEvent] {
  import TestAppender._

  override def start(): Unit = {
    newQueue()
  }

  override def stop(): Unit = {
    resetQueue()
  }

  // XXX: This shouldn't be necessary.
  override def doAppend(event: ILoggingEvent): Unit = append(event)

  override def append(event: ILoggingEvent): Unit = {
    addEvent(event)
  }
}

object TestAppender {
  private var loggingEvents: Option[mutable.Queue[ILoggingEvent]] = None

  @inline private[this] def events = {
    require(loggingEvents.isDefined, "Illegal operation with no active queue")
    loggingEvents.get
  }

  private def addEvent(event: ILoggingEvent): Unit = synchronized {
    events += event
  }

  def dequeue: ILoggingEvent = synchronized {
    events.dequeue
  }

  private def newQueue(): Unit = synchronized {
    loggingEvents match {
      case Some(_) =>
        throw new IllegalStateException("Can't have multiple test appenders")
      case None =>
        loggingEvents = Some(mutable.Queue.empty)
    }
  }

  private def resetQueue(): Unit = synchronized {
    loggingEvents = None
  }
}