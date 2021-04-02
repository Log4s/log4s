package org.log4s

inline def getLogger: Logger = ${LoggerMacros.getLoggerImpl}
def getLogger(name: String) = new Logger(org.slf4j.LoggerFactory.getLogger(name))
def getLogger(clazz: Class[_]) = new Logger(org.slf4j.LoggerFactory.getLogger(clazz))

// final val Log4sConfig: log4sjs.Log4sConfig = log4sjs.Log4sConfig
// final val AllThreshold = log4sjs.LogThreshold.AllThreshold
// final val OffThreshold = log4sjs.LogThreshold.OffThreshold
