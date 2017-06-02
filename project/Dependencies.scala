import sbt._

object Dependencies {
  final val slf4jVersion     = "1.7.25"
  final val logbackVersion   = "1.2.3"
  final val scalatestVersion = "3.0.3"

  val slf4j     = "org.slf4j"      %  "slf4j-api"       % slf4jVersion
  val logback   = "ch.qos.logback" %  "logback-classic" % logbackVersion
  val scalatest = "org.scalatest"  %% "scalatest"       % scalatestVersion

  def reflect(ver: String) = "org.scala-lang" % "scala-reflect" % ver
}
