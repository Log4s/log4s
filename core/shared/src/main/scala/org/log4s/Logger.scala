package org.log4s

import language.experimental.macros

import org.slf4j.{ Logger => JLogger }

object Logger {
  final val singletonsByName = true
  final val trailingDollar = false

  sealed trait LevelLogger extends Any {
    def isEnabled: Boolean
    def apply(msg: => String): Unit
    def apply(t: Throwable)(msg: => String): Unit
  }

  final class TraceLevelLogger private[log4s](val logger: JLogger) extends AnyVal with LevelLogger {
    @inline def isEnabled = logger.isTraceEnabled
    @inline def apply(msg: => String) = if (isEnabled) logger.trace(msg)
    @inline def apply(t: Throwable)(msg: => String) = if (isEnabled) logger.trace(msg, t)
  }

  final class DebugLevelLogger private[log4s](val logger: JLogger) extends AnyVal with LevelLogger {
    @inline def isEnabled = logger.isDebugEnabled
    @inline def apply(msg: => String) = if (isEnabled) logger.debug(msg)
    @inline def apply(t: Throwable)(msg: => String) = if (isEnabled) logger.debug(msg, t)
  }

  final class InfoLevelLogger private[log4s](val logger: JLogger) extends AnyVal with LevelLogger {
    @inline def isEnabled = logger.isInfoEnabled
    @inline def apply(msg: => String) = if (isEnabled) logger.info(msg)
    @inline def apply(t: Throwable)(msg: => String) = if (isEnabled) logger.info(msg, t)
  }

  final class WarnLevelLogger private[log4s](val logger: JLogger) extends AnyVal with LevelLogger {
    @inline def isEnabled = logger.isWarnEnabled
    @inline def apply(msg: => String) = if (isEnabled) logger.warn(msg)
    @inline def apply(t: Throwable)(msg: => String) = if (isEnabled) logger.warn(msg, t)
  }

  final class ErrorLevelLogger private[log4s](val logger: JLogger) extends AnyVal with LevelLogger {
    @inline def isEnabled = logger.isErrorEnabled
    @inline def apply(msg: => String) = if (isEnabled) logger.error(msg)
    @inline def apply(t: Throwable)(msg: => String) = if (isEnabled) logger.error(msg, t)
  }
}

final class Logger(val logger: JLogger) extends AnyVal with LoggerCompat {
  /** The name of this logger. */
  @inline def name = logger.getName

  @inline def isTraceEnabled: Boolean = logger.isTraceEnabled

  @inline def isDebugEnabled: Boolean = logger.isDebugEnabled

  @inline def isInfoEnabled: Boolean = logger.isInfoEnabled

  @inline def isWarnEnabled: Boolean = logger.isWarnEnabled

  @inline def isErrorEnabled: Boolean = logger.isErrorEnabled


  import Logger._

  /* These will allow maximum inlining if the type is known at compile time. */
  @inline def apply(lvl: Trace.type): TraceLevelLogger = new TraceLevelLogger(logger)
  @inline def apply(lvl: Debug.type): DebugLevelLogger = new DebugLevelLogger(logger)
  @inline def apply(lvl: Info .type): InfoLevelLogger  = new InfoLevelLogger (logger)
  @inline def apply(lvl: Warn .type): WarnLevelLogger  = new WarnLevelLogger (logger)
  @inline def apply(lvl: Error.type): ErrorLevelLogger = new ErrorLevelLogger(logger)

  def apply(level: LogLevel): LevelLogger = level match {
    case Trace => new TraceLevelLogger(logger)
    case Debug => new DebugLevelLogger(logger)
    case Info  => new InfoLevelLogger(logger)
    case Warn  => new WarnLevelLogger(logger)
    case Error => new ErrorLevelLogger(logger)
  }
}
