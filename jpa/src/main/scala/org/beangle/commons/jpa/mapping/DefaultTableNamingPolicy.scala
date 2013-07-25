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
package org.beangle.commons.jpa.mapping

import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.util.Properties

import org.beangle.commons.inject.Resources
import org.beangle.commons.lang.Strings
import org.beangle.commons.logging.Logging
import org.beangle.commons.text.inflector.Pluralizer
import org.beangle.commons.text.inflector.en.EnNounPluralizer

/**
 * 根据报名动态设置schema,prefix名字
 * 
 * @author chaostone
 */
class RailsNamingPolicy extends TableNamingPolicy with Logging{

  /** 实体表表名长度限制 */
  var entityTableMaxLength = 25

  /** 关联表表名长度限制 */
  var relationTableMaxLength = 30

  private var pluralizer:Pluralizer = new EnNounPluralizer()

  private var patterns : List[TableNamePattern]=_

  private val  packagePatterns = new collection.mutable.HashMap[String, TableNamePattern]

  def addConfig( url:URL) {
    loadProperties(url)
    patterns = patterns.sorted
  }

  private def loadProperties(url:URL) {
    try {
      logger.debug("loading {}", url)
      val is = url.openStream()
      val props = new Properties()
      if (null != is) {
        props.load(is)
      }
      val iter = props.keySet().iterator
      while(iter.hasNext()){
        val packageName =  iter.next().asInstanceOf[String]
        val schemaPrefix = props.getProperty(packageName).trim()

        var schema:String  = null
        var prefix:String = null
        var abbreviationStr:String = null
        val commaIndex = schemaPrefix.indexOf(',')
        if (commaIndex < 0 || (commaIndex + 1 == schemaPrefix.length())) {
          schema = schemaPrefix
        } else if (commaIndex == 0) {
          prefix = schemaPrefix.substring(1)
        } else {
          schema = Strings.substringBefore(schemaPrefix, ",")
          prefix = Strings.substringAfter(schemaPrefix, ",")
        }
        if (Strings.contains(prefix, ",")) {
          abbreviationStr = Strings.substringAfter(prefix, ",").toLowerCase()
          prefix = Strings.substringBefore(prefix, ",")
        }

        if(null==schema) schema=""
        if(null==prefix) prefix=""

        var  pattern = packagePatterns.get(packageName).orNull
        if (null == pattern) {
          pattern = new TableNamePattern(packageName, schema, prefix)
          packagePatterns.put(packageName, pattern)
          patterns ::= pattern
        } else {
          pattern.schema = schema
          pattern.prefix = prefix
        }
        if (null != abbreviationStr) {
          val pairs = Strings.split(abbreviationStr, "")
          for (pair <- pairs) {
            val longName = Strings.substringBefore(pair, "=")
            val shortName = Strings.substringAfter(pair, "=")
            pattern.abbreviations += (longName -> shortName)
          }
        }
      }
      is.close()
    } catch  {
      case e:IOException =>    logger.error("property load error", e)
    }
  }

  def getSchema(packageName:String ):String= {
    var schemaName:String  = null
    for ( pattern <- patterns) {
      if (packageName.indexOf(pattern.packageName) == 0) schemaName = pattern.schema
    }
    schemaName
  }

  def getPattern(packageName:String ):TableNamePattern= {
    var last:TableNamePattern = null
    for (pattern <- patterns) {
      if (packageName.indexOf(pattern.packageName) == 0) last = pattern
    }
    last
  }

  def getPrefix(packageName:String ):String= {
    var prefix:String = null
    for (pattern <- patterns) {
      if (packageName.indexOf(pattern.packageName) == 0) prefix = pattern.prefix
    }
    return prefix
  }

  /**
   * is Multiple schema for entity
   */
  def isMultiSchema:Boolean= {
    val schemas = new collection.mutable.HashSet[String]
    for (pattern <- patterns) {
      schemas += (if (null == pattern.schema)  "" else  pattern.schema)
    }
    schemas.size > 1
  }

  def setResources(resources:Resources) {
    if (null != resources) {
      for (url <- resources.paths)
        addConfig(url)
      logger.info("Table name pattern: -> \n{}", this)
    }
  }

  override def toString():String= {
    var  maxlength = 0
    for ( pattern <- patterns) {
      if (pattern.packageName.length() > maxlength) {
        maxlength = pattern.packageName.length()
      }
    }
    val sb = new StringBuilder()
    for (i <-  0 until patterns.size) {
      val pattern = patterns(i)
      sb.append(Strings.rightPad(pattern.packageName, maxlength, ' ')).append(" : [")
          .append(pattern.schema)
      sb.append(" , ").append(pattern.prefix)
      if (!pattern.abbreviations.isEmpty) {
        sb.append(" , ").append(pattern.abbreviations)
      }
      sb.append(']')
      if (i < patterns.size - 1) sb.append('\n')
    }
    sb.toString()
  }

  def classToTableName(clazzName:String ) :String ={
    val className= if (clazzName.endsWith("Bean")) Strings.substringBeforeLast(clazzName, "Bean") else clazzName

    var tableName = addUnderscores(unqualify(className))
    if (null != pluralizer) tableName = pluralizer.pluralize(tableName)

    val pattern = getPattern(className)
    if (null != pattern) tableName = pattern.prefix + tableName

    if (tableName.length() > entityTableMaxLength && null != pattern) {
      for ((k,v) <- pattern.abbreviations) {
        tableName = Strings.replace(tableName, k,v)
      }
    }
    return tableName
  }

 def  collectionToTableName(className:String , tableName:String , collectionName:String ):String= {
    val pattern = getPattern(className)
    var collectionTableName = tableName + "_" + addUnderscores(unqualify(collectionName))
    if ((collectionTableName.length() > relationTableMaxLength) && null != pattern) {
      for ((k,v) <- pattern.abbreviations) {
        collectionTableName = Strings.replace(collectionTableName, k,v)
      }
    }
    collectionTableName
  }

  protected def unqualify(qualifiedName:String ):String = {
    val loc = qualifiedName.lastIndexOf('.')
    if (loc < 0)  qualifiedName else qualifiedName.substring(loc + 1)
  }

  protected def  addUnderscores(name:String ):String= Strings.unCamel(name.replace('.', '_'), '_')

}

/**
 * 表命名模式
 * 
 * @author chaostone
 */
class TableNamePattern(var packageName:String ,var  schema: String,var prefix: String) extends Ordered[TableNamePattern] {

  var abbreviations: Map[String, String] = _

  override def compare(other:TableNamePattern):Int= this.packageName.compareTo(other.packageName)

  override  def  toString():String = {
    val sb = new StringBuilder()
    sb.append("[package:").append(packageName).append(", schema:").append(schema)
    sb.append(", prefix:").append(prefix).append(']')
    sb.append(", abbreviations:").append(abbreviations).append(']')
    sb.toString()
  }
}
