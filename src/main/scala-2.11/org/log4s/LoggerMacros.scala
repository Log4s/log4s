package org.log4s

import language.experimental.macros

import scala.reflect.macros.{ blackbox, whitebox }

/** Macros that support the logging system.
  *
  * @author Sarah Gerweck <sarah@atscale.com>
  */
private[log4s] object LoggerMacros {

  /** Get a logger by reflecting the enclosing class name. */
  final def getLoggerImpl(c: blackbox.Context) = {
    import c.universe._

    val cls = c.enclosingClass.symbol

    assert(cls.isModule || cls.isClass, "Enclosing class is always either a module or a class")

    def loggerByParam(param: c.Tree) = {
      q"new _root_.org.log4s.Logger(_root_.org.slf4j.LoggerFactory.getLogger(...${List(param)}))"
    }

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
      loggerByParam(q"${fullName(s)}")
    }

    def loggerByType(s: Symbol) = {
      val tp = if (s.isModule) s.asModule.moduleClass else s
      loggerByParam(q"classOf[$tp]")
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
  private[this] type LogCtx = whitebox.Context { type PrefixType = Logger }

  /** Log a message reflectively at a given level.
    *
    * This is the internal workhorse method that does most of the logging for real applications.
    *
    * @param msg the message that the user wants to log
    * @param error the `Throwable` that we're logging along with the message, if any
    * @param logLevel the level of the logging
    */
  private[this] def reflectiveLog(c: LogCtx)(msg: c.Expr[String], error: Option[c.Expr[Throwable]])(logLevel: LogLevel) = {
    import c.universe._

    val logger = q"${c.prefix.tree}.logger"
    val logValues = error match {
      case None    => List(msg.tree)
      case Some(e) => List(msg.tree, e.tree)
    }
    val logExpr = q"$logger.${TermName(logLevel.methodName)}(...$logValues)"
    val checkExpr = q"$logger.${TermName(s"is${logLevel.name}Enabled")}()"

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
        q"if ($checkExpr) $logExpr"
    }
  }

  def traceTM(c: LogCtx)(t: c.Expr[Throwable])(msg: c.Expr[String]) = reflectiveLog(c)(msg, Some(t))(Trace)
  def traceM(c: LogCtx)(msg: c.Expr[String]) = reflectiveLog(c)(msg, None)(Trace)

  def debugTM(c: LogCtx)(t: c.Expr[Throwable])(msg: c.Expr[String]) = reflectiveLog(c)(msg, Some(t))(Debug)
  def debugM(c: LogCtx)(msg: c.Expr[String]) = reflectiveLog(c)(msg, None)(Debug)

  def infoTM(c: LogCtx)(t: c.Expr[Throwable])(msg: c.Expr[String]) = reflectiveLog(c)(msg, Some(t))(Info)
  def infoM(c: LogCtx)(msg: c.Expr[String]) = reflectiveLog(c)(msg, None)(Info)

  def warnTM(c: LogCtx)(t: c.Expr[Throwable])(msg: c.Expr[String]) = reflectiveLog(c)(msg, Some(t))(Warn)
  def warnM(c: LogCtx)(msg: c.Expr[String]) = reflectiveLog(c)(msg, None)(Warn)

  def errorTM(c: LogCtx)(t: c.Expr[Throwable])(msg: c.Expr[String]) = reflectiveLog(c)(msg, Some(t))(Error)
  def errorM(c: LogCtx)(msg: c.Expr[String]) = reflectiveLog(c)(msg, None)(Error)
}
