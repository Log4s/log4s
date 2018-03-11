package org.log4s

import scala.scalajs.js
import js.annotation._

/** A severity level that can be assigned to log statements. */
sealed trait LogLevel {
  /** The name of this log level. It is spelled with initial capitals */
  def name: String = this.toString

  /** The name of the SLF4J method that does logging at this level */
  private[log4s] def methodName = name.toLowerCase
}
object LogLevel {
  def forName(name: String): LogLevel = {
    name.toLowerCase match {
      case "trace" => Trace
      case "debug" => Debug
      case "info"  => Info
      case "warn"  => Warn
      case "error" => Error
      case _ =>
        throw new IllegalArgumentException(s"No log level named $name")
    }
  }
}

/** The highest logging severity. This generally indicates an
  * application or system error that causes undesired outcomes.
  * An error generally indicates a bug or an environment
  * problem that warrants some kind of immediate intervention.
  */
@JSExportTopLevel("Error")
case object Error extends LogLevel

/** Generally indicates something is not expected but the system is
  * able to continue operating. This generally indicates a bug or
  * environment problem that does not require urgent intervention.
  */
@JSExportTopLevel("Warn")
case object Warn extends LogLevel

/** Indicates normal high-level activity. Generally a single user– or
  * system-initiated activity will trigger one or two info-level statements.
  * (E.g., one when starting and one when finishing for complex requests.)
  */
@JSExportTopLevel("Info")
case object Info extends LogLevel

/** Log statements that provide the ability to trace the progress and
  * behavior involved in tracking a single activity. These are useful for
  * debugging general issues, identifying how modules are interacting, etc.
  */
@JSExportTopLevel("Debug")
case object Debug extends LogLevel

/** Highly localized log statements useful for tracking the decisions made
  * inside a single unit of code. These may occur at a very high frequency.
  */
@JSExportTopLevel("Trace")
case object Trace extends LogLevel
