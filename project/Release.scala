import sbt._

import sbtrelease.ReleasePlugin.autoImport._
import com.jsuereth.sbtpgp.SbtPgp.autoImport._

object Release {
  lazy val settings = Seq (
    releaseCrossBuild             := true,
    releasePublishArtifactsAction := PgpKeys.publishSigned.value
  )
}
