package org.log4s
package log4sjs

import scala.scalajs.js
import js.annotation._

@JSExportTopLevel("Log4s")
object Log4s {
  @JSExportTopLevel("getLogger")
  @JSExport("getLogger")
  def getLogger(name: String) = org.slf4j.LoggerFactory.getLogger(name)
  @JSExport("Config")
  val Config = Log4sConfig
  @JSExport("MDC")
  val MDC = log4sjs.Log4sMDC
  @JSExport("LogThreshold")
  val LogThreshold = new AnyRef {
    @JSExport
    val AllThreshold = log4sjs.LogThreshold.AllThreshold
    @JSExport
    val OffThreshold = log4sjs.LogThreshold.OffThreshold
  }

  val Level = new AnyRef {
    @JSExport
    val Trace = org.log4s.Trace
    @JSExport
    val Debug = org.log4s.Debug
    @JSExport
    val Info = org.log4s.Info
    @JSExport
    val Warn = org.log4s.Warn
    @JSExport
    val Error = org.log4s.Error
  }
}
