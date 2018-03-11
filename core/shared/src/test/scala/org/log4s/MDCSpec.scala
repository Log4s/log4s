package org.log4s

import org.scalatest._

/** Test specs for mapped diagnostic contexts.
  *
  * @author Sarah Gerweck <sarah@atscale.com>
  */
class MDCSpec extends FlatSpec with Matchers {
  behavior of "MDC"

  it should "start out empty" in {
    MDC should have size 0
    MDC shouldEqual Map.empty
  }

  it should "restore empty contexts" in {
    MDC shouldBe empty
    MDC.withCtx("a" -> "1", "b" -> "2") {
      MDC shouldEqual Map("a" -> "1", "b" -> "2")
    }
    MDC shouldBe empty
  }

  it should "restore conflicting contexts" in {
    MDC.clear
    MDC("a") = "old"
    MDC("b") = "old"
    MDC shouldEqual Map("a" -> "old", "b" -> "old")
    MDC.withCtx("b" -> "new", "c" -> "new") {
      MDC shouldEqual Map("a" -> "old", "b" -> "new", "c" -> "new")
    }
    MDC shouldEqual Map("a" -> "old", "b" -> "old")
    MDC.clear
  }

  it should "handle duplicate keys in context" in {
    MDC shouldBe empty
    MDC.withCtx("a" -> "1", "b" -> "2", "a" -> "1", "a" -> "3") {
      MDC shouldEqual Map("a" -> "3", "b" -> "2")
    }
    MDC shouldBe empty
  }

  it should "set values" in {
    MDC.clear
    MDC get "a" shouldBe empty
    MDC("a") = "new"
    MDC get "a" should not be empty
    MDC("a") shouldEqual "new"
  }

  it should "clear maps" in {
    MDC("a") = "1"
    MDC("b") = "2"
    MDC.size should be >= 2
    MDC.clear
    MDC shouldBe empty
  }

  it should "support map operations" in {
    MDC.clear
    MDC("a") = "1"
    MDC should have size 1
    MDC should contain key ("a")
    MDC("a") shouldEqual "1"
    MDC shouldEqual Map("a" -> "1")
    MDC ++= Vector("b" -> "2", "c" -> "3")
    MDC should have size 3
    MDC.keys should contain allOf ("a", "b", "c")
    MDC.values should contain allOf ("1", "2", "3")
    MDC shouldEqual Map("a" -> "1", "b" -> "2", "c" -> "3")
    MDC --= Vector("a", "c")
    MDC should not contain key ("a")
    MDC should not contain key ("c")
    MDC should have size 1
    MDC shouldEqual Map("b" -> "2")
  }

  it should "remove values" in {
    MDC.clear
    MDC("a") = "1"
    MDC("b") = "2"
    MDC should have size 2
    MDC should contain key ("b")
    MDC -= "b"
    MDC should have size 1
    MDC should not contain key ("b")
    MDC shouldEqual Map("a" -> "1")
  }
}
