/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2016, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.commons.logging

import org.slf4j.LoggerFactory
import scala.annotation.elidable
import scala.annotation.elidable._

/**
 * Slf4j Logger delegate.
 */
class Logger(clazz: Class[_]) {

  val logger = LoggerFactory getLogger clazz

  final def isDebugEnabled: Boolean = logger.isDebugEnabled

  final def isErrorEnabled: Boolean = logger.isErrorEnabled

  final def isWarnEnabled: Boolean = logger.isWarnEnabled

  @elidable(FINEST)
  final def trace(msg: => String): Unit = if (logger.isTraceEnabled) logger.trace(msg)

  @elidable(FINEST)
  final def trace(msg: => String, t: => Throwable): Unit = if (logger.isTraceEnabled) logger.trace(msg, t)

  @elidable(FINE)
  final def debug(msg: => String): Unit = if (logger.isDebugEnabled) logger.debug(msg)

  @elidable(FINE)
  final def debug(msg: => String, t: => Throwable): Unit = if (logger.isDebugEnabled) logger.debug(msg, t)

  final def info(msg: => String): Unit = if (logger.isInfoEnabled) logger.info(msg)

  final def info(msg: => String, t: => Throwable): Unit = if (logger.isInfoEnabled) logger.info(msg, t)

  final def warn(msg: => String): Unit = if (logger.isWarnEnabled) logger.warn(msg)

  final def warn(msg: => String, t: => Throwable): Unit = if (logger.isWarnEnabled) logger.warn(msg, t)

  final def error(msg: => String): Unit = if (logger.isErrorEnabled) logger.error(msg)

  final def error(msg: => String, t: => Throwable): Unit = if (logger.isErrorEnabled) logger.error(msg, t)
}