import sbt._

import sbtrelease.ReleasePlugin.autoImport._
import com.typesafe.sbt.SbtPgp.autoImport._

object Release {
  lazy val settings = Seq (
    releaseCrossBuild             := true,
    releasePublishArtifactsAction := PgpKeys.publishSigned.value
  )
}
