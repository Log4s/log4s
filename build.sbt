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

lazy val root: Project = (project in file ("."))
  .enablePlugins(BasicSettings)
  .settings(Publish.settings: _*)
  .settings(Release.settings: _*)
  .aggregate(core, testing)
  .settings (
    name := "Log4s Root",

    publish := {},
    publishLocal := {},
    publishArtifact := false,

    exportJars := false,

    skip in Compile := true,
    skip in Test := true
  )

lazy val core: Project = (project in file ("core"))
  .enablePlugins(BasicSettings, SiteSettingsPlugin)
  .dependsOn(testing % "test")
  .settings(Publish.settings: _*)
  .settings(Release.settings: _*)
  .settings(
    name := "Log4s",

    mimaPreviousArtifacts := Set(organization.value %% artifact.value.name % "1.5.0"),

    libraryDependencies ++= Seq (
      slf4j,
      logback                     % "test",
      scalatest                   % "test",
      reflect(scalaVersion.value) % "provided"
    ),

    unmanagedSourceDirectories in Compile += {
      scalaBinaryVersion.value match {
        case "2.10" => baseDirectory.value / "src" / "main" / "scala-2.10"
        case _      => baseDirectory.value / "src" / "main" / "scala-2.11"
      }
    }
  )

lazy val testing: Project = (project in file ("testing"))
  .enablePlugins(BasicSettings, SiteSettingsPlugin)
  .settings(Publish.settings: _*)
  .settings(Release.settings: _*)
  .settings(
    name := "Log4s Testing",
    description := "Utilities to help with build-time unit tests for logging",
    libraryDependencies ++= Seq (
      slf4j,
      logback
    )
  )
