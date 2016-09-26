import Dependencies._

lazy val log4s = (project in file ("."))
  .enablePlugins(BasicSettings)
  .settings(Publish.settings: _*)
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
