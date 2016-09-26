import sbt._

object Dependencies {
  final val slf4jVersion     = "1.7.21"
  final val logbackVersion   = "1.1.7"

  val slf4j     = "org.slf4j"      %  "slf4j-api"       % slf4jVersion
  val logback   = "ch.qos.logback" %  "logback-classic" % logbackVersion

  def reflect(ver: String) = "org.scala-lang" % "scala-reflect" % ver

  def scalaTest(scalaBinaryVersion: String): ModuleID = scalaBinaryVersion match {
    case "2.12.0-M1" => "org.scalatest" %% "scalatest" % "2.2.5-M1"
    case "2.12.0-M2" => "org.scalatest" %% "scalatest" % "2.2.5-M2"
    case "2.12.0-M3" => "org.scalatest" %% "scalatest" % "2.2.5-M3"
    case "2.12.0-M4" => "org.scalatest" %% "scalatest" % "2.2.6"
    case _           => "org.scalatest" %% "scalatest" % "3.0.0"
  }
}