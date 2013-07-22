/*
 * Beangle, Agile Java/Scala Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2013, Beangle Software.
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
import org.slf4j.Logger

/**
 * Adds the lazy val logger of type [[$Logger]] to the class into which this trait is mixed.
 *
 * If you need a not-lazy [[$Logger]], which would probably be a special case,
 * use [[org.beangle.commons.logging.StrictLogging]].
 *
 * @define Logger org.beangle.commons.logging.Logger
 */
trait Logging {

  protected lazy val logger: Logger = LoggerFactory getLogger getClass
}

/**
 * Adds the not-lazy val logger of type [[$Logger]] to the class into which this trait is mixed.
 *
 * If you need a lazy [[$Logger]], which would probably be preferrable,
 * use [[org.beangle.commons.logging.Logging]].
 *
 * @define Logger org.beangle.commons.logging.Logger
 */
trait StrictLogging {

  protected val logger: Logger = LoggerFactory getLogger getClass
}
