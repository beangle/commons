/* Copyright c 2005-2012.
 * Licensed under GNU  LESSER General Public License, Version 3.
 * http://www.gnu.org/licenses
 */
package org.beangle.commons.jdbc.util

import javax.sql.DataSource

import org.apache.commons.dbcp._
import org.apache.commons.pool.impl.GenericObjectPool
import org.slf4j._

class PoolingDataSourceFactory(url: String, val username: String, password: String) {

  private val logger: Logger = LoggerFactory.getLogger(classOf[PoolingDataSourceFactory])

  def this(newDriverClassName: String, url: String, username: String, password: String) = {
    this(url, username, password)
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
    val connectionFactory: ConnectionFactory = new DriverManagerConnectionFactory(url, username, password)
    new PoolableConnectionFactory(connectionFactory, connectionPool, null, null, false, true)
    new PoolingDataSource(connectionPool)
  }

  def singleton = true

}
