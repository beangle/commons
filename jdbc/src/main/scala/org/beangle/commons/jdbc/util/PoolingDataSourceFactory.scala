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
package org.beangle.commons.jdbc.util

import javax.sql.DataSource

import org.apache.commons.dbcp._
import org.apache.commons.pool.impl.GenericObjectPool
import org.slf4j._
import java.util.Properties

class PoolingDataSourceFactory(url: String, username: String, password: String, props: Properties) {

  private val logger: Logger = LoggerFactory.getLogger(classOf[PoolingDataSourceFactory])

  val properties = if (null == props) new Properties() else new Properties(props)

  if (null != username) properties.put("user", username)
  if (null != password) properties.put("password", password)

  def this(newDriverClassName: String, url: String, username: String, password: String, props: Properties) = {
    this(url, username, password, props)
    registeDriver(newDriverClassName)
  }

  def registeDriver(newDriverClassName: String) = {
    //Validate.notEmpty(newDriverClassName, "Property 'driverClassName' must not be empty")
    val driverClassNameToUse: String = newDriverClassName.trim()
    try {
      Class.forName(driverClassNameToUse)
    } catch {
      case ex: ClassNotFoundException =>
        throw new IllegalStateException(
          "Could not load JDBC driver class [" + driverClassNameToUse + "]", ex)
    }
    logger.debug("Loaded JDBC driver: {}", driverClassNameToUse)
  }

  def getObject: DataSource = {
    val config = new GenericObjectPool.Config()
    config.maxActive = 16
    val connectionPool = new GenericObjectPool(null, config)
    val connectionFactory: ConnectionFactory = new DriverManagerConnectionFactory(url, properties)
    new PoolableConnectionFactory(connectionFactory, connectionPool, null, null, false, true)
    new PoolingDataSource(connectionPool)
  }

  def singleton = true

}
