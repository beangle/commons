/* Copyright c 2005-2012.
 * Licensed under GNU  LESSER General Public License, Version 3.
 * http://www.gnu.org/licenses
 */
package org.beangle.commons.jdbc.util

import java.io.IOException
import java.io.InputStream
import java.util.Enumeration
import java.util.List
import java.util.Map
import java.util.Properties
import java.util.Set

import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.lang.Strings
import org.slf4j._

import javax.sql.DataSource

class DataSourceUtil

object DataSourceUtil {

  val logger: Logger = LoggerFactory.getLogger(classOf[DataSourceUtil])

  def getDataSource(datasourceName: String): DataSource = {
    val props: Properties = new Properties()
    try {
      val is: InputStream = classOf[DataSourceUtil].getResourceAsStream("/database.properties")
      if (null == is) {
        throw new RuntimeException("cannot find database.properties")
      }
      props.load(is)
    } catch {
      case e: IOException => throw new RuntimeException("cannot find database.properties")
    }
    val names: Enumeration[String] = props.propertyNames().asInstanceOf[Enumeration[String]]
    val sourceProps = CollectUtils.newHashMap[String, String]
    while (names.hasMoreElements()) {
      val propertyName = names.nextElement()
      if (propertyName.startsWith(datasourceName + ".")) {
        sourceProps.put(Strings.substringAfter(propertyName, datasourceName + "."),
          props.getProperty(propertyName))
      }
    }
    if (sourceProps.isEmpty()) {
      return null
    } else {
      return build(sourceProps)
    }
  }

  def build(properties: Map[String, String]): DataSource = {
    new PoolingDataSourceFactory(properties.get("driverClassName"), properties.get("url"),
      properties.get("username"), properties.get("password")).getObject
  }

  def getDataSourceNames(): List[String] = {
    val props: Properties = new Properties()
    val is: InputStream = classOf[DataSourceUtil].getResourceAsStream("/database.properties")
    if (null != is) {
      props.load(is)
    } else {
      throw new RuntimeException("cannot find database.properties")
    }
    val dialects = CollectUtils.newHashSet[String]
    val names: Enumeration[String] = props.propertyNames().asInstanceOf[Enumeration[String]]
    while (names.hasMoreElements()) {
      val propertyName = names.nextElement()
      val dialect = Strings.substringBefore(propertyName, ".")
      if (!dialects.contains(dialect)) {
        dialects.add(dialect)
      }
    }
    CollectUtils.newArrayList(dialects)
  }

}
