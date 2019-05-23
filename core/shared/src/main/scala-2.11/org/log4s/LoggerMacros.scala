package org.log4s

import language.experimental.macros

import scala.annotation.tailrec
import scala.reflect.macros.blackbox

/** Macros that support the logging system.
  *
  * @author Sarah Gerweck <sarah@atscale.com>
  */
private[log4s] object LoggerMacros {

  /** Get a logger by reflecting the enclosing class name. */
  final def getLoggerImpl(c: blackbox.Context) = {
    import c.universe._

    @tailrec def findEnclosingClass(sym: c.universe.Symbol): c.universe.Symbol = {
      sym match {
        case NoSymbol =>
          c.abort(c.enclosingPosition, s"Couldn't find an enclosing class or module for the logger")
        case s if s.isModule || s.isClass =>
          s
        case other =>
          /* We're not in a module or a class, so we're probably inside a member definition. Recurse upward. */
          findEnclosingClass(other.owner)
      }
    }

    val cls = findEnclosingClass(c.internal.enclosingOwner)

    assert(cls.isModule || cls.isClass, "Enclosing class is always either a module or a class")

    def loggerByParam(param: c.Tree) = {
      q"new _root_.org.log4s.Logger(_root_.org.slf4j.LoggerFactory.getLogger(...${List(param)}))"
    }

    def loggerBySymbolName(s: Symbol) = {
      def fullName(s: Symbol): String = {
        @inline def isPackageObject = (
          (s.isModule || s.isModuleClass)
          && s.owner.isPackage
          && s.name.decodedName.toString == termNames.PACKAGE.decodedName.toString
        )
        if (s.isModule || s.isClass) {
          if (isPackageObject) {
            s.owner.fullName
          } else if (s.owner.isStatic) {
            s.fullName
          } else {
            fullName(s.owner) + "." + s.name.encodedName.toString
          }
        } else {
          fullName(s.owner)
        }
      }
      loggerByParam(q"${fullName(s)}")
    }

    def loggerByType(s: Symbol) = {
      val typeSymbol: ClassSymbol = (if (s.isModule) s.asModule.moduleClass else s).asClass
      val typeParams = typeSymbol.typeParams

      if (typeParams.isEmpty) {
        loggerByParam(q"classOf[$typeSymbol]")
      } else {
        if (typeParams.exists(_.asType.typeParams.nonEmpty)) {
          /* We have at least one higher-kinded type: fall back to by-name logger construction, as
           * there's no simple way to declare a higher-kinded type with an "any" parameter. */
          loggerBySymbolName(s)
        } else {
          val typeArgs = List.fill(typeParams.length)(WildcardType)
          val typeConstructor = tq"$typeSymbol[..${typeArgs}]"
          loggerByParam(q"classOf[$typeConstructor]")
        }
      }
    }

    @inline def isInnerClass(s: Symbol) = {
      s.isClass && !(s.owner.isPackage)
    }

    val instanceByName = Logger.singletonsByName && (cls.isModule || cls.isModuleClass) || cls.isClass && isInnerClass(cls)

    if (instanceByName) {
      loggerBySymbolName(cls)
    } else {
      loggerByType(cls)
    }
  }

  /** A macro context that represents a method call on a Logger instance. */
  private[this] type LogCtx = blackbox.Context { type PrefixType = Logger }

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
    val logExpr = q"$logger.${TermName(logLevel.methodName)}(..$logValues)"
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
