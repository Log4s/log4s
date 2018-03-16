package org.log4s
package log4sjs

import scala.scalajs.js

import org.slf4j.{ ILoggerFactory, Logger }

class Log4sLoggerFactory extends ILoggerFactory {
  @inline private final def config = Log4sConfig
  private[this] final class Log4sLoggerInstance private[Log4sLoggerFactory] (private[this] val name: String) extends Logger {
    private[this] val path = LoggerParser(name)

    override def getName = name

    protected[this] def isEnabled(level: LogLevel) = config.isPathEnabled(path, level)

    protected[this] def doLog(level: LogLevel, message: String, throwable: ExceptionInfo = ExceptionInfo.NoException): Unit = {
      val event = new Log4sEvent(name, path, level, message, org.slf4j.MDC.asScala(), Thread.currentThread.getName, throwable, new js.Date)
      config.doLog(event)
    }

    override def isTraceEnabled() = isEnabled(Trace)
    override def trace(message: String) = doLog(Trace, message)
    override def trace(message: String, t: Throwable) = doLog(Trace, message, ExceptionInfo(t))
    override def trace(message: String, e: js.Error) = doLog(Trace, message, ExceptionInfo(e))

    override def isDebugEnabled() = isEnabled(Debug)
    override def debug(message: String) = doLog(Debug, message)
    override def debug(message: String, t: Throwable) = doLog(Debug, message, ExceptionInfo(t))
    override def debug(message: String, e: js.Error) = doLog(Debug, message, ExceptionInfo(e))

    override def isInfoEnabled() = isEnabled(Info)
    override def info(message: String) = doLog(Info, message)
    override def info(message: String, t: Throwable) = doLog(Info, message, ExceptionInfo(t))
    override def info(message: String, e: js.Error) = doLog(Info, message, ExceptionInfo(e))

    override def isWarnEnabled() = isEnabled(Warn)
    override def warn(message: String) = doLog(Warn, message)
    override def warn(message: String, t: Throwable) = doLog(Warn, message, ExceptionInfo(t))
    override def warn(message: String, e: js.Error) = doLog(Warn, message, ExceptionInfo(e))

    override def isErrorEnabled() = isEnabled(Error)
    override def error(message: String) = doLog(Error, message)
    override def error(message: String, t: Throwable) = doLog(Error, message, ExceptionInfo(t))
    override def error(message: String, e: js.Error) = doLog(Error, message, ExceptionInfo(e))
  }
  override def getLogger(name: String): Logger = new Log4sLoggerInstance(name)
}
