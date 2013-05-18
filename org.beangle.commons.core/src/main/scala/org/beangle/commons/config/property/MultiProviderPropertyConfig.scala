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
package org.beangle.commons.config.property

import java.util._
import org.beangle.commons.bean.Initializing
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.lang.Numbers
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.conversion.impl.DefaultConversion
import scala.collection.JavaConversions._
import org.beangle.commons.lang.conversion.converter.String2BooleanConverter

/**
 * 系统配置
 *
 * @author chaostone
 * @version $Id: $
 */
class MultiProviderPropertyConfig extends PropertyConfig with Initializing {

  private var properties: Map[String, Any] = new HashMap[String, Any]()

  private var listeners: List[PropertyConfigListener] = CollectUtils.newArrayList()

  private var providers: List[PropertyConfig.Provider] = new ArrayList[PropertyConfig.Provider]()

  def init() {
    reload()
  }

  /**
   * Get value according to name
   */
  def get(name: String): Any = properties.get(name)

  /**
   * Insert or Update name's value
   */
  def set(name: String, value: Any) {
    properties.put(name, value)
  }

  def get[T](clazz: Class[T], name: String): T = {
    val value = get(name)
    if (null == value) null.asInstanceOf[T]
    else DefaultConversion.Instance.convert(value, clazz)
  }

  /**
   * <p>
   * getInt.
   * </p>
   *
   * @param name a {@link java.lang.String} object.
   * @return a int.
   */
  def getInt(name: String): Int = {
    Numbers.toInt(get(name).asInstanceOf[String])
  }

  /**
   * <p>
   * getBool.
   * </p>
   *
   * @param name a {@link java.lang.String} object.
   * @return a boolean.
   */
  def getBool(name: String): Boolean = {
    String2BooleanConverter.apply(get(name).asInstanceOf[String]).booleanValue()
  }

  def add(newer: Properties) {
    for (key <- newer.keySet) {
      this.properties.put(key.toString, newer.get(key))
    }
  }

  def addListener(listener: PropertyConfigListener) {
    listeners.add(listener)
  }

  def removeListener(listener: PropertyConfigListener) {
    listeners.remove(listener)
  }

  /**
   * <p>
   * multicast.
   * </p>
   */
  def multicast() {
    val e = new PropertyConfigEvent(this)
    for (listener <- listeners) {
      listener.onConfigEvent(e)
    }
  }

  /**
   * <p>
   * toString.
   * </p>
   *
   * @return a {@link java.lang.String} object.
   */
  override def toString(): String = {
    val sb = new StringBuilder("DefaultSystemConfig[")
    val props = new ArrayList[String](properties.keySet)
    Collections.sort(props)
    var maxlength = 0
    for (property <- props if property.length > maxlength) {
      maxlength = property.length
    }
    for (property <- props) {
      sb.append('\n').append(property)
      sb.append(Strings.repeat(" ", maxlength - property.length))
      sb.append('=').append(properties.get(property))
    }
    sb.append("\n]")
    sb.toString
  }

  /**
   * <p>
   * getNames.
   * </p>
   *
   * @return a {@link java.util.Set} object.
   */
  def getNames(): Set[String] = {
    CollectUtils.newHashSet(properties.keySet)
  }

  def addProvider(provider: PropertyConfig.Provider) {
    providers.add(provider)
  }

  /**
   * <p>
   * reload.
   * </p>
   */
  def reload() {
    synchronized {
      for (provider <- providers) add(provider.getConfig)
      multicast()
    }
  }

  /**
   * <p>
   * Setter for the field <code>providers</code>.
   * </p>
   *
   * @param providers a {@link java.util.List} object.
   */
  def setProviders(providers: List[PropertyConfig.Provider]) {
    this.providers = providers
  }
}
