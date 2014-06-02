package org

import language.experimental.macros

package object log4s {
  def getLogger: org.log4s.Logger = macro LoggerMacros.getLoggerImpl
  def getLogger(name: String) = new Logger(org.slf4j.LoggerFactory.getLogger(name))
  def getLogger(clazz: Class[_]) = new Logger(org.slf4j.LoggerFactory.getLogger(clazz))
}
