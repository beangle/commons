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

import java.io.IOException
import java.io.InputStream
import java.util.Enumeration
import java.util.Properties

import org.beangle.commons.lang.Strings
import org.beangle.commons.logging.Logging

import javax.sql.DataSource

class DataSourceUtil

object DataSourceUtil extends Logging {

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
        dialects += dialect
      }
    }
    dialects.toList
  }

}
