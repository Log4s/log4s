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
  @volatile
  private var initialized: Boolean = false
}

trait LoggerInit {
  locally {
    if (!LoggerInit.initialized) {
      LoggerInit.synchronized {
        if (!LoggerInit.initialized) {
          PlatformInit.init()
          /* This ensures that the underlying logging system is initialized, preventing races */
          getLogger
          ()
        }
      }
    }
  }
}
