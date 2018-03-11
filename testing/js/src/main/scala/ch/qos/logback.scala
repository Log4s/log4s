package ch.qos.logback

package core {
  class AppenderBase[A] {
    def start(): Unit = {}
    def stop(): Unit = {}
    def append(event: A): Unit = {}
  }
}

package classic {
  final class Level(val levelInt: Int, val levelStr: String) {
    override def toString: String = levelStr
    def toInt: Int = levelInt
    def toInteger: java.lang.Integer = toInt
    def isGreaterOrEqual(l: Level) = toInt >= l.toInt
  }
  object Level {
    val ALL = new Level(Int.MinValue, "ALL")
    val TRACE = new Level(5000, "TRACE")
    val DEBUG = new Level(10000, "DEBUG")
    val INFO = new Level(20000, "INFO")
    val WARN = new Level(30000, "WARN")
    val ERROR = new Level(40000, "ERROR")
    val OFF = new Level(Int.MaxValue, "OFF")
  }

  package spi {
    trait IThrowableProxy {
      def getCause(): IThrowableProxy
      def getClassName(): String
      def getCommonFrames(): Int
      def getMessage(): String
      def getStackTraceElementProxyArray(): Array[StackTraceElementProxy]
      def getSuppressed(): Array[IThrowableProxy]
    }
    trait StackTraceElementProxy {
      def getStackTraceElement(): StackTraceElement
      def getSTEAsString(): String
    }
    trait ILoggingEvent {
      def getArgumentArray(): Array[Any]
      def getCallerData(): Array[StackTraceElement]
      def getFormattedMessage(): String
      def getLevel(): Level
      def getLoggerName(): String
      def getMDCPropertyMap(): java.util.Map[String, String]
      def getMessage(): String
      def getMarker(): org.slf4j.Marker
      def getThreadName(): String
      def getThrowableProxy(): IThrowableProxy
      def getTimeStamp(): Long
      def hasCallerData(): Boolean
    }
  }
}
