package org.beangle.commons.jdbc

import java.sql.Types
import org.beangle.commons.lang.Strings

trait Engine {

  def storeCase: StoreCase.Value

  def keywords: Set[String]

  def typeNames: TypeNames

  def quoteChars: Tuple2[Char, Char]

  def sqlType(sqlCode: Int): SqlType

  def sqlType(sqlCode: Int, length: Int): SqlType

  def sqlType(sqlCode: Int, precision: Int, scale: Int): SqlType

  def needQuote(name: String): Boolean = {
    val rs = (name.indexOf(' ') > -1) || keywords.contains(name.toLowerCase)
    if (rs) return true
    storeCase match {
      case StoreCase.Lower => name.exists { c => Character.isUpperCase(c) }
      case StoreCase.Upper => name.exists { c => Character.isLowerCase(c) }
      case StoreCase.Mixed => false
    }
  }

  def quote(name: String): String = {
    if (needQuote(name)) {
      val qc = quoteChars
      qc._1 + name + qc._2
    } else {
      name
    }
  }

  def toIdentifier(literal: String): Identifier = {
    if (Strings.isEmpty(literal)) return null
    if (literal.charAt(0) == quoteChars._1) Identifier(literal.substring(1, literal.length - 1), true)
    else {
      storeCase match {
        case StoreCase.Lower => Identifier(literal.toLowerCase(), false)
        case StoreCase.Upper => Identifier(literal.toUpperCase(), false)
        case StoreCase.Mixed => Identifier(literal, false)
      }
    }
  }

}

abstract class AbstractEngine extends Engine {
  val typeNames = new TypeNames()

  var keywords: Set[String] = Set.empty[String]

  def registerKeywords(words: String*): Unit = {
    keywords ++= words.toList
  }

  override def quoteChars: Tuple2[Char, Char] = {
    ('\"', '\"')
  }

  protected def registerTypes(tuples: Tuple2[Int, String]*): Unit = {
    tuples foreach { tuple =>
      typeNames.put(tuple._1, tuple._2)
    }
  }

  protected def registerTypes2(tuples: Tuple3[Int, Int, String]*): Unit = {
    tuples foreach { tuple =>
      typeNames.put(tuple._1, tuple._2, tuple._3)
    }
  }

  override def sqlType(sqlCode: Int): SqlType = {
    sqlType(sqlCode, 0, 0)
  }

  override def sqlType(sqlCode: Int, length: Int): SqlType = {
    if (SqlType.isNumberType(sqlCode)) {
      sqlType(sqlCode, 0, length, 0)
    } else {
      sqlType(sqlCode, length, 0, 0)
    }
  }

  override def sqlType(sqlCode: Int, precision: Int, scale: Int): SqlType = {
    sqlType(sqlCode, 0, precision, scale)
  }

  private def sqlType(sqlCode: Int, length: Int, precision: Int, scale: Int): SqlType = {
    if (sqlCode == Types.OTHER) new SqlType(sqlCode, "other") else
      try {
        val result = new SqlType(sqlCode, typeNames.get(sqlCode, precision, precision, scale))
        if (precision > 0) {
          result.precision = Some(precision)
          result.scale = Some(scale)
        } else {
          result.length = Some(length)
        }
        result
      } catch {
        case e: Exception =>
          println("Cannot find type code" + sqlCode)
          new SqlType(sqlCode, "unkown")
      }
  }
  def storeCase: StoreCase.Value = {
    StoreCase.Mixed
  }
}
