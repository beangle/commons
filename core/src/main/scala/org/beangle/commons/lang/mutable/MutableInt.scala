package org.beangle.commons.lang.mutable

class MutableInt(var value: Int=0) {

  @inline
  def increment(): Int = {
    value += 1
    value
  }

  @inline
  def decrement(): Int = {
    value -= 1
    value
  }
}