package org.log4s

import org.slf4j.{ Logger => JLogger }
import scala.annotation.tailrec
import scala.quoted._

/** Support for cross-building Logger across Scala 2 and Scala 3
  */
private[log4s] trait LoggerCompat extends Any {
  def logger: JLogger

  import LoggerMacros._

  inline def trace(inline t: Throwable)(inline msg: String): Unit =
    ${traceTM('logger)('t)('msg)}
  inline def trace(inline msg: String): Unit =
    ${traceM('logger)('msg)}

  inline def debug(inline t: Throwable)(inline msg: String): Unit =
    ${debugTM('logger)('t)('msg)}
  inline def debug(inline msg: String): Unit =
    ${debugM('logger)('msg)}

  inline def info(inline t: Throwable)(inline msg: String): Unit =
    ${infoTM('logger)('t)('msg)}
  inline def info(inline msg: String): Unit =
    ${infoM('logger)('msg)}

  inline def warn(inline t: Throwable)(inline msg: String): Unit =
    ${warnTM('logger)('t)('msg)}
  inline def warn(inline msg: String): Unit =
    ${warnM('logger)('msg)}

  inline def error(inline t: Throwable)(inline msg: String): Unit =
    ${errorTM('logger)('t)('msg)}
  inline def error(inline msg: String): Unit =
    ${errorM('logger)('msg)}
}
