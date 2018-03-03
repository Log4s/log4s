package org.log4s
package log4sjs

import scala.scalajs.js

sealed abstract class ExceptionInfo extends js.Object
object ExceptionInfo {
  def apply(t: Throwable): ExceptionInfo = {
    Option(t).map(new ThrowableException(_)).getOrElse(NoException)
  }
  def apply(e: js.Error): ExceptionInfo = {
    Option(e).map(new JsErrorException(_)).getOrElse(NoException)
  }

  object NoException extends ExceptionInfo
  class ThrowableException(val throwable: Throwable) extends ExceptionInfo
  class JsErrorException(val error: js.Error) extends ExceptionInfo
}
