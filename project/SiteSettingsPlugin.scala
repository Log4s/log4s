import sbt._
import Keys._

import com.typesafe.sbt.site._

object SiteSettingsPlugin extends AutoPlugin {
  override def trigger = allRequirements
  override def requires = SiteScaladocPlugin

  override lazy val projectSettings = Seq(
    scalacOptions in (Compile,doc) ++= Seq(
      "-groups",
      "-implicits",
      "-diagrams",
      "-sourcepath", (baseDirectory in ThisBuild).value.getAbsolutePath
    ),
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
