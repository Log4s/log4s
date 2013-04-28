package org.log4s

import language.experimental.macros

import java.util.{ Map => JMap }

import scala.collection.JavaConversions._
import scala.reflect.macros.Context

import org.slf4j.{ Logger => JLogger }
import org.slf4j.LoggerFactory.{ getLogger => getJLogger }

object Logger {
  final val singletonsByName = true
  final val trailingDollar = false
  
  @deprecated("0.1", "Use org.log4s.getLogger")
  def getLogger: Logger = macro LoggerMacros.getLoggerImpl
}

private object LoggerMacros {
  final def getLoggerImpl(c: Context): c.Expr[Logger] = {
    import c.universe._
    
    val cls = c.enclosingClass.symbol

    if (Logger.singletonsByName) {
      if (cls.isModule) {
        val name = c.literal(cls.fullName)
        return reify { new Logger(getJLogger(name.splice)) }
      }
    }
    
    assert(cls.isModule || cls.isClass, "Enclosing class is always either a module or a class")
    
    val tp = if (cls.isModule) cls.asModule.moduleClass else cls
    
    val expr = c.Expr[Class[_]](Literal(Constant(tp.asType.toTypeConstructor)))
    reify { new Logger(getJLogger(expr.splice)) }
  }
  
  
  private[this] type LogCtx = Context { type PrefixType = Logger }
  
  @inline private[this] def reflectiveLog(c: LogCtx)(msg: c.Expr[String])(logLevel: String) = {
    import c.universe._
        
    val logger = Select(c.prefix.tree, newTermName("logger"))
    val logExpr = c.Expr[Unit](Apply(Select(logger, newTermName(logLevel)), List(msg.tree)))
    @inline def checkExpr = c.Expr[Boolean](Apply(Select(logger, newTermName(s"is${logLevel.capitalize}Enabled")), Nil))
    
    msg match {
      case c.Expr(Literal(Constant(_))) => logExpr
      case _ =>
        reify { if (checkExpr.splice) logExpr.splice }  
    }
  }
  
  final def traceM(c: LogCtx)(msg: c.Expr[String]): c.Expr[Unit] = reflectiveLog(c)(msg)("trace")
  final def debugM(c: LogCtx)(msg: c.Expr[String]): c.Expr[Unit] = reflectiveLog(c)(msg)("debug")
  final def infoM(c: LogCtx)(msg: c.Expr[String]): c.Expr[Unit] = reflectiveLog(c)(msg)("info")
  final def warnM(c: LogCtx)(msg: c.Expr[String]): c.Expr[Unit] = reflectiveLog(c)(msg)("warn")
  final def errorM(c: LogCtx)(msg: c.Expr[String]): c.Expr[Unit] = reflectiveLog(c)(msg)("error")
  
}

final class Logger(val logger: JLogger) extends AnyVal {
  @inline def isTraceEnabled: Boolean = logger.isTraceEnabled
  
  @inline def isDebugEnabled: Boolean = logger.isDebugEnabled
  
  @inline def isInfoEnabled: Boolean = logger.isInfoEnabled
  
  @inline def isWarnEnabled: Boolean = logger.isWarnEnabled
  
  @inline def isErrorEnabled: Boolean = logger.isErrorEnabled
  
  import LoggerMacros._
  
  def trace(msg: String) = macro traceM
  
  def debug(msg: String) = macro debugM
  
  def info(msg: String) = macro infoM
  
  def warn(msg: String) = macro warnM
  
  def error(msg: String) = macro errorM
  
}