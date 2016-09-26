import sbt._

import com.typesafe.sbteclipse.plugin.EclipsePlugin
import com.typesafe.sbteclipse.plugin.EclipsePlugin._

object EclipseDefaults extends AutoPlugin {
  override def requires = EclipsePlugin
  override def trigger = allRequirements

  override lazy val buildSettings = Seq(
    EclipseKeys.createSrc            := EclipseCreateSrc.Default,
    EclipseKeys.projectFlavor        := EclipseProjectFlavor.ScalaIDE,
    EclipseKeys.executionEnvironment := Some(EclipseExecutionEnvironment.JavaSE18),
    EclipseKeys.withSource           := true,
    EclipseKeys.withJavadoc          := true
  )
}
