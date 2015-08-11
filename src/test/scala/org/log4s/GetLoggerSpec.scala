package org.log4s

import org.scalatest._

/** Test suite for the behavior of `getLogger`.
  *
  * @author Sarah Gerweck <sarah@atscale.com>
  */
class GetLoggerSpec extends FlatSpec with Matchers with GivenWhenThen with LoggerInit {
  private[this] val logger = getLogger

  behavior of "getLogger"

  it should "properly name class loggers" in {
    logger.name shouldEqual "org.log4s.GetLoggerSpec"
  }

  it should "properly name simply parametrized class loggers" in {
    val lsp = new GetLoggerSpecParam[GetLoggerSpec]
    lsp.logger.name shouldEqual "org.log4s.GetLoggerSpecParam"
  }

  it should "properly name complex parametrized class loggers" in {
    implicit val intPrinter = new Printable[Int] { def print(i: Int) = i.toString }
    val lspb = new GetLoggerSpecParamBounded(3)
    lspb.logger.name shouldEqual "org.log4s.GetLoggerSpecParamBounded"
  }

  it should "properly name local object loggers" in {
    object LocalObject {
      private[GetLoggerSpec] val logger = getLogger
    }
    LocalObject.logger.name shouldEqual "org.log4s.GetLoggerSpec.LocalObject"
  }

  it should "properly name local class loggers" in {
    class LocalClass {
      private[GetLoggerSpec] val logger = getLogger
    }
    val ic = new LocalClass
    ic.logger.name shouldEqual "org.log4s.GetLoggerSpec.LocalClass"
  }

  it should "properly name top-level object loggers" in {
    GetLoggerSpecTLO.logger.name shouldEqual "org.log4s.GetLoggerSpecTLO"
  }

  it should "properly name objects inside objects" in {
    GetLoggerSpecTLO.InnerObject.logger.name shouldEqual "org.log4s.GetLoggerSpecTLO.InnerObject"
  }

  it should "properly name classes inside objects" in {
    val io = new GetLoggerSpecTLO.InnerClass
    io.logger.name shouldEqual "org.log4s.GetLoggerSpecTLO.InnerClass"
  }

  it should "properly name objects inside classes" in {
    val tlc = new GetLoggerSpecTLC
    tlc.InnerObject.logger.name shouldEqual "org.log4s.GetLoggerSpecTLC.InnerObject"
  }

  it should "properly name classes inside classes" in {
    val tlc = new GetLoggerSpecTLC
    val ic = new tlc.InnerClass
    ic.logger.name shouldEqual "org.log4s.GetLoggerSpecTLC.InnerClass"
  }

  it should "properly name loggers inside closures" in {
    val l = getLogger
    l.name shouldEqual "org.log4s.GetLoggerSpec"
  }

  def myFun(): Logger = {
    val l = getLogger
    l
  }

  it should "properly name loggers inside named functions" in {
    myFun().name shouldEqual "org.log4s.GetLoggerSpec"
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
    helper1().name shouldEqual "org.log4s.GetLoggerSpec"
    helper2().name shouldEqual "org.log4s.GetLoggerSpec"
  }

  it should "properly name package-scoped loggers" in {
    val l = packageScoped.logger
    l.name shouldEqual "org.log4s.packageScoped"
  }

  it should "support explicit logger names" in {
    getLogger("a.b.c").name shouldEqual "a.b.c"
  }
}

private class GetLoggerSpecParam[A] {
  val logger = getLogger
}

// The trait is here to facilitate this common use pattern of type bounds.
// (It has no significance in itself: its purpose is to give us a complex type structure.)
private trait Printable[A] { def print(a: A): String }
private class GetLoggerSpecParamBounded[A, B <: Printable[A]](default: A)(implicit printer: B) {
  val logger = getLogger
  /* The print method is, like the Printable trait, purely structural */
  def print = printer.print(default)
}

private class GetLoggerSpecTLC {
  val logger = getLogger
  object InnerObject {
    val logger = getLogger
  }
  class InnerClass {
    val logger = getLogger
  }
}

private object GetLoggerSpecTLO {
  val logger = getLogger
  object InnerObject {
    val logger = getLogger
  }
  class InnerClass {
    val logger = getLogger
  }
}

package object packageScoped {
  val logger = org.log4s.getLogger
}
