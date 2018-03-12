package org.log4s.log4sjs

import scala.collection.{ immutable, mutable }

private[log4sjs] object CategoryParser {
  def apply(category: String): immutable.Seq[String] = {
    val part = new mutable.StringBuilder()
    val result = immutable.Seq.newBuilder[String]
    val len = category.length
    var i = 0
    while (i < len) {
      category(i) match {
        case '\\' if i < len - 1 =>
          category(i + 1) match {
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
