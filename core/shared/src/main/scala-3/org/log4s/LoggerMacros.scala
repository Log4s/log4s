package org.log4s

import org.slf4j.{ Logger => JLogger, LoggerFactory => JLoggerFactory }
import scala.annotation.tailrec
import scala.language.experimental.macros
import scala.quoted._

/** Macros that support the logging system.
  */
private[log4s] object LoggerMacros {

  /** Get a logger by reflecting the enclosing class name. */
  final def getLoggerImpl(using qctx: QuoteContext): Expr[Logger] = {
    import qctx.tasty._

    @tailrec def findEnclosingClass(sym: Symbol): Symbol = {
      sym match {
        case s if s.isNoSymbol =>
          report.throwError("Couldn't find an enclosing class or module for the logger")
        case s if s.isClassDef =>
          s
        case other =>
          /* We're not in a module or a class, so we're probably inside a member definition. Recurse upward. */
          findEnclosingClass(other.owner)
      }
    }

    val cls = findEnclosingClass(Symbol.currentOwner)

    def loggerByName(name: Expr[String]): Expr[Logger] =
      '{new Logger(JLoggerFactory.getLogger($name))}

    def loggerBySymbolName(s: Symbol): Expr[Logger] = {
      def fullName(s: Symbol): String = {
        s.fullName
      }
      loggerByName(Expr(fullName(s)))
    }

    loggerBySymbolName(cls)
  }

  def traceTM(logger: Expr[JLogger])(t: Expr[Throwable])(msg: Expr[String])(using qctx: QuoteContext) =
    '{ if ($logger.isTraceEnabled) $logger.trace($msg, $t) }
  def traceM(logger: Expr[JLogger])(msg: Expr[String])(using qctx: QuoteContext) =
    '{ if ($logger.isTraceEnabled) $logger.trace($msg) }

  def debugTM(logger: Expr[JLogger])(t: Expr[Throwable])(msg: Expr[String])(using qctx: QuoteContext) =
    '{ if ($logger.isDebugEnabled) $logger.debug($msg, $t) }
  def debugM(logger: Expr[JLogger])(msg: Expr[String])(using qctx: QuoteContext) =
    '{ if ($logger.isDebugEnabled) $logger.debug($msg) }

  def infoTM(logger: Expr[JLogger])(t: Expr[Throwable])(msg: Expr[String])(using qctx: QuoteContext) =
    '{ if ($logger.isInfoEnabled) $logger.info($msg, $t) }
  def infoM(logger: Expr[JLogger])(msg: Expr[String])(using qctx: QuoteContext) =
    '{ if ($logger.isInfoEnabled) $logger.info($msg) }

  def warnTM(logger: Expr[JLogger])(t: Expr[Throwable])(msg: Expr[String])(using qctx: QuoteContext) =
    '{ if ($logger.isWarnEnabled) $logger.warn($msg, $t) }
  def warnM(logger: Expr[JLogger])(msg: Expr[String])(using qctx: QuoteContext) =
    '{ if ($logger.isWarnEnabled) $logger.warn($msg) }

  def errorTM(logger: Expr[JLogger])(t: Expr[Throwable])(msg: Expr[String])(using qctx: QuoteContext) =
    '{ if ($logger.isErrorEnabled) $logger.error($msg, $t) }
  def errorM(logger: Expr[JLogger])(msg: Expr[String])(using qctx: QuoteContext) =
    '{ if ($logger.isErrorEnabled) $logger.error($msg) }
}
