package org.log4s

/** Synchronization point for thread safety.
  *
  * SLF4J has a silly, years-old bug wherein you will get dropped messages if
  * you simultaneously initialize your first loggers from multiple threads.
  * Because ScalaTest is multi-threaded, I need a common entry point to
  * prevent that.
  *
  * @author Sarah Gerweck <sarah@atscale.com>
  */
object LoggerInit {
  // The `lazy` here gives me cheap synchronization
  private lazy val logger = getLogger
}

trait LoggerInit {
  locally {
    // Don't really do anything except hit my synchronized logger
    LoggerInit.logger
  }
}
