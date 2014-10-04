package org.beangle.commons.lang.primitive

class MutableInt(var value: Int=0) {

  def increment(): Int = {
    value += 1
    value
  }

  def decrement(): Int = {
    value -= 1
    value
  }
}