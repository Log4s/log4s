import sbt._
import Keys._

object Dependencies {
  final val slf4jVersion      = "1.7.25"
  final val logbackVersion    = "1.2.3"
  final val scalacheckVersion = "1.14.0"
  final val scalatestVersion  = "3.0.8"

  val slf4j     = "org.slf4j"      % "slf4j-api"       % slf4jVersion
  val logback   = "ch.qos.logback" % "logback-classic" % logbackVersion

  val reflect = Def.map(scalaVersion)("org.scala-lang" % "scala-reflect" % _)
}
