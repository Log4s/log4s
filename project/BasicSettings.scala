import sbt._
import Keys._

import scala.util.Properties.envOrNone

import sbtrelease.ReleasePlugin.autoImport._
import com.typesafe.sbt.site._

import Helpers._

sealed trait Basics {
  final val buildOrganization     = "org.log4s"
  final val buildOrganizationName = "Log4s"
  final val buildOrganizationUrl  = Some("http://log4s.org/")
  final val githubOrganization    = "Log4s"
  final val githubProject         = "log4s"
  final val projectDescription    = "High-performance SLF4J wrapper for Scala"
  final val projectStartYear      = 2013

  final val buildScalaVersion     = "2.11.8"
  final val extraScalaVersions    = Seq("2.10.6", "2.12.0-M1", "2.12.0-M2", "2.12.0-M3", "2.12.0-M4", "2.12.0-M5", "2.12.0-RC1", "2.12.0-RC2", "2.12.0")
  final val minimumJavaVersion    = "1.7"
  final val defaultOptimize       = true

  final val parallelBuild         = false
  final val cachedResolution      = true

  final val defaultNewBackend     = false

  /* Metadata definitions */
  lazy val githubPage = url(s"https://github.com/${githubOrganization}/${githubProject}")
  lazy val buildMetadata = Vector(
    licenses    := Seq("Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")),
    homepage    := Some(url("http://log4s.org")),
    description := projectDescription,
    startYear   := Some(projectStartYear),
    scmInfo     := Some(ScmInfo(githubPage, s"scm:git:git@github.com:${githubOrganization}/${githubProject}.git"))
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

object BasicSettings extends AutoPlugin with Basics {
  override def requires = SiteScaladocPlugin

  override lazy val projectSettings = (
    buildMetadata ++
    Seq (
      organization         :=  buildOrganization,
      organizationName     :=  buildOrganizationName,
      organizationHomepage :=  buildOrganizationUrl map { url _ },

      scalaVersion         :=  buildScalaVersion,
      crossScalaVersions   :=  buildScalaVersions,
      addScalacOptions(),
      addJavacOptions(),

      autoAPIMappings      :=  true,

      updateOptions        :=  updateOptions.value.withCachedResolution(cachedResolution),
      parallelExecution    :=  parallelBuild,

      /* Many OSS projects push here and then appear in Maven Central later */
      resolvers            +=  Resolver.sonatypeRepo("releases"),

      evictionWarningOptions in update :=
        EvictionWarningOptions.default.withWarnTransitiveEvictions(false).withWarnDirectEvictions(false).withWarnScalaVersionEviction(false)
    )
  )

  /* Overridable flags */
  lazy val optimize     = boolFlag("OPTIMIZE") orElse boolFlag("OPTIMISE") getOrElse defaultOptimize
  lazy val deprecation  = boolFlag("NO_DEPRECATION") map (!_) getOrElse true
  lazy val inlineWarn   = boolFlag("INLINE_WARNINGS") getOrElse false
  lazy val debug        = boolFlag("DEBUGGER") getOrElse false
  lazy val debugPort    = envOrNone("DEBUGGER_PORT") map { _.toInt } getOrElse 5050
  lazy val debugSuspend = boolFlag("DEBUGGER_SUSPEND") getOrElse true
  lazy val unusedWarn   = boolFlag("UNUSED_WARNINGS") getOrElse false
  lazy val importWarn   = boolFlag("IMPORT_WARNINGS") getOrElse false
  lazy val java8Flag    = boolFlag("BUILD_JAVA_8") getOrElse false
  lazy val newBackend   = boolFlag("NEW_BCODE_BACKEND") getOrElse defaultNewBackend

  lazy val buildScalaVersions = buildScalaVersion +: extraScalaVersions

  def addScalacOptions() = Def.derive {
    scalacOptions ++= {
      val sv = SVer(scalaBinaryVersion.value)
      var options = Seq.empty[String]

      options :+= "-unchecked"
      options :+= "-feature"
      if (deprecation) {
        options :+= "-deprecation"
      }
      if (inlineWarn) {
        options :+= "-Yinline-warnings"
      }
      if (unusedWarn) {
        options :+= "-Ywarn-unused"
      }
      if (importWarn) {
        options :+= "-Ywarn-unused-import"
      }
      if (!sv.requireJava8) {
        options :+= "-target:jvm-" + minimumJavaVersion
      }
      if (optimize) {
        if (sv.supportsNewBackend && newBackend) {
          options :+= "-Yopt:l:classpath"
        } else if (sv.newOptimize) {
          options :+= "-opt:_"
        } else if (!sv.requireJava8) {
          options :+= "-optimize"
        }
      }
      if (sv.supportsNewBackend && newBackend && !sv.requireJava8) {
        options :+= "-Ybackend:GenBCode"
      }

      options
    }
  }

  def addJavacOptions() = Def.derive {
    javacOptions ++= {
      val sv = SVer(scalaBinaryVersion.value)
      var options = Seq.empty[String]

      if (sv.requireJava8) {
        options ++= Seq[String](
          "-target", "1.8",
          "-source", "1.8"
        )
      } else {
        options ++= Seq[String](
          "-target", minimumJavaVersion,
          "-source", minimumJavaVersion
        )
      }

      options
    }
  }
}
