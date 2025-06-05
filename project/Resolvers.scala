import sbt._

object Resolvers {
  val sonatypeSnaps   = Resolver.sonatypeOssRepos("snapshots")
  val sonatypeRelease = Resolver.sonatypeOssRepos("releases")
  val sonatypeStaging = "Sonatype Staging" at "https://oss.sonatype.org/service/local/staging/deploy/maven2"
}

