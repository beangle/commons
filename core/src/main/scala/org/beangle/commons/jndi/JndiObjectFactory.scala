/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2014, Beangle Software.
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
package org.beangle.commons.jndi

import java.{ util => ju }

import org.beangle.commons.bean.Factory

import javax.naming.{ InitialContext, NameNotFoundException }

object JndiObjectFactory {
  /** JNDI prefix used in a J2EE container */
  val containerPrefix = "java:comp/env/"
}
/**
 * JNDI Object Factory
 */
class JndiObjectFactory extends Factory[AnyRef] {

  var jndiName: String = _

  var resourceRef = false

  var environment: ju.Properties = _

  var expectedType: Class[AnyRef] = classOf[AnyRef]

  def getObject: AnyRef = {
    val ctx = new InitialContext
    val located = ctx.lookup(convertJndiName(jndiName))
    if (null == located)
      throw new NameNotFoundException(
        "JNDI object with [" + jndiName + "] not found: JNDI implementation returned null");
    located
  }

  def singleton = true

  def getObjectType = expectedType

  /**
   * Convert the given JNDI name into the actual JNDI name to use.
   * applies the "java:comp/env/" prefix if  resourceRef  is true
   */
  protected def convertJndiName(jndiName: String): String = {
    import JndiObjectFactory._
    if (resourceRef && !jndiName.startsWith(containerPrefix) && jndiName.indexOf(':') == -1) containerPrefix + jndiName
    else jndiName
  }

}