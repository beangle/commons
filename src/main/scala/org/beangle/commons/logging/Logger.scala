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

/** SLF4J Logger delegate. */
object Logger {
  /** Creates a Logger for the given class. */
  def apply(clazz: Class[_]): Logger = {
    new Logger(LoggerFactory getLogger clazz)
  }
}

/** SLF4J Logger wrapper with lazy-evaluated messages. */
class Logger(private val logger: JLogger) {

  /** Returns true if debug level is enabled. */
  @inline def isDebugEnabled: Boolean = logger.isDebugEnabled

  /** Returns true if error level is enabled. */
  @inline def isErrorEnabled: Boolean = logger.isErrorEnabled

  /** Returns true if warn level is enabled. */
  @inline def isWarnEnabled: Boolean = logger.isWarnEnabled

  /** Logs trace message (lazy). */
  @elidable(FINEST)
  def trace(msg: => String): Unit = {
    if (logger.isTraceEnabled) logger.trace(msg)
  }

  /** Logs trace message with throwable (lazy). */
  @elidable(FINEST)
  def trace(msg: => String, t: => Throwable): Unit = {
    if (logger.isTraceEnabled) logger.trace(msg, t)
  }

  /** Logs debug message (lazy). */
  @elidable(FINE)
  def debug(msg: => String): Unit = {
    if (logger.isDebugEnabled) logger.debug(msg)
  }

  /** Logs debug message with throwable (lazy). */
  @elidable(FINE)
  def debug(msg: => String, t: => Throwable): Unit = {
    if (logger.isDebugEnabled) logger.debug(msg, t)
  }

  /** Logs info message. */
  def info(msg: String): Unit = {
    logger.info(msg)
  }

  /** Logs warn message. */
  def warn(msg: String): Unit = {
    logger.warn(msg)
  }

  /** Logs warn message with throwable. */
  def warn(msg: String, t: Throwable): Unit = {
    logger.warn(msg, t)
  }

  /** Logs error message. */
  def error(msg: String): Unit = {
    logger.error(msg)
  }

  /** Logs error message with throwable. */
  def error(msg: String, t: Throwable): Unit = {
    logger.error(msg, t)
  }

}
