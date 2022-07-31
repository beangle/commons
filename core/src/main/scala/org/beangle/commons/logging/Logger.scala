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

import org.slf4j.{ LoggerFactory, Logger => JLogger }

import scala.annotation.elidable
import scala.annotation.elidable._

/**
 * Slf4j Logger delegate.
 */
object Logger {
  def apply(clazz: Class[_]): Logger =
    new Logger(LoggerFactory getLogger clazz)

  sealed trait LevelLogger extends Any {
    def apply(msg: => String): Unit

    def apply(msg: => String, t: Throwable): Unit
  }

  private[logging] object ZeroLogger extends LevelLogger {
    @inline def apply(msg: => String): Unit = {}

    @inline def apply(msg: => String, t: Throwable): Unit = {}
  }

  final class TraceLogger private[logging] (val logger: JLogger) extends AnyVal with LevelLogger {
    @inline def apply(msg: => String): Unit = logger.trace(msg)

    @inline def apply(msg: => String, t: Throwable): Unit = logger.trace(msg, t)
  }

  final class DebugLogger private[logging] (val logger: JLogger) extends AnyVal with LevelLogger {
    @inline def apply(msg: => String): Unit = logger.debug(msg)

    @inline def apply(msg: => String, t: Throwable): Unit = logger.debug(msg, t)
  }

  final class InfoLogger private[logging] (val logger: JLogger) extends AnyVal with LevelLogger {
    @inline def apply(msg: => String): Unit = logger.info(msg)

    @inline def apply(msg: => String, t: Throwable): Unit = logger.info(msg, t)
  }

  final class WarnLogger private[logging] (val logger: JLogger) extends AnyVal with LevelLogger {
    @inline def apply(msg: => String): Unit = logger.warn(msg)

    @inline def apply(msg: => String, t: Throwable): Unit = logger.warn(msg, t)
  }

  final class ErrorLogger private[logging] (val logger: JLogger) extends AnyVal with LevelLogger {
    @inline def apply(msg: => String): Unit = logger.error(msg)

    @inline def apply(msg: => String, t: Throwable): Unit = logger.error(msg, t)
  }
}

final class Logger(val logger: JLogger) extends AnyVal {

  @inline final def isDebugEnabled: Boolean = logger.isDebugEnabled

  @inline final def isErrorEnabled: Boolean = logger.isErrorEnabled

  @inline final def isWarnEnabled: Boolean = logger.isWarnEnabled

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

  import Logger._

  def apply(level: LogLevel): LevelLogger = level match {
    case Trace => if (logger.isTraceEnabled) new TraceLogger(logger) else ZeroLogger
    case Debug => if (logger.isDebugEnabled) new DebugLogger(logger) else ZeroLogger
    case Info => if (logger.isInfoEnabled) new InfoLogger(logger) else ZeroLogger
    case Warn => if (logger.isWarnEnabled) new WarnLogger(logger) else ZeroLogger
    case Error => if (logger.isErrorEnabled) new ErrorLogger(logger) else ZeroLogger
  }

  @inline def apply(lvl: Trace.type): LevelLogger = if (logger.isTraceEnabled) new TraceLogger(logger) else ZeroLogger

  @inline def apply(lvl: Debug.type): LevelLogger = if (logger.isDebugEnabled) new DebugLogger(logger) else ZeroLogger

  @inline def apply(lvl: Info.type): LevelLogger = if (logger.isInfoEnabled) new InfoLogger(logger) else ZeroLogger

  @inline def apply(lvl: Warn.type): LevelLogger = if (logger.isWarnEnabled) new WarnLogger(logger) else ZeroLogger

  @inline def apply(lvl: Error.type): LevelLogger = if (logger.isErrorEnabled) new ErrorLogger(logger) else ZeroLogger
}
