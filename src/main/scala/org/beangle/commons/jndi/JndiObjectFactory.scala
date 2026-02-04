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

package org.beangle.commons.jndi

import org.beangle.commons.bean.Factory
import org.beangle.commons.lang.annotation.description

import java.util as ju
import javax.naming.{InitialContext, NameNotFoundException}
import javax.sql.DataSource

object JndiObjectFactory {
  /** JNDI prefix used in a J2EE container */
  val containerPrefix = "java:comp/env/"
}

/** JNDI Object Factory
 */
class JndiObjectFactory[T](val jndiName: String) extends Factory[T] {

  var resourceRef = true

  var environment: ju.Properties = _

  override def getObject: T = {
    val ctx = new InitialContext
    val located = ctx.lookup(convertJndiName(jndiName))
    if null == located then
      throw new NameNotFoundException(s"JNDI object with [$jndiName] not found: JNDI implementation returned null")
    located.asInstanceOf[T]
  }

  /** Convert the given JNDI name into the actual JNDI name to use.
   * applies the "java:comp/env/" prefix if  resourceRef  is true
   */
  protected def convertJndiName(jndiName: String): String = {
    import JndiObjectFactory.*
    if (resourceRef && !jndiName.startsWith(containerPrefix) && jndiName.indexOf(':') == -1) containerPrefix + jndiName
    else jndiName
  }
}

@description("JNDI提供的数据源工厂")
class JndiDataSourceFactory(jndiName: String) extends JndiObjectFactory[DataSource](jndiName) {

}
