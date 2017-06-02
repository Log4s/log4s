import sbt._
import Keys._

object Helpers {
  def getProp(name: String): Option[String] = sys.props.get(name) orElse sys.env.get(name)
  def parseBool(str: String): Boolean = Set("yes", "y", "true", "t", "1") contains str.trim.toLowerCase
  def boolFlag(name: String): Option[Boolean] = getProp(name) map { parseBool _ }
  def boolFlag(name: String, default: Boolean): Boolean = boolFlag(name) getOrElse default
  def opts(names: String*): Option[String] = names.view.map(getProp _).foldLeft(None: Option[String]) { _ orElse _ }

  lazy val isJenkins = sys.env.contains("BUILD_NUMBER")

  import scala.xml._
  def excludePomDeps(exclude: (String, String) => Boolean): Node => Node = { node: Node =>
    val rewriteRule = new transform.RewriteRule {
      override def transform(n: Node): NodeSeq = {
        if ((n.label == "dependency") && exclude((n \ "groupId").text, (n \ "artifactId").text))
          NodeSeq.Empty
        else
          n
      }
    }
    val transformer = new transform.RuleTransformer(rewriteRule)
    transformer.transform(node)(0)
  }

  final lazy val sver = Def.map(scalaBinaryVersion)(SVer.apply _)

  sealed trait Backend
  case object NewBackend extends Backend
  case object SupportsNewBackend extends Backend
  case object OldBackend extends Backend

  sealed trait SVer {
    val backend: Backend
    val requireJava8: Boolean

    def supportsNewBackend = backend match {
      case NewBackend         => true
      case SupportsNewBackend => true
      case OldBackend         => false
    }
  }
  object SVer {
    def apply(scalaVersion: String): SVer = {
      CrossVersion.partialVersion(scalaVersion) match {
        case Some((2, 10))           => SVer2_10
        case Some((2, 11))           => SVer2_11
        case Some((2, n)) if n >= 12 => SVer2_12
        case _ =>
          throw new IllegalArgumentException(s"Scala version $scalaVersion is not supported")
      }
    }
  }
  case object SVer2_10 extends SVer {
    override final val backend = OldBackend
    override final val requireJava8 = false
  }
  case object SVer2_11 extends SVer {
    override final val backend = SupportsNewBackend
    override final val requireJava8 = false
  }
  case object SVer2_12 extends SVer {
    override final val backend = NewBackend
    override final val requireJava8 = true
  }
}
