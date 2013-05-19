package org.log4s

import java.util.{ Map => JMap }

import scala.collection.JavaConversions._

import org.slf4j.{ MDC => JMDC }

/** A singleton used for accessing the mapped diagnostic context of your
  * loggers. This acts like a regular map, except that the values are different
  * in every thread.
  *  
  * Getting and setting single values and clearing the map are fast operations
  * operations, but other operations, including getting the size, requires
  * making a copy of the MDC.
  */
object MDC extends collection.mutable.Map[String,String] {
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
  
  /** Get an iterator over the values of this map. This requires making a copy of
    * the map, so it may cause performance problems if called frequently.
    */
  final def iterator: Iterator[(String,String)] = copyMap.iterator
  
  /** Get the size of this map This requires making a copy of the map, so it may
    * cause performance problems if called frequently.
    */
  override final def size: Int = copyMap.size
}

