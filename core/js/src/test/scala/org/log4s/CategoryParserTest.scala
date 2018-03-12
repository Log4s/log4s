package org.log4s.log4sjs

import scala.collection.immutable.{ Seq => ISeq }

import org.scalacheck._
import org.scalacheck.Arbitrary._

import org.scalatest._
import org.scalatest.prop.PropertyChecks
import org.scalatest.Matchers._

private object CategoryParserSpec {
  private[this] final val catParser = CategoryParser

  object select
  final class select(val cat: String) extends AnyVal {
    def category(s: String*): Assertion = {
      catParser(cat) should equal (s.to[ISeq])
    }
    @inline
    def category(s: ISeq[String]): Assertion = this.category(s: _*)
  }

  implicit final class CategoryString(val cat: String) extends AnyVal {
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

import CategoryParserSpec._

class CategoryParserSpec extends FlatSpec with PropertyChecks {
  it should "handle the root category correctly" in {
    "" should select category ()
  }
  it should "handle simple first-level categories" in {
    "test" should select category ("test")
    "a" should select category ("a")
  }
  it should "handle second-level categories" in {
    "a.b" should select category ("a", "b")
    "org.log4s" should select category ("org", "log4s")
  }
  it should "handle third-level categories" in {
    "a.b.c" should select category ("a", "b", "c")
    "org.log4s.log4sjs" should select category ("org", "log4s", "log4sjs")
  }
  it should "handle simple alphanumeric paths" in {
    forAll(simpleLoggerPath) { cat =>
      val categoryName = makePath(cat)
      categoryName should select category cat
    }
  }
  it should "handle complicated arbitrary paths" in {
    forAll(complexLoggerPath) { cat =>
      val categoryName = makePath(cat)
      categoryName should select category cat
    }
  }
}
