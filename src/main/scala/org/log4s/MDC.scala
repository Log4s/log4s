package org.log4s

import java.util.{ Map => JMap }

import scala.collection.JavaConversions._

import org.slf4j.{ MDC => JMDC }

object MDC extends AnyRef with collection.mutable.Map[String,String] {
  @inline private[this] final def copyMap: JMap[String,String] = JMDC.getCopyOfContextMap.asInstanceOf[JMap[String,String]]
  
  final def += (kv: (String,String)): this.type = {
    val (key, value) = kv
    JMDC.put(key, value)
    this
  }
  
  final def -= (key: String): this.type = {
    JMDC.remove(key)
    this
  }
  
  override final def clear() { JMDC.clear() }
  
  final def get(key: String): Option[String] = Option(JMDC.get(key))
  
  final def iterator: Iterator[(String,String)] = copyMap.iterator
  
  override final def size: Int = copyMap.size
}

