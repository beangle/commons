package org.beangle.commons.logging

sealed trait LogLevel {
  def name: String = this.toString
}
case object Error extends LogLevel
case object Warn extends LogLevel
case object Info extends LogLevel
case object Debug extends LogLevel
case object Trace extends LogLevel