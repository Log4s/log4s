package org.log4s
package log4sjs

import scala.scalajs.js
import js.Dynamic.{ global => g }

class Log4sConsoleAppender(val formatter: MessageFormatter = new StandardMessageFormatter()) extends js.Object with Log4sAppender {
  def append(event: LoggedEvent): Unit = {
    val formatted = formatter.render(event)
    event.level match {
      case Trace | Debug | Info =>
        g.console.log(formatted)
      case Warn =>
        g.console.warn(formatted)
      case Error =>
        g.console.error(formatted)
    }
  }
}
