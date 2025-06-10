import sbt.{Resolvers => _, _}
import Keys._

object Publish {
  import Helpers._

  val sonaCreds = (
    for {
      user <- getProp("SONATYPE_USER")
      pass <- getProp("SONATYPE_PASS")
    } yield {
      credentials +=
          Credentials("Sonatype Central Portal",
                      "central.sonatype.com",
                      user, pass)
    }
  ).toSeq

  val settings = sonaCreds ++ Seq (
    publishMavenStyle      := true,
    pomIncludeRepository   := { _ => false },
    Test / publishArtifact := false,

    publishTo              := {
      val centralSnapshots = "https://central.sonatype.com/repository/maven-snapshots/"
      if (isSnapshot.value) Some("central-snapshots" at centralSnapshots)
      else localStaging.value
    },

    pomExtra               := BasicSettings.developerInfo
  )

  /** Use this if you don't want to publish a certain module.
    * (SBT's release plugin doesn't handle this well.)
    */
  val falseSettings = settings ++ Seq (
    Compile / publishArtifact := false,
    Test / publishArtifact := false,
    publishTo := Some(Resolver.file("phony-repo", file("target/repo")))
  )
}
