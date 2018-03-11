package org.slf4j

object LoggerFactory extends org.log4s.log4sjs.Log4sLoggerFactory {
  def getLogger(c: Class[_]): Logger = {
    this.getLogger(c.getName())
  }
}
