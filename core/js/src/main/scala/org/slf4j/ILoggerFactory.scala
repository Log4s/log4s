package org.slf4j

trait ILoggerFactory {
  def getLogger(name: String): Logger
}
