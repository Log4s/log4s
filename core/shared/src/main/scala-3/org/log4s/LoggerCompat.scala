package org.log4s

import org.slf4j.{ Logger => JLogger }
import scala.annotation.tailrec
import scala.quoted._

/** Support for cross-building Logger across Scala 2 and Scala 3
  */
private[log4s] trait LoggerCompat extends Any {
  def logger: JLogger

}
