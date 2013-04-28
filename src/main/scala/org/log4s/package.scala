package org

import language.experimental.macros

package object log4s {
  def getLogger = macro LoggerMacros.getLoggerImpl
}