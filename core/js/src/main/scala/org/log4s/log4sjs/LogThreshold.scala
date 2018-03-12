package org.log4s
package log4sjs

import scala.math.Ordered._

import scala.scalajs.js
import js.annotation._

/** A threshold that can be applied to log configuration or filtering.
  *
  * This includes all log levels, plus additional "all" and "off" levels.
  */
sealed trait LogThreshold extends Any {
  def permits(ll: LogLevel): Boolean = this <= LevelThreshold(ll)
}
object LogThreshold {
  implicit val order: Ordering[LogThreshold] = Ordering.by {
    case OffThreshold => Int.MaxValue
    case LevelThreshold(Error) => 40000
    case LevelThreshold(Warn)  => 30000
    case LevelThreshold(Info)  => 20000
    case LevelThreshold(Debug) => 10000
    case LevelThreshold(Trace) => 5000
    case AllThreshold          => Int.MinValue
  }
  def forName(name: String): LogThreshold = {
    name.toLowerCase match {
      case "off" => OffThreshold
      case "all" => AllThreshold
      case other => LevelThreshold(LogLevel.forName(other))
    }
  }
}

@JSExportTopLevel("AllThreshold")
case object AllThreshold extends LogThreshold

@JSExportTopLevel("OffThreshold")
case object OffThreshold extends LogThreshold

object LevelThreshold {
  def apply(ll: LogLevel) = new LevelThreshold(ll)
  def unapply(lt: LogThreshold) = lt match {
    case lt: LevelThreshold => Some(lt.inner)
    case _                  => None
  }
}
class LevelThreshold(val inner: LogLevel) extends AnyVal with LogThreshold
