package org.log4s

import org.scalatest._

/** Test suite for the standard Log4s loggers.
  *
  * @author Sarah Gerweck <sarah@atscale.com>
  */
class LoggerSpec extends FlatSpec with Matchers {
  private[this] val logger = getLogger

  behavior of "getLogger"

  it should "properly name class loggers" in {
    classOf[LoggerSpec].getCanonicalName
    logger.name shouldEqual "org.log4s.LoggerSpec"
  }

  it should "properly name local object loggers" in {
    object LocalObject {
      private[LoggerSpec] val logger = getLogger
    }
    LocalObject.logger.name shouldEqual "org.log4s.LoggerSpec.LocalObject"
  }

  it should "properly name local class loggers" in {
    class LocalClass {
      private[LoggerSpec] val logger = getLogger
    }
    val ic = new LocalClass
    ic.logger.name shouldEqual "org.log4s.LoggerSpec.LocalClass"
  }

  it should "properly name top-level object loggers" in {
    LoggerSpecTLO.logger.name shouldEqual "org.log4s.LoggerSpecTLO"
  }

  it should "properly name objects inside objects" in {
    LoggerSpecTLO.InnerObject.logger.name shouldEqual "org.log4s.LoggerSpecTLO.InnerObject"
  }

  it should "properly name classes inside objects" in {
    val io = new LoggerSpecTLO.InnerClass
    io.logger.name shouldEqual "org.log4s.LoggerSpecTLO.InnerClass"
  }

  it should "properly name objects inside classes" in {
    val tlc = new LoggerSpecTLC
    tlc.InnerObject.logger.name shouldEqual "org.log4s.LoggerSpecTLC.InnerObject"
  }

  it should "properly name classes inside classes" in {
    val tlc = new LoggerSpecTLC
    val ic = new tlc.InnerClass
    ic.logger.name shouldEqual "org.log4s.LoggerSpecTLC.InnerClass"
  }

  it should "properly name loggers inside closures" in {
    val l = getLogger
    l.name shouldEqual "org.log4s.LoggerSpec"
  }

  def myFun(): Logger = {
    val l = getLogger
    l
  }

  it should "properly name loggers inside named functions" in {
    myFun().name shouldEqual "org.log4s.LoggerSpec"
  }

  it should "properly name loggers inside nested functions" in {
    def helper1() = {
      val l = getLogger
      l
    }
    def helper2() = {
      def helper3(i: Int) = {
        val l = getLogger
        l
      }
      helper3(37)
    }
    helper1().name shouldEqual "org.log4s.LoggerSpec"
    helper2().name shouldEqual "org.log4s.LoggerSpec"
  }

  it should "support explicit logger names" in {
    getLogger("a.b.c").name shouldEqual "a.b.c"
  }
}

private class LoggerSpecTLC {
  val logger = getLogger
  object InnerObject {
    val logger = getLogger
  }
  class InnerClass {
    val logger = getLogger
  }
}

private object LoggerSpecTLO {
  val logger = getLogger
  object InnerObject {
    val logger = getLogger
  }
  class InnerClass {
    val logger = getLogger
  }
}
