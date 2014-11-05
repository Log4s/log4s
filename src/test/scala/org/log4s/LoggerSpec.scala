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
    logger.name shouldEqual "org.log4s.LoggerSpec"
  }

  it should "properly name simply parametrized class loggers" in {
    val lsp = new LoggerSpecParam[LoggerSpec]
    lsp.logger.name shouldEqual "org.log4s.LoggerSpecParam"
  }

  it should "properly name complex parametrized class loggers" in {
    implicit val intPrinter = new Printable[Int] { def print(i: Int) = i.toString }
    val lspb = new LoggerSpecParamBounded(3)
    lspb.logger.name shouldEqual "org.log4s.LoggerSpecParamBounded"
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

  // these are strictly compile tests for the macro generators.
  it should "properly compile macro logging" in {
    val quietLogger = getLogger("quiet")

    quietLogger.trace("foo")
    quietLogger.trace(new Exception())("foo")

    quietLogger.debug("Foo")
    quietLogger.debug(new Exception())("foo")

    quietLogger.info("Foo")
    quietLogger.info(new Exception())("foo")

    quietLogger.warn("Foo")
    quietLogger.warn(new Exception())("foo")

    quietLogger.error("Foo")
    quietLogger.error(new Exception())("foo")

    true shouldEqual true
  }
}

private class LoggerSpecParam[A] {
  val logger = getLogger
}


// The trait is here to facilitate this common use pattern of type bounds.
// (It has no significance in itself: its purpose is to give us a complex type structure.)
private trait Printable[A] { def print(a: A): String }
private class LoggerSpecParamBounded[A, B <: Printable[A]](default: A)(implicit printer: B) {
  val logger = getLogger
  /* The print method is, like the Printable trait, purely structural */
  def print = printer.print(default)
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
