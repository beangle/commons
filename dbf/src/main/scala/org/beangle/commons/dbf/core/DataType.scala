package org.beangle.commons.dbf.core

object DataType extends Enumeration {
  class DataType(val v: Byte) extends Val {
    def this(c: Char) {
      this((c & 0xff).asInstanceOf[Byte])
    }
    override def toString = {
      String.valueOf(v.asInstanceOf[Char])
    }
  }

  val Char = new DataType('C')
  val Date = new DataType('D')
  val Float = new DataType('F')
  val Logical = new DataType('L')
  val Numeric = new DataType('N')

  def valueOf(bv: Byte): DataType = {
    values.find(p => p.asInstanceOf[DataType].v == bv).getOrElse(null).asInstanceOf[DataType]
  }
}
