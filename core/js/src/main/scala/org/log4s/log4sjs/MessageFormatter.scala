package org.log4s
package log4sjs

import scala.annotation.tailrec
import scala.scalajs.js

trait MessageFormatter extends js.Any {
  def render(le: LoggedEvent): String
}
object MessageFormatter extends FunctionalType[MessageFormatter, LoggedEvent, String]("MessageFormatter", 'render) {
  protected[this] def fromFunction(fn: LoggedEvent => String) =
    new js.Object with MessageFormatter { def render(le: LoggedEvent) = fn(le) }
}

object StandardMessageFormatter {
  sealed trait MDCFormat
  object MDCFormat {
    case object NoMDC extends MDCFormat
    case object NonEmptyMDC extends MDCFormat
    case object AlwaysMDC extends MDCFormat
  }
}

class StandardMessageFormatter(
    val useLocalizedThrowableMessages: Boolean = false,
    val mdcFormat: StandardMessageFormatter.MDCFormat = StandardMessageFormatter.MDCFormat.AlwaysMDC)
  extends js.Object with MessageFormatter {

  private[this] final val indentSize = 8
  private[this] final val indentString = " " * indentSize

  override def render(le: LoggedEvent): String = {
    val t = le.throwable
    val message = le.message

    def throwablePart = renderExceptionInfo(t).map("\n" + _).getOrElse("")

    def levelPart = le.level.name.toUpperCase

    def timePart = {
      le.timestamp
        .toISOString()
        .init
        .map { case 'T' => ' '; case '.' => ','; case other => other }
    }

    def loggerPart = le.loggerName

    def mdcPart = mdcBuilder(le.mdc)

    f"$timePart $levelPart%-5s$mdcPart $loggerPart - $message$throwablePart"
  }

  protected[this] def renderExceptionInfo(ei: ExceptionInfo): Option[String] = {
    import ExceptionInfo._
    ei match {
      case NoException => None
      case te: ThrowableException => Some(renderStackTrace(te.throwable))
      case ee: JsErrorException => Some(renderJsError(ee.error))
    }
  }

  protected[this] def renderStackTrace(t: Throwable): String = {
    (t +: unfoldCauses(t)).map(renderOneStack).mkString("Caused by: ")
  }

  protected[this] def renderJsError(jse: js.Error): String ={
    val stackPart =
      (jse.asInstanceOf[js.Dynamic].stack: Any) match {
        case s: String => Some(s)
        case other     => None
      }
    stackPart.getOrElse(s"{jse.name}: ${jse.message}")
  }

  protected[this] def renderOneStack(t: Throwable): String = {
    val className = t.getClass.getName
    val message = if (useLocalizedThrowableMessages) t.getLocalizedMessage else t.getMessage
    val stack = {
      val frames = t.getStackTrace
      frames flatMap { frame =>
        val basicInfo = Vector(indentString, "at ", frame.getClassName, ".", frame.getMethodName)
        val positionInfo = {
          Option(frame.getFileName) .map { fn =>
            val linePart =
              frame.getLineNumber match {
                case n if n < 0 => Nil
                case other      => Seq(":", other.toString)
              }
            "(" +: fn +: linePart :+ ")"
          }.toSeq.flatten
        }
        (basicInfo ++ positionInfo :+ "\n")
      }
    }
    s"$className: $message\n${stack.mkString}"
  }

  private[this] lazy val mdcBuilder: Map[String, String] => String = {
    import StandardMessageFormatter.MDCFormat._

    def buildMDC(mdc: Map[String, String]): String =
      mdc
        .foldRight[List[String]](Nil) { case ((k, v), l) =>
          ", " :: k :: "=" :: v :: l
        }
        .tail
        .mkString(" {", "", "}")

    mdcFormat match {
      case NoMDC =>
        Function.const("")
      case NonEmptyMDC =>
        mdc => if (mdc.isEmpty) "" else buildMDC(mdc)
      case AlwaysMDC =>
        mdc => if (mdc.isEmpty) " {}" else buildMDC(mdc)
    }
  }

  private[this] def unfoldCauses(t: Throwable): Seq[Throwable] = {
    def unfold[A, B](b: B)(fn: B => Option[(A, B)]): Seq[A] = {
      @tailrec def helper(b: B, accum: Vector[A]): Seq[A] = {
        fn(b) match {
          case Some((a, b2)) => helper(b2, accum :+ a)
          case None          => accum
        }
      }
      helper(b, Vector.empty)
    }
    unfold(t)(thr => Option(thr.getCause).map(c => (c, c)))
  }
}
