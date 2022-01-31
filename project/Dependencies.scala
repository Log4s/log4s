import sbt._
import Keys._

object Dependencies {
  final val slf4jVersion                   = "1.7.35"
  final val logbackVersion                 = "1.2.3"
  final val scalacheckVersion              =
    Def.map(scalaBinaryVersion) {
      case "2.11"      => "1.15.2"
      case _           => "1.15.3"
    }
  final val scalatestVersion               =
    Def.map(scalaBinaryVersion) {
      case "2.11"      => "3.2.3"
      case _           => "3.2.9"
    }
  final val scalatestPlusScalacheckVersion =
    Def.map(scalaBinaryVersion) {
      case "2.11"      => "3.2.3.0"
      case _           => "3.2.9.0"
    }
  final val scalajsStubsVersion            = "1.0.0"

  val slf4j     = "org.slf4j"      % "slf4j-api"       % slf4jVersion
  val logback   = "ch.qos.logback" % "logback-classic" % logbackVersion

  val reflect = Def.map(scalaVersion)("org.scala-lang" % "scala-reflect" % _)
}
