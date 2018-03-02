package org.log4s

import scala.collection.Map
import scala.collection.JavaConverters._
import org.slf4j.Marker

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi._

/** A Scala-centric view of a Logback logging event. */
final class LoggedEvent private[log4s] (val inner: ILoggingEvent) extends AnyVal {
  /** Get the argument array.
    *
    * Log4s doesn't use arguments, so you probably don't want this unless
    * you're also testing non-Log4s logging events. */
  def argumentArray: Option[IndexedSeq[Any]] = Option(inner.getArgumentArray)

  def callerData: IndexedSeq[StackTraceElement] = inner.getCallerData
  def formattedMessage: String = inner.getFormattedMessage
  /* TODO: Convert these to Log4s levels? This would require some clever
   * tricks to deal with the circular dependency it would create. */
  def level: Level = inner.getLevel
  def loggerName: String = inner.getLoggerName
  def marker: Option[Marker] = Option(inner.getMarker)
  def mdc: Map[String, String] = inner.getMDCPropertyMap.asScala
  def message: String = inner.getMessage
  def threadName: String = inner.getThreadName
  def throwable: Option[LoggedThrowable] = Option(inner.getThrowableProxy).map(new LoggedThrowable(_))
  def timestamp: Long = inner.getTimeStamp
  def hasCallerData: Boolean = inner.hasCallerData

}
