package org.slf4j

import scala.collection.JavaConverters._

import java.util.{ Map => JMap }

object MDC {
  sealed trait MDCCloseable extends java.io.Closeable with AutoCloseable

  private[this] object mdc {
    private[this] val dynamicMDC = new ThreadLocal[Map[String, String]]
    def apply(): Map[String, String] = Option(dynamicMDC()).getOrElse(Map.empty[String, String])
    def update(m: Map[String, String]) = {
      if (m.isEmpty) {
        dynamicMDC.remove()
      } else {
        dynamicMDC() = m
      }
    }
    def clear(): Unit = dynamicMDC.remove()

    /* HELPERS */
    private[this] implicit final class RichThreadLocal[A](val inner: ThreadLocal[A]) extends AnyVal {
      @inline def update(a: A) = inner.set(a)
      @inline def apply(): A = inner.get()
    }
  }

  def clear(): Unit = mdc.clear()
  /* TODO: Make me package private? */
  def asScala(): Map[String, String] = mdc()
  def get(key: String): String = mdc().get(key).orNull
  def getCopyOfContextMap(): JMap[String, String] = mdc().asJava
  def put(key: String, `val`: String): Unit = {
    mdc() = mdc() + (key -> `val`)
  }
  def putCloseable(key: String, `val`: String): MDCCloseable = {
    val closeOp: () => Unit =
      mdc().get(key) match {
        case None    => () => remove(key)
        case Some(v) => () => put(key, v)
      }
    put(key, `val`)
    new MDCCloseable { def close(): Unit = closeOp() }
  }
  def remove(key: String) = {
    mdc() = mdc() - key
  }
  def setContextMap(m: JMap[String, String]) = {
    mdc() = m.asScala.toMap
  }
}
