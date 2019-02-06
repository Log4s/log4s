package org.log4s
package log4sjs

import scala.scalajs.js
import js.annotation._
import js.JSConverters._

object Log4sMDC {
  @JSExport
  def get(key: String): String = MDC.get(key).orNull

  @JSExport
  def update(key: String, value: String): Unit = {
    Option(value) match {
      case Some(v) => MDC(key) = value
      case None    => remove(key)
    }
  }

  @JSExport
  def remove(key: String): Unit = {
    MDC -= key
  }

  @JSExport
  def clear(): Unit = {
    MDC.clear()
  }

  @JSExport
  def getCopyOfContextMap(): js.Dictionary[String] = MDC.toMap.toJSDictionary

  @JSExport
  def withCtx[A](key: String, value: String): js.Function1[js.Function0[_], _] = {
    withCtx(js.Dictionary(key -> value))
  }

  @JSExport
  def withCtx(values: js.Dictionary[String]): js.Function1[js.Function0[_], _] = {
    fn: js.Function0[_] => MDC.withCtx(values.toSeq: _*)(fn())
  }
}
