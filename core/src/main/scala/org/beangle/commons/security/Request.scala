package org.beangle.commons.security

trait Request {

  def resource: Any

  def operation: Any

}

class DefaultRequest(val resource: Any, val operation: Any) extends Request {

}