import Dependencies._
import ReleaseTransformations._

import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

/* TODO: Attempts to do this with the existing `TaskKey` have failed, but that
 * would be better than doing it using the string. This approach also won't
 * work if you were to dynamically modify the cross-build settings. The key is
 * autoimported and named `mimaReportBinaryIssues`. */
lazy val binaryCompatStep = releaseStepCommandAndRemaining("+mimaReportBinaryIssues")

// Workarounds for Dotty being incomplete
lazy val testIfRelevantStep = releaseStepCommandAndRemaining("+testIfRelevant")
lazy val publishIfRelevantStep = releaseStepCommandAndRemaining("+publishSignedIfRelevant")

/* This is the standard release process plus a binary compat check after tests */
releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  testIfRelevantStep,
  binaryCompatStep,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  publishIfRelevantStep,
  setNextVersion,
  commitNextVersion,
  pushChanges
)
val prevVersions = settingKey[Set[String]]("The previous versions for the current project")
val prevArtifacts = Def.derive {
  mimaPreviousArtifacts := prevVersions.value.map(organization.value %%% artifact.value.name % _)
}

def jsOpts = new Def.SettingList(Seq(
  scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
  crossScalaVersions := crossScalaVersions.value.filterNot(_ == "3.0.0-M1")
))

lazy val root: Project = (project in file ("."))
  .enablePlugins(BasicSettings)
  .settings(Publish.settings: _*)
  .settings(Release.settings: _*)
  .aggregate(coreJVM, coreJS, testingJVM, testingJS)
  .settings (
    name := "Log4s Root",

    publish := {},
    publishLocal := {},
    publishArtifact := false,

    exportJars := false,

    skip in Compile := true,
    skip in Test := true,

    mimaFailOnNoPrevious := false
  )

lazy val jsPrevVersions = Set.empty[String]

lazy val core = (crossProject(JSPlatform, JVMPlatform) in file ("core"))
  .enablePlugins(BasicSettings, SiteSettingsPlugin)
  .dependsOn(testing % "test")
  .settings(Publish.settings: _*)
  .settings(Release.settings: _*)
  .settings(
    name := "Log4s",
    prevArtifacts,
    mimaBinaryIssueFilters ++= {
      import com.typesafe.tools.mima.core._
      import ProblemFilters.exclude
      /* import com.typesafe.tools.mima.core.ProblemFilters._ */
      Seq(
        /* These macros are not part of the runtime and are not a binary compatibility concern */
        exclude[IncompatibleResultTypeProblem]("org.log4s.LoggerMacros.*"),
        exclude[IncompatibleMethTypeProblem]("org.log4s.LoggerMacros.getLoggerImpl"),

        /* These false positives happened in log4s-1.7.0 when we
         * upgraded to Scala 2.12.8.
         *
         * See https://github.com/lightbend/mima/issues/388 */
        exclude[DirectMissingMethodProblem]("org.log4s.MDC.+"),
        exclude[DirectMissingMethodProblem]("org.log4s.MDC.++"),
        exclude[DirectMissingMethodProblem]("org.log4s.MDC.-"),
        exclude[DirectMissingMethodProblem]("org.log4s.MDC.--"),
        exclude[DirectMissingMethodProblem]("org.log4s.MDC.andThen"),
        exclude[DirectMissingMethodProblem]("org.log4s.MDC.clone"),
        exclude[DirectMissingMethodProblem]("org.log4s.MDC.empty"),
        exclude[DirectMissingMethodProblem]("org.log4s.MDC.filterNot"),
        exclude[DirectMissingMethodProblem]("org.log4s.MDC.seq"),
        exclude[DirectMissingMethodProblem]("org.log4s.MDC.updated"),
        exclude[DirectMissingMethodProblem]("org.log4s.MDC.view"),
      )
    },

    libraryDependencies ++= Seq (
      slf4j,
      logback             %   "test",
      "org.scalacheck"    %%% "scalacheck"      % scalacheckVersion.value          % "test",
    ),
    libraryDependencies ++= {
      if (isDotty.value) Seq.empty
      else Seq(reflect.value % Provided)
    },

    unmanagedSourceDirectories in Compile ++= {
      scalaBinaryVersion.value match {
        case s if s.startsWith("2.") =>
          Seq(baseDirectory.value / ".." / "shared" / "src" / "main" / "scala-2")
        case s if s.startsWith("3.") =>
          Seq(baseDirectory.value / ".." / "shared" / "src" / "main" / "scala-3")
      }
    },

    unmanagedSourceDirectories in Compile ++= {
      scalaBinaryVersion.value match {
        case "2.11" | "2.12" =>
          Seq(baseDirectory.value / ".." / "shared" / "src" / "main" / "scala-oldcoll")
        case _ =>
          Seq(baseDirectory.value / ".." / "shared" / "src" / "main" / "scala-newcoll")
      }
    }

  )
  .jvmSettings(
    libraryDependencies += ("org.scala-js" %% "scalajs-stubs" % scalajsStubsVersion % "provided").withDottyCompat(scalaVersion.value),
    libraryDependencies ++= Seq(
      "org.scalatest"     %%% "scalatest"       % scalatestVersion.value               % Test,
      "org.scalatestplus" %%% "scalacheck-1-15" % scalatestPlusScalacheckVersion.value % Test
    ),
    prevVersions := {
      /* I'm using the first & last version of each minor release rather than
       * including every single patch-level update. */
      def `2.11Versions` =
        Set("1.0.3", "1.0.5",
            "1.1.0", "1.1.5",
            "1.2.0", "1.2.1",
            "1.3.0")
      def `2.12Versions` =
        Set("1.3.3", "1.3.6",
            "1.4.0",
            "1.5.0",
            "1.6.0", "1.6.1",
            "1.7.0",
            "1.8.0", "1.8.1")
      def `2.13Versions` =
        Set("1.8.2",
            "1.9.0")
      def DottyVersions =
        Set.empty[String]
      scalaBinaryVersion.value match {
        case "2.11"                   => `2.11Versions` ++ `2.12Versions` ++ `2.13Versions` ++ DottyVersions
        case "2.12"                   => `2.12Versions` ++ `2.13Versions` ++ DottyVersions
        case "2.13"                   => `2.13Versions` ++ DottyVersions
        case "3.0.0-M3" | "3.0.0-RC1" => DottyVersions
        case other                    =>
          sLog.value.info(s"No known MIMA artifacts for: $other")
          Set.empty
      }
    }
  )
  .jsSettings(jsOpts)
  .jsSettings(
    prevVersions := jsPrevVersions,
    libraryDependencies ++= {
      if (!isDotty.value) Seq(
        "org.scalatest"     %%% "scalatest"       % scalatestVersion.value               % Test,
        "org.scalatestplus" %%% "scalacheck-1-15" % scalatestPlusScalacheckVersion.value % Test,
      )
      else Seq.empty
    }
  )

lazy val coreJS = core.js
lazy val coreJVM = core.jvm

lazy val testing = (crossProject(JSPlatform, JVMPlatform) in file ("testing"))
  .enablePlugins(BasicSettings, SiteSettingsPlugin)
  .settings(Publish.settings: _*)
  .settings(Release.settings: _*)
  .settings(
    name := "Log4s Testing",
    description := "Utilities to help with build-time unit tests for logging",
    prevArtifacts,
    libraryDependencies ++= Seq (
      slf4j,
      logback
    )
  )
  .jvmSettings(
    prevVersions := {
      val `2.12Versions` = Set("1.5.0", "1.6.0", "1.6.1", "1.7.0", "1.8.0", "1.8.1")
      val `2.13Versions` = Set("1.8.2")
      val DottyVersions  = Set.empty[String]
      scalaBinaryVersion.value match {
        case "2.11" | "2.12"          => `2.12Versions` ++ `2.13Versions` ++ DottyVersions
        case "2.13"                   => `2.13Versions` ++ DottyVersions
        case "3.0.0-M3" | "3.0.0-RC1" => DottyVersions
        case other =>
          Set.empty
      }
    }
  )
  .jsSettings(jsOpts)
  .jsSettings(
    prevVersions := jsPrevVersions
  )

lazy val testingJS = testing.js
lazy val testingJVM = testing.jvm
