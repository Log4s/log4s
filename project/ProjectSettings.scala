import sbt._

/** Basic metadata about the project that gets pulled into the build */
trait ProjectSettings
    extends SettingTemplate
    with SettingTemplate.ApacheLicensed
    with SettingTemplate.GithubProject {
  override final val buildOrganization     = "org.log4s"
  override final val buildOrganizationName = "Log4s"
  override final val buildOrganizationUrl  = Some(url("http://log4s.org/"))
  override final val projectDescription    = "High-performance SLF4J wrapper for Scala"
  override final val projectStartYear      = 2013
  override final val projectHomepage       = Some(url("http://log4s.org/"))

  override final val githubOrganization    = "Log4s"
  override final val githubProject         = "log4s"

  override final val buildScalaVersion     = "2.12.4"
  override final val extraScalaVersions    = Seq("2.10.6", "2.11.11", "2.13.0-M2")
  override final val minimumJavaVersion    = "1.7"
  override final val defaultOptimize       = true
  override final val defaultOptimizeGlobal = true

  override final val sonatypeResolver      = true

  lazy val developerInfo = {
    <developers>
      <developer>
        <id>sarah</id>
        <name>Sarah Gerweck</name>
        <email>sarah.a180@gmail.com</email>
        <url>https://github.com/sarahgerweck</url>
        <timezone>America/Los_Angeles</timezone>
      </developer>
    </developers>
  }
}
