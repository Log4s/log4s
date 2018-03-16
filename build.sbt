import Dependencies._
import ReleaseTransformations._

/* TODO: Attempts to do this with the existing `TaskKey` have failed, but that
 * would be better than doing it using the string. This approach also won't
 * work if you were to dynamically modify the cross-build settings. The key is
 * autoimported and named `mimaReportBinaryIssues`. */
lazy val binaryCompatStep = releaseStepCommandAndRemaining("+mimaReportBinaryIssues")
/* This is the standard release process plus a binary compat check after tests */
releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  binaryCompatStep,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  publishArtifacts,
  setNextVersion,
  commitNextVersion,
  pushChanges
)

def jsOpts = new Def.SettingList(Seq(
  scalacOptions += "-P:scalajs:sjsDefinedByDefault",
  scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) }
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
    skip in Test := true
  )

lazy val core = (crossProject in file ("core"))
  .enablePlugins(BasicSettings, SiteSettingsPlugin)
  .dependsOn(testing % "test")
  .settings(Publish.settings: _*)
  .settings(Release.settings: _*)
  .settings(
    name := "Log4s",

    mimaPreviousArtifacts := {
      /* I'm using the first & last version of each minor release rather than
       * including every single patch-level update. */
      def `2.11Versions` = Set("1.0.3", "1.0.5", "1.1.0", "1.1.5", "1.2.0", "1.2.1", "1.3.0")
      def `2.12Versions` = Set("1.3.3", "1.3.6", "1.4.0", "1.5.0")
      val checkVersions = scalaBinaryVersion.value match {
        case "2.10" | "2.11" => `2.11Versions` ++ `2.12Versions`
        case "2.12"          => `2.12Versions`
        case "2.13.0-M2"     => Set("1.5.0")
        case other           =>
          sLog.value.info(s"No known MIMA artifacts for: $other")
          Set.empty
      }
      checkVersions.map(organization.value %% artifact.value.name % _)
    },
    mimaBinaryIssueFilters ++= {
      import com.typesafe.tools.mima.core._
      import ProblemFilters.exclude
      /* import com.typesafe.tools.mima.core.ProblemFilters._ */
      /* These macros are not part of the runtime and are not a binary compatibility concern */
      Seq(
        exclude[IncompatibleResultTypeProblem]("org.log4s.LoggerMacros.*"),
        exclude[IncompatibleMethTypeProblem]("org.log4s.LoggerMacros.getLoggerImpl"),
        exclude[ReversedMissingMethodProblem]("org.log4s.LogLevel.$js$exported$prop$name")
      )
    },

    libraryDependencies ++= Seq (
      slf4j,
      logback          %   "test",
      "org.scalacheck" %%% "scalacheck" % scalacheckVersion % "test",
      "org.scalatest"  %%% "scalatest"  % scalatestVersion.value % "test",
      reflect.value    %   "provided"
    ),

    unmanagedSourceDirectories in Compile ++= {
      scalaBinaryVersion.value match {
        case "2.10" | "2.11" =>
          Seq.empty
        case _ =>
          Seq(baseDirectory.value / ".." / "shared" / "src" / "main" / "scala-2.11")
      }
    }
  )
  .jvmSettings(
    libraryDependencies += "org.scala-js" %% "scalajs-stubs" % scalaJSVersion % "provided"
  )
  .jsSettings(jsOpts)

lazy val coreJS = core.js
lazy val coreJVM = core.jvm

lazy val testing = (crossProject in file ("testing"))
  .enablePlugins(BasicSettings, SiteSettingsPlugin)
  .settings(Publish.settings: _*)
  .settings(Release.settings: _*)
  .settings(
    name := "Log4s Testing",
    description := "Utilities to help with build-time unit tests for logging",
    mimaPreviousArtifacts := Set(organization.value %% artifact.value.name % "1.5.0"),
    libraryDependencies ++= Seq (
      slf4j,
      logback
    )
  )
  .jsSettings(jsOpts)

lazy val testingJS = testing.js
lazy val testingJVM = testing.jvm
