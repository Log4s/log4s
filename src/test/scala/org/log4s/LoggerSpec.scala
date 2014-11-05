package org.log4s

import org.scalatest._

import ch.qos.logback.classic.{ Level => Lvl }
import ch.qos.logback.classic.spi.IThrowableProxy

/** Test suite for the behavior of Log4s loggers.
  *
  * @author Sarah Gerweck <sarah@atscale.com>
  */
class LoggerSpec extends FlatSpec with Matchers with GivenWhenThen with LoggerInit {
  private[this] val testLogger = getLogger("test")

  behavior of "log compilation"

  it should "generate trace logging" in {
    When("doing static logging")
    testLogger.trace("traceLiteral")
    event hasData ("traceLiteral", Lvl.TRACE, None)

    When("doing dynamic logging")
    testLogger.trace(s"trace${1+1}")
    event hasData ("trace2", Lvl.TRACE, None)

    When("doing static error logging")
    val e1 = new Exception()
    testLogger.trace(e1)("traceErrorLiteral")
    event hasData ("traceErrorLiteral", Lvl.TRACE, Some(e1))

    When("doing dynamic error logging")
    val e2 = new Exception()
    testLogger.trace(Some(e2).get)(s"traceError${0+2}")
    event hasData ("traceError2", Lvl.TRACE, Some(e2))
  }

  it should "generate debug logging" in {
    When("doing literal logging")
    testLogger.debug("debugLiteral")
    event hasData ("debugLiteral", Lvl.DEBUG, None)

    When("doing dynamic logging")
    testLogger.debug(s"debug${1+1}")
    event hasData ("debug2", Lvl.DEBUG, None)

    When("doing static error logging")
    val e1 = new Exception()
    testLogger.debug(e1)("debugErrorLiteral")
    event hasData ("debugErrorLiteral", Lvl.DEBUG, Some(e1))

    When("doing dynamic error logging")
    val e2 = new Exception()
    testLogger.debug(Some(e2).get)(s"debugError${0+2}")
    event hasData ("debugError2", Lvl.DEBUG, Some(e2))
  }

  it should "generate info logging" in {
    When("doing literal logging")
    testLogger.info("infoLiteral")
    event hasData ("infoLiteral", Lvl.INFO, None)

    When("doing dynamic logging")
    testLogger.info(s"info${1+1}")
    event hasData ("info2", Lvl.INFO, None)

    When("doing static error logging")
    val e1 = new Exception()
    testLogger.info(e1)("infoErrorLiteral")
    event hasData ("infoErrorLiteral", Lvl.INFO, Some(e1))

    When("doing dynamic error logging")
    val e2 = new Exception()
    testLogger.info(Some(e2).get)(s"infoError${0+2}")
    event hasData ("infoError2", Lvl.INFO, Some(e2))
  }

  it should "generate warn logging" in {
    When("doing literal logging")
    testLogger.warn("warnLiteral")
    event hasData ("warnLiteral", Lvl.WARN, None)

    When("doing dynamic logging")
    testLogger.warn(s"warn${1+1}")
    event hasData ("warn2", Lvl.WARN, None)

    When("doing static error logging")
    val e1 = new Exception()
    testLogger.warn(e1)("warnErrorLiteral")
    event hasData ("warnErrorLiteral", Lvl.WARN, Some(e1))

    When("doing dynamic error logging")
    val e2 = new Exception()
    testLogger.warn(Some(e2).get)(s"warnError${0+2}")
    event hasData ("warnError2", Lvl.WARN, Some(e2))
  }

  it should "generate error logging" in {
    When("doing literal logging")
    testLogger.error("errorLiteral")
    event hasData ("errorLiteral", Lvl.ERROR, None)

    When("doing dynamic logging")
    testLogger.error(s"error${1+1}")
    event hasData ("error2", Lvl.ERROR, None)

    When("doing static error logging")
    val e1 = new Exception()
    testLogger.error(e1)("errorErrorLiteral")
    event hasData ("errorErrorLiteral", Lvl.ERROR, Some(e1))

    When("doing dynamic error logging")
    val e2 = new Exception()
    testLogger.error(Some(e2).get)(s"errorError${0+2}")
    event hasData ("errorError2", Lvl.ERROR, Some(e2))
  }

  private[this] implicit class ComparableThrowable(val t: Throwable) {
    def shouldMatch (tp: IThrowableProxy) {
      t.getMessage shouldEqual tp.getMessage
      t.getCause match {
        case null => tp.getCause should be (null)
        case c    => c shouldMatch tp.getCause
      }
      t.getClass.getCanonicalName shouldEqual tp.getClassName
    }
  }

  private[this] object event {
    def hasData (msg: String, level: Lvl, throwable: Option[Throwable]) = {
      val event = TestAppender.dequeue

      event.getFormattedMessage shouldEqual msg
      event.getLevel shouldEqual level

      event.getArgumentArray should be (null)
      event.getLoggerName shouldEqual "test"

      val tp = event.getThrowableProxy
      throwable match {
        case Some(t) => t shouldMatch tp
        case None    => tp should be (null)
      }
    }
  }
}

