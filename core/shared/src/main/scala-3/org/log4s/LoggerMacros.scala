package org.log4s

import org.slf4j.{ Logger => JLogger }
import scala.annotation.tailrec
import scala.language.experimental.macros
import scala.quoted._

/** Macros that support the logging system.
  */
private[log4s] object LoggerMacros {

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
