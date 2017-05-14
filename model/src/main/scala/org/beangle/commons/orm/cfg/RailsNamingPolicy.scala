/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2017, Beangle Software.
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
package org.beangle.commons.orm.cfg

import java.io.IOException
import java.net.URL

import org.beangle.commons.config.Resources
import org.beangle.commons.lang.ClassLoaders
import org.beangle.commons.lang.Strings.{ replace, substringBetween, isEmpty, isNotEmpty, rightPad, substringBeforeLast, unCamel }
import org.beangle.commons.logging.Logging
import org.beangle.commons.orm.Name
import org.beangle.commons.orm.NamingPolicy
import org.beangle.commons.text.inflector.Pluralizer
import org.beangle.commons.text.inflector.en.EnNounPluralizer

/**
 * 根据报名动态设置schema,prefix名字
 *
 * @author chaostone
 */
class RailsNamingPolicy(profiles: Profiles) extends NamingPolicy with Logging {

  private var pluralizer: Pluralizer = EnNounPluralizer

  override def classToTableName(clazz: Class[_], entityName: String): Name = {
    val className = if (clazz.getName.endsWith("Bean")) substringBeforeLast(clazz.getName, "Bean") else clazz.getName
    var tableName = addUnderscores(unqualify(className))
    if (null != pluralizer) tableName = pluralizer.pluralize(tableName)
    tableName = profiles.getPrefix(clazz) + tableName
    //      if (tableName.length() > entityTableMaxLength) {
    //        for ((k, v) <- p.abbreviations)
    //          tableName = replace(tableName, k, v)
    //      }
    Name(profiles.getSchema(clazz), tableName)
  }

  override def collectionToTableName(clazz: Class[_], entityName: String, tableName: String, collectionName: String): Name = {
    var collectionTableName = tableName + "_" + addUnderscores(unqualify(collectionName))
    //    getModule(ClassLoaders.load(className)) foreach { p =>
    //      if ((collectionTableName.length() > relationTableMaxLength)) {
    //        for ((k, v) <- p.abbreviations)
    //          collectionTableName = replace(collectionTableName, k, v)
    //      }
    //    }
    Name(profiles.getSchema(clazz), collectionTableName)
  }

  override def propertyToColumnName(clazz: Class[_], property: String): String = {
    addUnderscores(property)
  }

  private def unqualify(qualifiedName: String): String = {
    val loc = qualifiedName.lastIndexOf('.')
    if (loc < 0) qualifiedName else qualifiedName.substring(loc + 1)
  }

  private def addUnderscores(name: String): String = unCamel(name.replace('.', '_'), '_')
}
