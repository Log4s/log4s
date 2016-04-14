import sbt._
import Keys._

import com.typesafe.sbt.SbtSite.site
import sbtrelease.ReleasePlugin.autoImport._
import com.typesafe.sbt.SbtPgp.autoImport._

import scala.util.Properties.envOrNone

sealed trait Basics {
  final val buildOrganization     = "org.log4s"

  final val buildScalaVersion     = "2.11.8"
  final val extraScalaVersions    = Seq("2.10.6", "2.12.0-M1", "2.12.0-M2", "2.12.0-M3", "2.12.0-M4")
  final val minimumJavaVersion    = "1.7"
  final val defaultOptimize       = true
  final val projectMainClass      = None

  final val parallelBuild         = false
  final val cachedResolution      = false

  final val buildOrganizationName = "Log4s"
  final val buildOrganizationUrl  = Some("http://log4s.org/")

  lazy val buildMetadata = Vector(
    licenses     := Seq("Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")),
    homepage     := Some(url("http://log4s.org")),
    description  := "High-performance SLF4J wrapper that provides convenient Scala bindings using macros.",
    startYear    := Some(2013),
    scmInfo      := Some(ScmInfo(url("http://github.com/Log4s/log4s"), "scm:git:git@github.com:Log4s/log4.git"))
  )

  lazy val developerInfo = {
    <developers>
      <developer>
        <id>sarah</id>
        <name>Sarah Gerweck</name>
        <email>sarah.a180@gmail.com</email>
        <url>https://github.com/sarahgerweck</url>
        <timezone>America/Los_Angeles</timezone>
      </developer>
    </developers>
  }
}

object BuildSettings extends Basics {
  import Helpers._

  /* Overridable flags */
  lazy val optimize     = boolFlag("OPTIMIZE") orElse boolFlag("OPTIMISE") getOrElse defaultOptimize
  lazy val deprecation  = boolFlag("NO_DEPRECATION") map (!_) getOrElse true
  lazy val inlineWarn   = boolFlag("INLINE_WARNINGS") getOrElse false
  lazy val debug        = boolFlag("DEBUGGER") getOrElse false
  lazy val debugPort    = envOrNone("DEBUGGER_PORT") map { _.toInt } getOrElse 5050
  lazy val debugSuspend = boolFlag("DEBUGGER_SUSPEND") getOrElse true
  lazy val unusedWarn   = boolFlag("UNUSED_WARNINGS") getOrElse false
  lazy val importWarn   = boolFlag("IMPORT_WARNINGS") getOrElse false

  val buildScalaVersions = buildScalaVersion +: extraScalaVersions

  private[this] val sharedScalacOptions = Seq (
    "-unchecked",
    "-feature"
  ) ++ (
    if (deprecation) Seq("-deprecation") else Seq.empty
  ) ++ (
    if (inlineWarn) Seq("-Yinline-warnings") else Seq.empty
  ) ++ (
    if (unusedWarn) Seq("-Ywarn-unused") else Seq.empty
  ) ++ (
    if (importWarn) Seq("-Ywarn-unused-import") else Seq.empty
  )
  def addScalacOptions() = Def.derive {
    scalacOptions ++= sharedScalacOptions ++ {
      SVer(scalaBinaryVersion.value) match {
        case j8 if j8.requireJava8 =>
          Seq.empty
        case nonJ8 =>
          Seq (
            "-target:jvm-" + minimumJavaVersion
          ) ++ (
            if (optimize) Seq("-optimize") else Seq.empty
          )
      }
    }
  }

  private[this] val sharedJavacOptions = Seq.empty
  def addJavacOptions() = Def.derive {
    javacOptions ++= sharedJavacOptions ++ {
      SVer(scalaBinaryVersion.value) match {
        case j8 if j8.requireJava8 =>
          Seq (
            "-target", "1.8",
            "-source", "1.8"
          )
        case nonJ8 =>
          Seq (
            "-target", minimumJavaVersion,
            "-source", minimumJavaVersion
          )
      }
    }
  }

  lazy val siteSettings = site.settings ++ site.includeScaladoc()

  def sharedBuildSettings = buildMetadata ++
                            siteSettings ++
                            projectMainClass.toSeq.map(mainClass := Some(_)) ++
                            Seq (
    organization         :=  buildOrganization,
    organizationName     :=  buildOrganizationName,
    organizationHomepage :=  buildOrganizationUrl map { url(_) },

    scalaVersion         :=  buildScalaVersion,
    addScalacOptions(),
    addJavacOptions(),
    crossScalaVersions   :=  buildScalaVersions,

    autoAPIMappings      :=  true,

    updateOptions        :=  updateOptions.value.withCachedResolution(cachedResolution),
    parallelExecution    :=  parallelBuild,

    evictionWarningOptions in update :=
      EvictionWarningOptions.default.withWarnTransitiveEvictions(false).withWarnDirectEvictions(false).withWarnScalaVersionEviction(false)
  )

}

object Helpers {
  def getProp(name: String): Option[String] = sys.props.get(name) orElse sys.env.get(name)
  def parseBool(str: String): Boolean = Set("yes", "y", "true", "t", "1") contains str.trim.toLowerCase
  def boolFlag(name: String): Option[Boolean] = getProp(name) map { parseBool _ }
  def boolFlag(name: String, default: Boolean): Boolean = boolFlag(name) getOrElse default
  def opts(names: String*): Option[String] = names.view.map(getProp _).foldLeft(None: Option[String]) { _ orElse _ }

  sealed trait SVer {
    def requireJava8: Boolean
  }
  object SVer {
    def apply(scalaVersion: String): SVer = {
      scalaVersion match {
        case "2.10"      => SVer2_10
        case "2.11"      => SVer2_11
        case "2.12.0-M1" => SVer2_12M1
        case "2.12.0-M2" => SVer2_12M2
        case "2.12.0-M3" => SVer2_12M3
        case "2.12.0-M4" => SVer2_12M4
        case "2.12"      => SVer2_12
      }
    }
  }
  case object SVer2_10 extends SVer {
    def requireJava8 = false
  }
  case object SVer2_11 extends SVer {
    def requireJava8 = false
  }
  case object SVer2_12M1 extends SVer {
    def requireJava8 = true
  }
  case object SVer2_12M2 extends SVer {
    def requireJava8 = true
  }
  case object SVer2_12M3 extends SVer {
    def requireJava8 = true
  }
  case object SVer2_12M4 extends SVer {
    def requireJava8 = true
  }
  case object SVer2_12 extends SVer {
    def requireJava8 = true
  }
}

object Resolvers {
  val sonaSnaps     = "Sonatype Snaps" at "https://oss.sonatype.org/content/repositories/snapshots"
  val sonaStage     = "Sonatype Staging" at "https://oss.sonatype.org/service/local/staging/deploy/maven2"
}

object PublishSettings {
  import BuildSettings._
  import Resolvers._
  import Helpers._

  val sonaCreds = (
    for {
      user <- getProp("SONATYPE_USER")
      pass <- getProp("SONATYPE_PASS")
    } yield {
      credentials +=
          Credentials("Sonatype Nexus Repository Manager",
                      "oss.sonatype.org",
                      user, pass)
    }
  ).toSeq

  val publishSettings = sonaCreds ++ Seq (
    publishMavenStyle       := true,
    pomIncludeRepository    := { _ => false },
    publishArtifact in Test := false,

    publishTo               := {
      if (version.value.trim endsWith "SNAPSHOT")
        Some(sonaSnaps)
      else
        Some(sonaStage)
    },

    pomExtra                := developerInfo
  )
}

object Release {
  val settings = Seq (
    releaseCrossBuild := true,
    releasePublishArtifactsAction := PgpKeys.publishSigned.value
  )
}

object Eclipse {
  import com.typesafe.sbteclipse.plugin.EclipsePlugin._

  val settings = Seq (
    EclipseKeys.createSrc            := EclipseCreateSrc.Default,
    EclipseKeys.projectFlavor        := EclipseProjectFlavor.ScalaIDE,
    EclipseKeys.executionEnvironment := Some(EclipseExecutionEnvironment.JavaSE17),
    EclipseKeys.withSource           := true
  )
}

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
    case _           => "org.scalatest" %% "scalatest" % "2.2.6"
  }
}

object Log4sBuild extends Build {
  build =>

  import BuildSettings._
  import Resolvers._
  import Dependencies._
  import PublishSettings._
  import Helpers._

  lazy val log4s = (project in file ("."))
    .settings(sharedBuildSettings: _*)
    .settings(Eclipse.settings: _*)
    .settings(publishSettings: _*)
    .settings(Release.settings: _*)
    .settings(
      name := "Log4s",

      libraryDependencies ++= Seq (
        slf4j,
        logback                       % "test",
        scalaTest(scalaVersion.value) % "test",
        reflect(scalaVersion.value)   % "provided"
      ),

      unmanagedSourceDirectories in Compile <+= (scalaBinaryVersion, baseDirectory) { (ver, dir) =>
        ver match {
          case "2.10" => dir / "src" / "main" / "scala-2.10"
          case _      => dir / "src" / "main" / "scala-2.11"
        }
      }
    )
}
