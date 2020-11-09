package org.log4s

import scala.language.experimental.macros

def getLogger: Logger = getLogger(???.asInstanceOf[String])
def getLogger(name: String) = new Logger(org.slf4j.LoggerFactory.getLogger(name))
def getLogger(clazz: Class[_]) = new Logger(org.slf4j.LoggerFactory.getLogger(clazz))
