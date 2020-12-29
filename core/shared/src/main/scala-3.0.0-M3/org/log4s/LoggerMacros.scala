package org.log4s

import org.slf4j.{ Logger => JLogger, LoggerFactory => JLoggerFactory }
import scala.annotation.tailrec
import scala.language.experimental.macros
import scala.quoted._

/** Macros that support the logging system.
  */
private[log4s] object LoggerMacros {

  /** Get a logger by reflecting the enclosing class name. */
  final def getLoggerImpl(using qctx: Quotes): Expr[Logger] = {
    import qctx.reflect._

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

    def logger(s: Symbol): Expr[Logger] = {
      def fullName(s: Symbol): String = {
        val flags = s.flags
        if (flags.is(Flags.Package)) {
          s.fullName
        }
        else if (s.isClassDef) {
          if (flags.is(Flags.Module)) {
            if (s.name == "package$") {
              fullName(s.owner)
            }
            else {
              val chomped = s.name.stripSuffix("$")
              fullName(s.owner) + "." + chomped
            }
          }
          else {
            fullName(s.owner) + "." + s.name
          }
        }
        else {
          fullName(s.owner)
        }
      }

      val name = Expr(fullName(s))
      '{ new Logger(JLoggerFactory.getLogger($name)) }
    }

    val cls = findEnclosingClass(Symbol.spliceOwner)
    logger(cls)
  }

  def traceTM(logger: Expr[JLogger])(t: Expr[Throwable])(msg: Expr[String])(using qctx: Quotes) =
    '{ if ($logger.isTraceEnabled()) $logger.trace($msg, $t) }
  def traceM(logger: Expr[JLogger])(msg: Expr[String])(using qctx: Quotes) =
    '{ if ($logger.isTraceEnabled()) $logger.trace($msg) }

  def debugTM(logger: Expr[JLogger])(t: Expr[Throwable])(msg: Expr[String])(using qctx: Quotes) =
    '{ if ($logger.isDebugEnabled()) $logger.debug($msg, $t) }
  def debugM(logger: Expr[JLogger])(msg: Expr[String])(using qctx: Quotes) =
    '{ if ($logger.isDebugEnabled()) $logger.debug($msg) }

  def infoTM(logger: Expr[JLogger])(t: Expr[Throwable])(msg: Expr[String])(using qctx: Quotes) =
    '{ if ($logger.isInfoEnabled()) $logger.info($msg, $t) }
  def infoM(logger: Expr[JLogger])(msg: Expr[String])(using qctx: Quotes) =
    '{ if ($logger.isInfoEnabled()) $logger.info($msg) }

  def warnTM(logger: Expr[JLogger])(t: Expr[Throwable])(msg: Expr[String])(using qctx: Quotes) =
    '{ if ($logger.isWarnEnabled()) $logger.warn($msg, $t) }
  def warnM(logger: Expr[JLogger])(msg: Expr[String])(using qctx: Quotes) =
    '{ if ($logger.isWarnEnabled()) $logger.warn($msg) }

  def errorTM(logger: Expr[JLogger])(t: Expr[Throwable])(msg: Expr[String])(using qctx: Quotes) =
    '{ if ($logger.isErrorEnabled()) $logger.error($msg, $t) }
  def errorM(logger: Expr[JLogger])(msg: Expr[String])(using qctx: Quotes) =
    '{ if ($logger.isErrorEnabled()) $logger.error($msg) }
}
