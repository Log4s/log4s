package org.log4s

import ch.qos.logback.classic.spi.IThrowableProxy

final class LoggedThrowable private[log4s] (val inner: IThrowableProxy) extends AnyVal {
  def cause: Option[LoggedThrowable] = Option(inner.getCause).map(new LoggedThrowable(_))
  def className: String = inner.getClassName
  def commonFrames: Int = inner.getCommonFrames
  def message: Option[String] = Option(inner.getMessage)
  def stackTrace: IndexedSeq[StackTraceElement] = inner.getStackTraceElementProxyArray.map(_.getStackTraceElement)
}
