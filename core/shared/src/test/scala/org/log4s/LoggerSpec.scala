package org.log4s

import org.scalatest.GivenWhenThen
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import ch.qos.logback.classic.{ Level => Lvl }

/** Test suite for the behavior of Log4s loggers.
  *
  * @author Sarah Gerweck <sarah@atscale.com>
  */
class LoggerSpec extends AnyFlatSpec with Matchers with GivenWhenThen with LoggerInit {
  private[this] val testLogger = getLogger("test")
  private[this] val traceLogger = getLogger("level.tr")
  private[this] val debugLogger = getLogger("level.de")
  private[this] val infoLogger = getLogger("level.in")
  private[this] val warnLogger = getLogger("level.wa")
  private[this] val errorLogger = getLogger("level.er")

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
    val e1 = new Exception( /* no message */ )
    testLogger.warn(e1)("warnErrorLiteral")
    event hasData ("warnErrorLiteral", Lvl.WARN, Some(e1))

    When("doing dynamic error logging")
    val e2 = new Exception("test message e2")
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
    val e1 = new Exception("test message e1")
    testLogger.error(e1)("errorErrorLiteral")
    event hasData ("errorErrorLiteral", Lvl.ERROR, Some(e1))

    When("doing dynamic error logging")
    val e2 = new Exception(/* no message */)
    testLogger.error(Some(e2).get)(s"errorError${0+2}")
    event hasData ("errorError2", Lvl.ERROR, Some(e2))
  }

  it should "identify trace enablement" in {
    val l: Logger = traceLogger

    When("threshold is trace")
    l.isTraceEnabled shouldBe true

    When("threshold is debug")
    l.isDebugEnabled shouldBe true

    When("threshold is info")
    l.isInfoEnabled shouldBe true

    When("threshold is warn")
    l.isWarnEnabled shouldBe true

    When("threshold is error")
    l.isErrorEnabled shouldBe true
  }

  it should "identify debug enablement" in {
    val l: Logger = debugLogger

    When("threshold is trace")
    l.isTraceEnabled shouldBe false

    When("threshold is debug")
    l.isDebugEnabled shouldBe true

    When("threshold is info")
    l.isInfoEnabled shouldBe true

    When("threshold is warn")
    l.isWarnEnabled shouldBe true

    When("threshold is error")
    l.isErrorEnabled shouldBe true
  }

  it should "identify info enablement" in {
    val l: Logger = infoLogger

    When("threshold is trace")
    l.isTraceEnabled shouldBe false

    When("threshold is debug")
    l.isDebugEnabled shouldBe false

    When("threshold is info")
    l.isInfoEnabled shouldBe true

    When("threshold is warn")
    l.isWarnEnabled shouldBe true

    When("threshold is error")
    l.isErrorEnabled shouldBe true
  }

  it should "identify warn enablement" in {
    val l: Logger = warnLogger

    When("threshold is trace")
    l.isTraceEnabled shouldBe false

    When("threshold is debug")
    l.isDebugEnabled shouldBe false

    When("threshold is info")
    l.isInfoEnabled shouldBe false

    When("threshold is warn")
    l.isWarnEnabled shouldBe true

    When("threshold is error")
    l.isErrorEnabled shouldBe true
  }

  it should "identify error enablement" in {
    val l: Logger = errorLogger

    When("threshold is trace")
    l.isTraceEnabled shouldBe false

    When("threshold is debug")
    l.isDebugEnabled shouldBe false

    When("threshold is info")
    l.isInfoEnabled shouldBe false

    When("threshold is warn")
    l.isWarnEnabled shouldBe false

    When("threshold is error")
    l.isErrorEnabled shouldBe true
  }

  it should "handle MDC behavior" in {
    When("doing normal logging using MDC.withCtx")
    MDC.withCtx("a" -> "b") {
      testLogger.debug("Test message")
      event mdcSatisfies { mdc =>
        mdc should have size 1
        mdc should contain key ("a")
        mdc("a") shouldEqual "b"
      }
    }
    When("doing manual MDC context manipulations")
    locally {
      MDC += "b" -> "c"
      MDC += "f" -> "a"
    }
    try {
      testLogger.debug("Test message 2")
      event mdcSatisfies { mdc =>
        mdc should have size 2
        mdc should contain key ("b")
        mdc should contain key ("f")
        mdc("b") should equal ("c")
        mdc("f") should equal ("a")
      }
    } finally {
      MDC -= "b"
      MDC -= "f"
    }
  }

  it should "run the example TestAppender code" in {
    TestAppender.withAppender() {
      testLogger.debug("Here's a test message")
      val eventOpt = TestAppender.dequeue
      eventOpt.isDefined should equal (true)
      eventOpt foreach { e =>
        e.message should equal ("Here's a test message")
        e.throwable.isDefined should equal (false)
      }
    }
  }

  private[this] implicit class ComparableThrowable(val t: Throwable) {
    def shouldMatch (tp: LoggedThrowable): Unit = {
      Option(t.getMessage) shouldEqual tp.message
      Option(t.getCause) match {
        case None => tp.cause.isDefined should equal (false)
        case Some(c) =>
          tp.cause.isDefined should equal (true)
          tp.cause foreach (c shouldMatch _)
      }
      t.getClass.getName shouldEqual tp.className
    }
  }

  private[this] object event {
    def satisfies[A](fn: LoggedEvent => A): Unit = {
      val eventOpt = TestAppender.dequeue
      eventOpt.isDefined should equal (true)
      eventOpt foreach fn
    }
    def mdcSatisfies[A](fn: scala.collection.Map[String, String] => A): Unit = {
      this satisfies { event =>
        fn(event.mdc)
      }
    }
    def hasData (msg: String, level: Lvl, throwable: Option[Throwable]) = {
      this satisfies { event =>
        event.formattedMessage shouldEqual msg
        event.level shouldEqual level

        event.inner.getArgumentArray() should be (null)
        event.loggerName shouldEqual "test"

        val tp = event.throwable
        throwable match {
          case Some(t) =>
            tp.isDefined should equal (true)
            tp foreach (t shouldMatch _)
          case None    =>
            tp.isDefined should equal (false)
        }
      }
    }
  }
}

