object Helpers {
  def getProp(name: String): Option[String] = sys.props.get(name) orElse sys.env.get(name)
  def parseBool(str: String): Boolean = Set("yes", "y", "true", "t", "1") contains str.trim.toLowerCase
  def boolFlag(name: String): Option[Boolean] = getProp(name) map { parseBool _ }
  def boolFlag(name: String, default: Boolean): Boolean = boolFlag(name) getOrElse default
  def opts(names: String*): Option[String] = names.view.map(getProp _).foldLeft(None: Option[String]) { _ orElse _ }

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

  sealed trait SVer {
    val supportsNewBackend: Boolean = false
    val requireJava8: Boolean = true
    val newOptimize: Boolean = false
  }
  object SVer {
    def apply(scalaVersion: String): SVer = {
      scalaVersion match {
        case "2.10"       => SVer2_10
        case "2.11"       => SVer2_11
        case "2.12"       => SVer2_12
      }
    }
  }
  case object SVer2_10 extends SVer {
    override val requireJava8 = false
  }
  case object SVer2_11 extends SVer {
    override val supportsNewBackend = true
    override val requireJava8 = false
  }
  case object SVer2_12 extends SVer {
    override val newOptimize = true
  }
}
