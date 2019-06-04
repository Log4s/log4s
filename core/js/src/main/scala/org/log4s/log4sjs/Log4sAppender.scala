package org.log4s
package log4sjs

import scala.scalajs.js

trait Log4sAppender extends js.Any {
  def append(event: LoggedEvent): Unit
}

object Log4sAppender extends FunctionalType[Log4sAppender, LoggedEvent, Unit]("Appender", Symbol("append")) {
  protected[this] def fromFunction(fn: LoggedEvent => Unit) = {
    new js.Object with Log4sAppender {
      override def append(le: LoggedEvent): Unit = { fn(le) }
    }
  }

  def consoleAppender[A: MessageFormatter.Provider](mf: A): Log4sAppender = {
    new Log4sConsoleAppender(MessageFormatter.from(mf))
  }
}
