package org.log4s.log4sjs

private[log4sjs] object CategoryParser {
  /* TODO: Handle category escaping? Don't parse the full string if not needed? */
  def apply(category: String): Seq[String] = {
    category
      .foldLeft(Nil: List[String]) { (l, c) =>
        c match {
          case '.' =>
            "" :: l
          case oc  =>
            l match {
              case h :: t => h + oc :: t
              case Nil    => oc.toString :: Nil
            }
        }
      }
      .reverse
  }
}
