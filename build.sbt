import Dependencies._

lazy val log4s = (project in file ("."))
  .enablePlugins(BasicSettings)
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
