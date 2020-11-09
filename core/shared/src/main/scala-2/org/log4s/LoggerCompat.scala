package org.log4s

import language.experimental.macros

import scala.annotation.tailrec
import scala.reflect.macros.{ blackbox, whitebox }

/** Support for cross-building Logger across Scala 2 and Scala 3
  */
private[log4s] trait LoggerCompat extends Any {
  import LoggerMacros._

  def trace(t: Throwable)(msg: String): Unit = macro traceTM
  def trace(msg: String): Unit = macro traceM

  def debug(t: Throwable)(msg: String): Unit = macro debugTM
  def debug(msg: String): Unit = macro debugM

  def info(t: Throwable)(msg: String): Unit = macro infoTM
  def info(msg: String): Unit = macro infoM

  def warn(t: Throwable)(msg: String): Unit = macro warnTM
  def warn(msg: String): Unit = macro warnM

  def error(t: Throwable)(msg: String): Unit = macro errorTM
  def error(msg: String): Unit = macro errorM
}
