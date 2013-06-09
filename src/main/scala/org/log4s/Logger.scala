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
  
  sealed trait LevelLogger extends Any {
    def isEnabled: Boolean
    def apply(msg: => String): Unit
    def apply(t: Throwable)(msg: => String)
  }

  final class TraceLevelLogger private[log4s](val logger: JLogger) extends AnyVal with LevelLogger {
    @inline def isEnabled = logger.isTraceEnabled
    @inline def apply(msg: => String) = if (isEnabled) logger.trace(msg)
    @inline def apply(t: Throwable)(msg: => String) = if (isEnabled) logger.trace(msg, t)
  }
  
  final class DebugLevelLogger private[log4s](val logger: JLogger) extends AnyVal with LevelLogger {
    @inline def isEnabled = logger.isDebugEnabled
    @inline def apply(msg: => String) = if (isEnabled) logger.debug(msg)
    @inline def apply(t: Throwable)(msg: => String) = if (isEnabled) logger.debug(msg, t)
  }
  
  final class InfoLevelLogger private[log4s](val logger: JLogger) extends AnyVal with LevelLogger {
    @inline def isEnabled = logger.isInfoEnabled
    @inline def apply(msg: => String) = if (isEnabled) logger.info(msg)
    @inline def apply(t: Throwable)(msg: => String) = if (isEnabled) logger.info(msg, t)
  }
  
  final class WarnLevelLogger private[log4s](val logger: JLogger) extends AnyVal with LevelLogger {
    @inline def isEnabled = logger.isWarnEnabled
    @inline def apply(msg: => String) = if (isEnabled) logger.warn(msg)
    @inline def apply(t: Throwable)(msg: => String) = if (isEnabled) logger.warn(msg, t)
  }
  
  final class ErrorLevelLogger private[log4s](val logger: JLogger) extends AnyVal with LevelLogger {
    @inline def isEnabled = logger.isErrorEnabled
    @inline def apply(msg: => String) = if (isEnabled) logger.error(msg)
    @inline def apply(t: Throwable)(msg: => String) = if (isEnabled) logger.error(msg, t)
  }
}

final class Logger(val logger: JLogger) extends AnyVal {
  @inline def isTraceEnabled: Boolean = logger.isTraceEnabled
  
  @inline def isDebugEnabled: Boolean = logger.isDebugEnabled
  
  @inline def isInfoEnabled: Boolean = logger.isInfoEnabled
  
  @inline def isWarnEnabled: Boolean = logger.isWarnEnabled
  
  @inline def isErrorEnabled: Boolean = logger.isErrorEnabled
  
  
  import Logger._
  
  /* These will allow maximum inlining if the type is known at compile time. */
  @inline def apply(lvl: Trace.type): TraceLevelLogger = new TraceLevelLogger(logger)
  @inline def apply(lvl: Debug.type): DebugLevelLogger = new DebugLevelLogger(logger)
  @inline def apply(lvl: Info .type): InfoLevelLogger  = new InfoLevelLogger (logger)
  @inline def apply(lvl: Warn .type): WarnLevelLogger  = new WarnLevelLogger (logger)
  @inline def apply(lvl: Error.type): ErrorLevelLogger = new ErrorLevelLogger(logger)
  
  def apply(level: LogLevel): LevelLogger = level match {
    case Trace => new TraceLevelLogger(logger)
    case Debug => new DebugLevelLogger(logger)
    case Info  => new InfoLevelLogger(logger)
    case Warn  => new WarnLevelLogger(logger)
    case Error => new ErrorLevelLogger(logger)
  }
  
  import LoggerMacros._
  
  def trace(t: Throwable)(msg: String) = macro traceTM
  def trace(msg: String) = macro traceM
  
  def debug(t: Throwable)(msg: String) = macro debugTM
  def debug(msg: String) = macro debugM
  
  def info(t: Throwable)(msg: String) = macro infoTM
  def info(msg: String) = macro infoM
  
  def warn(t: Throwable)(msg: String) = macro warnTM
  def warn(msg: String) = macro warnM
  
  def error(t: Throwable)(msg: String) = macro errorTM
  def error(msg: String) = macro errorM
  
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
  
  @inline private[this] def reflectiveLog(c: LogCtx)(msg: c.Expr[String], error: Option[c.Expr[Throwable]])(logLevel: LogLevel) = {
    import c.universe._
        
    val logger = Select(c.prefix.tree, newTermName("logger"))
    val logValues = error match {
      case None    => List(msg.tree)
      case Some(e) => List(msg.tree, e.tree)
    }
    val logExpr = c.Expr[Unit](Apply(Select(logger, newTermName(logLevel.methodName)), logValues))
    @inline def checkExpr = c.Expr[Boolean](Apply(Select(logger, newTermName(s"is${logLevel.name}Enabled")), Nil))
    
    msg match {
      case c.Expr(Literal(Constant(_))) => logExpr
      case _ =>
        reify { if (checkExpr.splice) logExpr.splice }  
    }
  }
  
  def traceTM(c: LogCtx)(t: c.Expr[Throwable])(msg: c.Expr[String]): c.Expr[Unit] = reflectiveLog(c)(msg, Some(t))(Trace)
  def traceM(c: LogCtx)(msg: c.Expr[String]): c.Expr[Unit] = reflectiveLog(c)(msg, None)(Trace)
  
  def debugTM(c: LogCtx)(t: c.Expr[Throwable])(msg: c.Expr[String]): c.Expr[Unit] = reflectiveLog(c)(msg, Some(t))(Debug)
  def debugM(c: LogCtx)(msg: c.Expr[String]): c.Expr[Unit] = reflectiveLog(c)(msg, None)(Debug)
  
  def infoTM(c: LogCtx)(t: c.Expr[Throwable])(msg: c.Expr[String]): c.Expr[Unit] = reflectiveLog(c)(msg, Some(t))(Info)
  def infoM(c: LogCtx)(msg: c.Expr[String]): c.Expr[Unit] = reflectiveLog(c)(msg, None)(Info)
  
  def warnTM(c: LogCtx)(t: c.Expr[Throwable])(msg: c.Expr[String]): c.Expr[Unit] = reflectiveLog(c)(msg, Some(t))(Warn)
  def warnM(c: LogCtx)(msg: c.Expr[String]): c.Expr[Unit] = reflectiveLog(c)(msg, None)(Warn)
  
  def errorTM(c: LogCtx)(t: c.Expr[Throwable])(msg: c.Expr[String]): c.Expr[Unit] = reflectiveLog(c)(msg, Some(t))(Error)
  def errorM(c: LogCtx)(msg: c.Expr[String]): c.Expr[Unit] = reflectiveLog(c)(msg, None)(Error)
  
}
