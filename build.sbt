import Dependencies._

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
    libraryDependencies ++= Seq (
      slf4j,
      logback
    )
  )
