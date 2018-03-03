package org.log4s
package log4sjs

import scala.scalajs.js
import js.annotation._

object Log4s {
  @JSExportTopLevel("getLogger")
  def getLogger(name: String) = org.slf4j.LoggerFactory.getLogger(name)
}
