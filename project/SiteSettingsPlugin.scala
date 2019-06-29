import sbt._
import Keys._

import com.typesafe.sbt.site._

object SiteSettingsPlugin extends AutoPlugin {
  override def trigger = allRequirements
  override def requires = SiteScaladocPlugin

  lazy val includeSiteDiagrams = Def.settingKey[Boolean]("Whether to include Scaladoc diagrams")

  override lazy val globalSettings = Seq(
    includeSiteDiagrams := false
  )

  override lazy val projectSettings = Seq(
    scalacOptions in (Compile,doc) ++= {
      var s =
        Seq(
          "-groups",
          "-implicits",
          "-sourcepath", (baseDirectory in ThisBuild).value.getAbsolutePath
        )
      if (includeSiteDiagrams.value) {
        s :+= "-diagrams"
      }
      s
    },
    scalacOptions in (Compile,doc) ++= (
      (BasicSettings: SettingTemplate).sourceLocation("master") match {
        case Some(url) =>
          val srcUrl = url + "€{FILE_PATH}.scala"
          Seq("-doc-source-url", srcUrl)
        case None =>
          Seq.empty
      }
    )
  )
}
