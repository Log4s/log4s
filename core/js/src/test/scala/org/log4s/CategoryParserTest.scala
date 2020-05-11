package org.log4s.log4sjs

import scala.collection.immutable.{ Seq => ISeq }

import org.scalacheck._
import org.scalacheck.Arbitrary._

import org.scalatest.Assertion
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._

private object LoggerParserSpec {
  private[this] final val catParser = LoggerParser

  object select
  final class select(val cat: String) extends AnyVal {
    def logger(s: String*): Assertion = {
      catParser(cat) should equal (s.toList)
    }
    @inline
    def loggerOf(s: ISeq[String]): Assertion = this.logger(s: _*)
  }

  implicit final class LoggerString(val cat: String) extends AnyVal {
    @inline
    def should(sel: select.type): select = new select(cat)
  }

  val simpleLoggerPath: Gen[ISeq[String]] = Gen.listOf(Gen.identifier)

  val complexLoggerPath = arbitraryLoggerPath(true)

  def arbitraryLoggerPath(aggressive: Boolean): Gen[ISeq[String]] = {
    val arbitraryPart = {
      val safeChar = arbitrary[Char].retryUntil(c => c.isUnicodeIdentifierPart || c == '.' || c == '\\')
      val anyChar = arbitrary[Char].retryUntil(c => !(c.isWhitespace || c.isControl))
      val escapedChar = Gen.oneOf('.', '\\')
      val printableChar = if (aggressive) anyChar else safeChar
      val partChar = Gen.frequency(1 -> escapedChar, 8 -> printableChar)
      Gen.nonEmptyListOf(partChar).map(_.mkString)
    }
    Gen.listOf(arbitraryPart)
  }

  def makePath(path: ISeq[String]): String = {
    def escapePart(s: String): String = {
      s .flatMap {
        case '.'   => "\\."
        case '\\'  => "\\\\"
        case other => String.valueOf(other)
      }
    }
    path.map(escapePart).mkString(".")
  }
}

import LoggerParserSpec._

class LoggerParserSpec extends AnyFlatSpec with ScalaCheckPropertyChecks {
  it should "handle the root logger correctly" in {
    "" should select loggerOf (Nil)
  }
  it should "handle simple first-level loggers" in {
    "test" should select logger ("test")
    "a" should select logger ("a")
  }
  it should "handle second-level loggers" in {
    "a.b" should select logger ("a", "b")
    "org.log4s" should select logger ("org", "log4s")
  }
  it should "handle third-level loggers" in {
    "a.b.c" should select logger ("a", "b", "c")
    "org.log4s.log4sjs" should select logger ("org", "log4s", "log4sjs")
  }
  it should "handle simple alphanumeric paths" in {
    forAll(simpleLoggerPath) { cat =>
      val loggerName = makePath(cat)
      loggerName should select loggerOf cat
    }
  }
  it should "handle complicated arbitrary paths" in {
    forAll(complexLoggerPath) { cat =>
      val loggerName = makePath(cat)
      loggerName should select loggerOf cat
    }
  }
}
