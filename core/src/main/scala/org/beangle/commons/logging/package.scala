package org.beangle.commons
import org.slf4j.LoggerFactory

package object logging {
  def getLogger(name: String) = new Logger(LoggerFactory.getLogger(name))
  def getLogger(clazz: Class[_]) = new Logger(LoggerFactory.getLogger(clazz))
}