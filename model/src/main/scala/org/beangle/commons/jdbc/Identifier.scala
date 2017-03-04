package org.beangle.commons.jdbc

case class Identifier(value: String, quoted: Boolean = false) extends Ordered[Identifier] {

  def toCase(lower: Boolean): Identifier = {
    Identifier(if (lower) value.toLowerCase() else value.toUpperCase(), quoted)
  }

  override def toString: String = {
    if (quoted) "`" + value + "`"
    else value
  }

  override def compare(other: Identifier): Int = {
    value.compareTo(other.value)
  }

  override def equals(other: Any): Boolean = {
    other match {
      case n: Identifier => n.value == this.value
      case _             => false
    }
  }
  override def hashCode: Int = {
    value.hashCode()
  }

  def attach(engine: Engine): Identifier = {
    val needQuote = engine.needQuote(value)
    if (needQuote != quoted) Identifier(value, needQuote)
    else this
  }

  def toLiteral(engine: Engine): String = {
    if (quoted) {
      val qc = engine.quoteChars
      qc._1 + value + qc._2
    } else {
      value
    }
  }
}
