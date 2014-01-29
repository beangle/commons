package org.beangle.commons.lang

final object Range {
  def range(start: Int, end: Int): IntRange = new IntRange(start, end)
  def range(start: Int, end: Int, step: Int): IntStepRange = new IntStepRange(start, end, step)
}

final class IntStepRange(final val start: Int, val end: Int, val step: Int) {
  @inline def foreach[@specialized(Unit) U](f: Int => U) {
    var i = start
    val term = end
    val s = step
    while (i < term) {
      f(i)
      i += s
    }
  }
}

final class IntRange(val start: Int, val end: Int) {
  @inline def foreach[@specialized(Unit) U](f: Int => U) {
    var s = start
    val e = end
    while (s < e) {
      f(s)
      s += 1
    }
  }
}
