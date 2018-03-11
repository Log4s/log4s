package org.log4s

import scala.collection.JavaConverters._

import scala.scalajs.js
import js.Dynamic.{ global => g }

import org.log4s.log4sjs._

object PlatformInit extends IPlatformInit {
  private[this] final val showInit: Boolean = true
  def init(): Unit = {
    if (showInit) {
      g.console.log("Initializing Scala.JS platform")
    }
    val testAppender = new TestAppender
    testAppender.start()
    val log4sAppender: Log4sAppender = new js.Object with Log4sAppender {
      override def append(event: org.log4s.log4sjs.LoggedEvent): Unit = {
        import ch.qos.logback.classic.{ Level => LBLevel }
        import ch.qos.logback.classic.spi._
        val logbackEvent: ILoggingEvent = new ILoggingEvent {
          def getArgumentArray() = None.orNull
          def getCallerData() = None.orNull
          def getFormattedMessage() = event.message
          def getLevel() = event.level match {
            case Trace => LBLevel.TRACE
            case Debug => LBLevel.DEBUG
            case Info  => LBLevel.INFO
            case Warn  => LBLevel.WARN
            case Error => LBLevel.ERROR
          }
          def getLoggerName() = event.loggerName
          def getMDCPropertyMap() = event.mdc.asJava
          def getMessage() = event.message
          def getMarker() = None.orNull
          def getThreadName() = ""
          def getThrowableProxy() = {
            import ExceptionInfo._
            js.Dynamic.global.console.log("findme")
            val throwable = event.throwable match {
              case te: ThrowableException => Some(te.throwable)
              case _                      => None
            }
            makeThrowableProxy(throwable.orNull)
          }
          def getTimeStamp() = event.timestamp.getTime.toLong
          def hasCallerData() = false

          /* Note that the `throwable` parameter is permitted to be null */
          private[this] def makeThrowableProxy(throwable: Throwable): IThrowableProxy = {
            Option(throwable)
              .map { t =>
                new IThrowableProxy {
                  def getCause() = makeThrowableProxy(t.getCause)
                  def getClassName() = t.getClass.getName
                  /* TODO: Find the right value for this */
                  def getCommonFrames() = 0
                  def getMessage() = t.getMessage
                  def getStackTraceElementProxyArray() = {
                    t.getStackTrace .map { ste =>
                      new StackTraceElementProxy {
                        def getStackTraceElement() = ste
                        def getSTEAsString() = ste.toString
                      }
                    }
                  }
                  def getSuppressed() = Array.empty
                }
              }
              .orNull
          }
        }
        testAppender.append(logbackEvent)
      }
    }
    Log4sConfig.addCategoryAppender("test", log4sAppender)
  }
}
