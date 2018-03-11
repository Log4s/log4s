package org.slf4j

import scala.scalajs.js
import js.annotation._

/** A Scala reimplementation of the SLF4J API interface.
  *
  * This is not meant to act as a replacement for SLF4J. Rather, its intended use is for non-JVM
  * runtimes like ScalaJS and Scala Native where SLF4J itself is not available. */
@JSExportAll
abstract class Logger {
  def getName(): String

  def isTraceEnabled(): Boolean
  def trace(message: String): Unit
  def trace(message: String, t: Throwable): Unit
  def trace(message: String, e: js.Error): Unit

  def isDebugEnabled(): Boolean
  def debug(message: String): Unit
  def debug(message: String, t: Throwable): Unit
  def debug(message: String, e: js.Error): Unit

  def isInfoEnabled(): Boolean
  def info(message: String): Unit
  def info(message: String, t: Throwable): Unit
  def info(message: String, e: js.Error): Unit

  def isWarnEnabled(): Boolean
  def warn(message: String): Unit
  def warn(message: String, t: Throwable): Unit
  def warn(message: String, e: js.Error): Unit

  def isErrorEnabled(): Boolean
  def error(message: String): Unit
  def error(message: String, t: Throwable): Unit
  def error(message: String, e: js.Error): Unit
}
