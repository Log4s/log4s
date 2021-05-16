package org.log4s
package log4sjs

import scala.scalajs.js

abstract class FunctionalType[ObjectType <: js.Any, Argument, Result]
                             (val typeName: String, dynamicField: Symbol) { thisFT =>
  type Type = ObjectType
  type FunctionType = js.Function1[Argument, Result]
  type DynamicType = js.|[FunctionType, Type]

  // Workaround for formatter.merge.asInstanceOf[js.Dynamic] since formatter.merge doesn't work on Scala 3.0.0
  // For more information, please check out https://github.com/Log4s/log4s/pull/75#issuecomment-841758231
  private def unionMerge[A <: js.|[_, _], B](a: A)(implicit ev: js.|.Evidence[A, B]): B =
    a.asInstanceOf[B]

  def fromDynamicType(formatter: DynamicType): Type = {
    val dynamic = unionMerge(formatter).asInstanceOf[js.Dynamic]
    if (dynamic.selectDynamic(dynamicField.name).isInstanceOf[js.Function]) {
      from(dynamic.asInstanceOf[Type])
    } else if (dynamic.isInstanceOf[js.Function]) {
      from(dynamic.asInstanceOf[FunctionType])
    } else {
      throw new IllegalArgumentException(s"$typeName object has no $dynamicField and is not a function: $dynamic")
    }
  }

  protected[this] def fromFunction(fn: Argument => Result): Type

  @inline
  final def from[A](a: A)(implicit p: Provider[A]) = p(a)

  trait Provider[A] {
    def apply(a: A): Type
  }
  object Provider {
    private[this] def fromFunction[A](f: A => Type): Provider[A] =
        new Provider[A] { def apply(a: A) = f(a) }
    implicit val identityProvider: Provider[Type] = fromFunction(identity)
    implicit val functionProvider: Provider[Argument => Result] =
      fromFunction(thisFT.fromFunction)
    implicit val jsFunctionProvider: Provider[FunctionType] =
      fromFunction(thisFT.fromFunction(_))
    implicit val dynamicProvider: Provider[DynamicType] =
      fromFunction(fromDynamicType)
  }
}
