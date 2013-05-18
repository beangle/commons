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

  protected lazy val logger: Logger =  LoggerFactory getLogger getClass
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
