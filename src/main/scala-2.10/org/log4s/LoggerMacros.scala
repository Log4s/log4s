package org.log4s

import language.experimental.macros

import scala.reflect.macros.Context

import org.slf4j.{ Logger => JLogger }
import org.slf4j.LoggerFactory.{ getLogger => getJLogger }

/** Macros that support the logging system.
  *
  * @author Sarah Gerweck <sarah@atscale.com>
  */
private[log4s] object LoggerMacros {
  /** Get a logger by reflecting the enclosing class name. */
  final def getLoggerImpl(c: Context): c.Expr[Logger] = {
    import c.universe._

    val cls = c.enclosingClass.symbol

    assert(cls.isModule || cls.isClass, "Enclosing class is always either a module or a class")

    def loggerBySymbolName(s: Symbol) = {
      def fullName(s: Symbol): String = {
        if (!(s.isModule || s.isClass)) {
          fullName(s.owner)
        } else if (!s.owner.isStatic) {
          fullName(s.owner) + "." + s.name.encodedName.toString
        } else {
          s.fullName
        }
      }
      reify { new Logger(getJLogger(c.literal(fullName(s)).splice)) }
    }

    def loggerByType(s: Symbol) = {
      val tp = if (cls.isModule) cls.asModule.moduleClass else cls

      val expr = c.Expr[Class[_]](Literal(Constant(tp.asType.toTypeConstructor)))
      reify { new Logger(getJLogger(expr.splice)) }
    }

    @inline def isInnerClass(s: Symbol) = {
      s.isClass && !(s.owner.isPackage)
    }

    val instanceByName = Logger.singletonsByName && cls.isModule || cls.isClass && isInnerClass(cls)

    if (instanceByName) {
      loggerBySymbolName(cls)
    } else {
      loggerByType(cls)
    }
  }

  /** A macro context that represents a method call on a Logger instance. */
  private[this] type LogCtx = Context { type PrefixType = Logger }

  /** Log a message reflectively at a given level.
    *
    * This is the internal workhorse method that does most of the logging for real applications.
    *
    * @param msg the message that the user wants to log
    * @param error the `Throwable` that we're logging along with the message, if any
    * @param logLevel the level of the logging
    */
  @inline private[this] def reflectiveLog(c: LogCtx)(msg: c.Expr[String], error: Option[c.Expr[Throwable]])(logLevel: LogLevel) = {
    import c.universe._

    val logger = Select(c.prefix.tree, newTermName("logger"))
    val logValues = error match {
      case None    => List(msg.tree)
      case Some(e) => List(msg.tree, e.tree)
    }
    val logExpr = c.Expr[Unit](Apply(Select(logger, newTermName(logLevel.methodName)), logValues))
    def checkExpr = c.Expr[Boolean](Apply(Select(logger, newTermName(s"is${logLevel.name}Enabled")), Nil))

    def errorIsSimple = {
      error match {
        case None | Some(c.Expr(Ident(_))) => true
        case _                             => false
      }
    }

    msg match {
      case c.Expr(Literal(Constant(_))) if errorIsSimple =>
        logExpr
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
