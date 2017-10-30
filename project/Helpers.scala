import sbt._
import sbt.Keys._

import HelpersImpl._
object Helpers extends AnyRef with PropertyHelper with VersionHelper with PomHelper

/* Note: This file is shared among many projects. Avoid putting project-specific things here. */

object HelpersImpl {
  sealed trait PropertyHelper {
    private[this] lazy val boolNames = Set("yes", "y", "true", "t", "1")

    def getProp(name: String): Option[String] = sys.props.get(name) orElse sys.env.get(name)
    def parseBool(str: String): Boolean = boolNames(str.trim.toLowerCase)
    def boolFlag(name: String): Option[Boolean] = getProp(name).map(parseBool)
    def boolFlag(name: String, default: Boolean): Boolean = boolFlag(name) getOrElse default
    def intFlag(name: String) = getProp(name).map(_.toInt)
    def intFlag(name: String, default: Int): Int = intFlag(name) getOrElse default
    def opts(names: String*): Option[String] = names.collectFirst(Function.unlift(getProp))

    lazy val buildNumberOpt = sys.env.get("BUILD_NUMBER")
    lazy val isJenkins      = buildNumberOpt.isDefined
  }

  sealed trait VersionHelper {
    final lazy val sver: Def.Initialize[SVer] = {
      Def.map(scalaBinaryVersion)(SVer.apply)
    }

    final lazy val forceOldInlineSyntax: Def.Initialize[Boolean] = {
      val pat = """(?x)^ (?: 2\.12\.[0-2] (?:[^\d].*)? ) | (?: 2\.13\.0 .+ ) $""".r
      Def.map(scalaVersion) {
        case pat() => true
        case _     => false
      }
    }

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
          case Some((2, 12))           => SVer2_12
          case Some((2, n)) if n >= 13 => SVer2_13
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
    case object SVer2_13 extends SVer {
      override final val backend = NewBackend
      override final val requireJava8 = true
    }
  }

  sealed trait PomHelper {
    import scala.xml.{ Node, NodeSeq }
    import scala.xml.transform.{ RewriteRule, RuleTransformer }

    def excludePomDeps(exclude: (String, String) => Boolean): Node => Node = { node: Node =>
      def shouldExclude(n: Node): Boolean =
        n.label == "dependency" && exclude((n \ "groupId").text, (n \ "artifactId").text)

      val rewriteRule = new RewriteRule {
        override def transform(n: Node): NodeSeq = if (shouldExclude(n)) NodeSeq.Empty else n
      }
      val transformer = new RuleTransformer(rewriteRule)
      transformer.transform(node).head
    }
  }
}
