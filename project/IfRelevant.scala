import sbt._
import sbt.Keys._

// Borrowed from sbt-spiewak for the Dotty transition
// https://github.com/djspiewak/sbt-spiewak/blob/7048b251e96773b6ebdc1100b243608628326351/core/src/main/scala/sbtspiewak/SpiewakPlugin.scala
object IfRelevant extends AutoPlugin {
  override def trigger = allRequirements

  object autoImport {
    lazy val testIfRelevant = taskKey[Unit]("A wrapper around the `test` task which checks to ensure the current scalaVersion is in crossScalaVersions")
  }

  def filterTaskWhereRelevant(delegate: TaskKey[Unit]) =
    Def.taskDyn {
      val cross = crossScalaVersions.value
      val ver = scalaVersion.value

      if (cross.contains(ver))
        Def.task(delegate.value)
      else
        Def.task(streams.value.log.warn(s"skipping `${delegate.key.label}` in ${name.value}: $ver is not in $cross"))
    }

  import autoImport._

  override def projectSettings = Seq(
    Test / testIfRelevant := filterTaskWhereRelevant(Test / test).value,
  )
}
