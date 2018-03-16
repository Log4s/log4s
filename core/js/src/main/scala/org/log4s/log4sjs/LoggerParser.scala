package org.log4s.log4sjs

import scala.collection.{ immutable, mutable }

private[log4sjs] object LoggerParser {
  def apply(loggerName: String): immutable.Seq[String] = {
    val part = new mutable.StringBuilder()
    val result = immutable.Seq.newBuilder[String]
    val len = loggerName.length
    var i = 0
    while (i < len) {
      loggerName(i) match {
        case '\\' if i < len - 1 =>
          loggerName(i + 1) match {
            case c@('\\' | '.') =>
              part += c
              i += 1
            case _ =>
              /* Don't recognize escape sequence, keep the backslash */
              part += '\\'
          }
        case '.' if i < len - 1 =>
          result += part.result()
          part.clear()
        case c =>
          part += c
      }
      i += 1
    }
    if (part.nonEmpty) {
      result += part.result()
    }
    result.result()
  }
}
