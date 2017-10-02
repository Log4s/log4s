scalacOptions in ThisBuild ++= Seq("-unchecked", "-deprecation")

addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "5.2.2")

addSbtPlugin("com.typesafe.sbt" % "sbt-site" % "1.3.1")

addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.1.0")

addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "2.0")

addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.6")
