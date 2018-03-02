package org.log4s

import scala.collection.mutable
import scala.util._

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
    super.start()
    newQueue()
  }

  override def stop(): Unit = {
    resetQueue()
    super.stop()
  }

  override def append(event: ILoggingEvent): Unit = {
    addEvent(event)
  }
}

object TestAppender {
  private var loggingEvents: Option[mutable.Queue[LoggedEvent]] = None

  @inline private[this] def events = {
    require(loggingEvents.isDefined, "Illegal operation with no active queue")
    loggingEvents.get
  }

  private def addEvent(event: ILoggingEvent): Unit = synchronized {
    events += new LoggedEvent(event)
  }

  def dequeue: Option[LoggedEvent] = synchronized {
    Try(events.dequeue).toOption
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

  /** Run a block of code while the provided appender capturing the events
    *
    * Only one of these blocks can be active at a time. The system will impose synchronization to enforce this,
    * so you may have deadlocks if you have several of these blocks running concurrently and producing values for one another.
    *
    * This will only allow you to dequeue events from the same thread that called `withAppender`, so you should
    * use `Await` if you are testing asynchronous code (or you can manage the queue yourself).
   */
  def withAppender[A](mustStartEmpty: Boolean = true, mustEndEmpty: Boolean = false)(f: => A): A = synchronized {
    if (!mustStartEmpty) {
      resetQueue()
    }
    newQueue()
    val result = Try(f)
    val endSize = events.size
    resetQueue()
    result
      .map { r =>
        if (mustEndEmpty && endSize != 0) {
          throw new IllegalStateException(s"Expected to code to consume all log elements, but $endSize were left")
        }
        r
      }
      .get
  }
}
