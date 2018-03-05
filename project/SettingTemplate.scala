/* Note: This file is shared among many projects. Avoid putting project-specific things here. */

import sbt._
import sbt.Keys._

import java.net.URL

trait SettingTemplate {
  val buildOrganization: String
  val buildOrganizationName: String
  val buildOrganizationUrl: Option[URL] = None
  val projectDescription: String
  val projectStartYear: Int
  val projectHomepage: Option[URL] = None

  val buildScalaVersion: String
  val extraScalaVersions: Seq[String] = Seq.empty
  val minimumJavaVersion: String = "1.8"
  val defaultOptimize: Boolean = true
  val defaultOptimizeGlobal: Boolean = false
  val inlinePatterns: Seq[String] = Seq("!akka.**","!slick.**")
  val defaultDisableAssertions: Boolean = false
  val defaultWarnUnused: Boolean = false
  val defaultWarnImport: Boolean = false
  val defaultWarnInline: Boolean = false
  val extraScalacOptions: Seq[String] = Seq.empty
  val autoAddCompileOptions: Boolean = true

  val parallelBuild: Boolean = true
  val cachedResolution: Boolean = true
  val sonatypeResolver: Boolean = false

  val projectLicenses: Seq[(String, URL)]

  val defaultNewBackend: Boolean = false

  val developerInfo: scala.xml.Elem

  val buildMetadata: Seq[Setting[_]]

  def sourceLocation(branch: String): Option[URL] = None
}

object SettingTemplate {
  trait GithubProject extends SettingTemplate {
    val githubOrganization: String
    val githubProject: String

    val githubOrgPageFallback: Boolean = true
    lazy val githubPage = url(s"https://github.com/${githubOrganization}/${githubProject}")
    override def sourceLocation(branch: String) = Some(url(s"${githubPage.toExternalForm}/blob/$branch"))

    lazy val buildMetadata = Vector(
      licenses    := projectLicenses,
      homepage    := Some(projectHomepage.getOrElse(githubPage)),
      description := projectDescription,
      startYear   := Some(projectStartYear),
      scmInfo     := Some(ScmInfo(githubPage, s"scm:git:git@github.com:${githubOrganization}/${githubProject}.git"))
    )
  }

  trait ApacheLicensed extends SettingTemplate {
    final val projectLicenses = Seq("Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))
  }
}
