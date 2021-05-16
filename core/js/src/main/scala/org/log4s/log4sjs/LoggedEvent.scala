package org.log4s
package log4sjs

import scala.scalajs.js

trait LoggedEvent extends js.Any {
  def loggerPath: Seq[String]
  def level: LogLevel
  def message: String
  def mdc: Map[String, String]
  def threadName: String
  def throwable: ExceptionInfo
  def timestamp: js.Date
  def loggerName: String
}

final class Log4sEvent private[log4sjs] (
    val loggerName: String,
    val loggerPath: Seq[String],
    val level: LogLevel,
    val message: String,
    val mdc: Map[String, String],
    val threadName: String,
    val throwable: ExceptionInfo,
    val timestamp: js.Date)
  extends js.Object with LoggedEvent {

  override def toString() = s"Log4sEvent(loggerName=$loggerName, level=$level, message=$message, mdc=$mdc, threadName=$threadName, throwable=$throwable, timestamp=$timestamp)"
}
