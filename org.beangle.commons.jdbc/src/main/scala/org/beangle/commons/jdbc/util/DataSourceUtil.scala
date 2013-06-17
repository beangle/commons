/* Copyright c 2005-2012.
 * Licensed under GNU  LESSER General Public License, Version 3.
 * http://www.gnu.org/licenses
 */
package org.beangle.commons.jdbc.util

import java.io.IOException
import java.io.InputStream
import java.util.Enumeration
import java.util.Properties

import org.beangle.commons.lang.Strings
import org.beangle.commons.logging.Logging

import javax.sql.DataSource

class DataSourceUtil

object DataSourceUtil extends Logging{

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
    val sourceProps = new collection.mutable.HashMap[String, String]
    while (names.hasMoreElements()) {
      val propertyName = names.nextElement()
      if (propertyName.startsWith(datasourceName + ".")) {
        sourceProps.put(Strings.substringAfter(propertyName, datasourceName + "."),
          props.getProperty(propertyName))
      }
    }
    if (sourceProps.isEmpty) null else build(sourceProps)
  }

  private def build(properties: collection.Map[String, String]): DataSource = {
    new PoolingDataSourceFactory(properties("driverClassName"), properties("url"),
      properties("username"), properties("password")).getObject
  }

  def getDataSourceNames(): List[String] = {
    val props: Properties = new Properties()
    val is: InputStream = classOf[DataSourceUtil].getResourceAsStream("/database.properties")
    if (null != is) {
      props.load(is)
    } else {
      throw new RuntimeException("cannot find database.properties")
    }
    val dialects = new collection.mutable.HashSet[String]
    val names: Enumeration[String] = props.propertyNames().asInstanceOf[Enumeration[String]]
    while (names.hasMoreElements()) {
      val propertyName = names.nextElement()
      val dialect = Strings.substringBefore(propertyName, ".")
      if (!dialects.contains(dialect)) {
        dialects+=dialect
      }
    }
    dialects.toList
  }

}
