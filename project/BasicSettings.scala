/* Note: This file is shared among many projects. Avoid putting project-specific things here. */

import sbt._
import sbt.Keys._

import Helpers._

object BasicSettings extends AutoPlugin with BasicSettings {
  override def projectSettings = basicSettings
}

trait BasicSettings extends ProjectSettings { st: SettingTemplate =>
  /* Overridable flags */
  lazy val optimize       = boolFlag("OPTIMIZE") orElse boolFlag("OPTIMISE") getOrElse defaultOptimize
  lazy val optimizeGlobal = boolFlag("OPTIMIZE_GLOBAL") getOrElse defaultOptimizeGlobal
  lazy val optimizeWarn   = boolFlag("OPTIMIZE_WARNINGS") getOrElse false
  lazy val noFatalWarn    = boolFlag("NO_FATAL_WARNINGS") getOrElse false
  lazy val deprecation    = boolFlag("NO_DEPRECATION") map (!_) getOrElse true
  lazy val inlineWarn     = boolFlag("INLINE_WARNINGS") getOrElse false
  lazy val debug          = boolFlag("DEBUGGER") getOrElse false
  lazy val debugPort      = intFlag("DEBUGGER_PORT", 5050)
  lazy val debugSuspend   = boolFlag("DEBUGGER_SUSPEND") getOrElse true
  lazy val unusedWarn     = boolFlag("UNUSED_WARNINGS") getOrElse false
  lazy val importWarn     = boolFlag("IMPORT_WARNINGS") getOrElse false
  lazy val findbugsHtml   = boolFlag("FINDBUGS_HTML") getOrElse !isJenkins
  lazy val newBackend     = boolFlag("NEW_BCODE_BACKEND") getOrElse defaultNewBackend
  lazy val noBuildDocs    = boolFlag("NO_SBT_DOCS").getOrElse(false) && !isJenkins

  lazy val basicSettings = new Def.SettingList(
    buildMetadata ++
    Seq (
      organization         :=  buildOrganization,
      organizationName     :=  buildOrganizationName,
      organizationHomepage :=  buildOrganizationUrl.orElse(if (githubOrgPageFallback) Some(githubOrgPage) else None),

      scalaVersion         :=  buildScalaVersion,
      crossScalaVersions   :=  buildScalaVersions,

      autoAPIMappings      :=  true,

      updateOptions        :=  updateOptions.value.withCachedResolution(cachedResolution),
      parallelExecution    :=  parallelBuild,

      evictionWarningOptions in update :=
        EvictionWarningOptions.default.withWarnTransitiveEvictions(false).withWarnDirectEvictions(false).withWarnScalaVersionEviction(false)
    ) ++ (
      if (noBuildDocs) {
        Seq(sources in (Compile, doc) := Seq.empty)
      } else {
        Seq.empty
      }
    ) ++ (
      if (autoAddCompileOptions) {
        addScalacOptions() ++ addJavacOptions()
      } else {
        Seq.empty
      }
    ) ++ (
      if (sonatypeResolver) {
        /* Many OSS projects push here and then appear in Maven Central later */
        Seq(resolvers += Resolver.sonatypeRepo("releases"))
      } else {
        Seq.empty
      }
    )
  )

  lazy val buildScalaVersions = buildScalaVersion +: extraScalaVersions

  def basicScalacOptions = Def.derive {
    scalacOptions ++= {
      var options = Seq.empty[String]
      val sv = sver.value

      options :+= "-unchecked"
      options :+= "-feature"
      if (deprecation) {
        options :+= "-deprecation"
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
      if (sv.backend == SupportsNewBackend && newBackend) {
        options :+= "-Ybackend:GenBCode"
      }

      options
    }
  }

  def optimizationScalacOptions(optim: Boolean = optimize) = Def.derive {
    scalacOptions ++= {
      var options = Seq.empty[String]
      val sv = sver.value
      val fos = forceOldInlineSyntax.value

      if (optim) {
        def doNewWarn(): Unit = {
          if (optimizeWarn) {
            options :+= "-opt-warnings:_"
          }
        }

        if (sv.backend == NewBackend && !fos) {
          options :+= "-opt:l:inline"

          val inlineFrom = {
            var patterns = Seq.empty[String]
            if (optimizeGlobal) {
              patterns :+= "**"
            } else {
              patterns :+= "<sources>"
            }
            patterns ++= inlinePatterns
            patterns
          }

          options :+= inlineFrom.mkString("-opt-inline-from:", ":", "")

          doNewWarn()
        } else if (sv.backend == NewBackend && fos || sv.backend == SupportsNewBackend && newBackend) {
          if (optimizeGlobal) {
            options :+= "-opt:l:classpath"
          } else {
            options :+= "-opt:l:project"
          }
          doNewWarn()
        } else {
          options :+= "-optimize"
          if (optimizeWarn) {
            options :+= "-Yinline-warnings"
          }
        }
      }

      options
    }
  }

  def addScalacOptions(optim: Boolean = optimize) = new Def.SettingList(Seq(
    basicScalacOptions,
    optimizationScalacOptions(optim)
  ))

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

  private[this] lazy val githubOrgPage = url(s"https://github.com/${githubOrganization}")
}
