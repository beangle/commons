/*
 * Copyright (C) 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.beangle.commons.logging

import org.slf4j.{LoggerFactory, Logger as JLogger}

import scala.annotation.elidable
import scala.annotation.elidable.*

/** Slf4j Logger delegate.
 */
object Logger {
  def apply(clazz: Class[_]): Logger = {
    new Logger(LoggerFactory getLogger clazz)
  }
}

final class Logger(val logger: JLogger) extends AnyVal {

  @inline def isDebugEnabled: Boolean = logger.isDebugEnabled

  @inline def isErrorEnabled: Boolean = logger.isErrorEnabled

  @inline def isWarnEnabled: Boolean = logger.isWarnEnabled

  @elidable(FINEST)
  def trace(msg: => String): Unit = {
    if (logger.isTraceEnabled) logger.trace(msg)
  }

  @elidable(FINEST)
  def trace(msg: => String, t: => Throwable): Unit = {
    if (logger.isTraceEnabled) logger.trace(msg, t)
  }

  @elidable(FINE)
  def debug(msg: => String): Unit = {
    if (logger.isDebugEnabled) logger.debug(msg)
  }

  @elidable(FINE)
  def debug(msg: => String, t: => Throwable): Unit = {
    if (logger.isDebugEnabled) logger.debug(msg, t)
  }

  def info(msg: => String): Unit = {
    logger.info(msg)
  }

  def warn(msg: => String): Unit = {
    logger.warn(msg)
  }

  def warn(msg: => String, t: => Throwable): Unit = {
    logger.warn(msg, t)
  }

  def error(msg: => String): Unit = {
    logger.error(msg)
  }

  def error(msg: => String, t: => Throwable): Unit = {
    logger.error(msg, t)
  }

}
