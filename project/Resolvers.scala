import sbt._

object Resolvers {
  val sonatypeSnaps   = Resolver.sonatypeRepo("snapshots")
  val sonatypeRelease = Resolver.sonatypeRepo("releases")
  val sonatypeStaging = "Sonatype Staging" at "https://oss.sonatype.org/service/local/staging/deploy/maven2"
}

