package org.beangle.commons.logging

object LoggerTest {
  def main(args: Array[String]) {
    val logger = getLogger(getClass)
    val info = logger(Info)
    info("ddd")

    val debug = logger(Debug)
    debug("debug")

    println(debug.getClass())
  }
}