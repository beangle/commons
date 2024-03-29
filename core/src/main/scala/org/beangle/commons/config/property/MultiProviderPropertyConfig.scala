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

package org.beangle.commons.config.property

import org.beangle.commons.bean.Initializing
import org.beangle.commons.conversion.impl.DefaultConversion
import org.beangle.commons.conversion.string.BooleanConverter
import org.beangle.commons.lang.{Numbers, Strings}

import scala.collection.mutable

/** 系统配置
  *
  * @author chaostone
  */
class MultiProviderPropertyConfig extends PropertyConfig with Initializing {

  private var properties = new mutable.HashMap[String, Any]

  private var listeners = new mutable.ListBuffer[PropertyConfigListener]

  private var providers = new mutable.ListBuffer[PropertyConfig.Provider]

  def init(): Unit =
    reload()

  /** Get value according to name
    */
  def get(name: String): Option[Any] = properties.get(name)

  /** Insert or Update name's value
    */
  def set(name: String, value: Any): Unit =
    properties.put(name, value)

  def get[T](clazz: Class[T], name: String): Option[T] =
    get(name) match {
      case Some(value) =>
        if (null == value) None
        else Some(DefaultConversion.Instance.convert(value, clazz))
      case _ => None
    }

  /** getInt.
    */
  def getInt(name: String): Option[Int] =
    get(name) match {
      case Some(value) => Some(Numbers.toInt(value.asInstanceOf[String]))
      case _ => None
    }

  /** getBoolean.
    */
  def getBoolean(name: String): Option[Boolean] =
    get(name) match {
      case Some(value) => Some(BooleanConverter.apply(value.asInstanceOf[String]))
      case _ => None
    }

  def add(newer: collection.Map[String, Any]): Unit =
    this.properties ++= newer

  def addListener(listener: PropertyConfigListener): Unit =
    listeners += listener

  def removeListener(listener: PropertyConfigListener): Unit =
    listeners -= listener

  /** multicast.
    */
  def multicast(): Unit = {
    val e = new PropertyConfigEvent(this)
    for (listener <- listeners)
      listener.onConfigEvent(e)
  }

  /** toString.
    */
  override def toString: String = {
    val sb = new StringBuilder("DefaultSystemConfig[")
    val props = properties.keySet.toList
    var maxlength = 0
    for (property <- props if property.length > maxlength)
      maxlength = property.length
    for (property <- props) {
      sb.append('\n').append(property)
      sb.append(Strings.repeat(" ", maxlength - property.length))
      sb.append('=').append(properties.get(property))
    }
    sb.append("\n]")
    sb.toString
  }

  /** getNames.
    */
  def names: Set[String] = properties.keySet.toSet

  def addProvider(provider: PropertyConfig.Provider): Unit =
    providers += provider

  /** reload.
    */
  def reload(): Unit =
    synchronized {
      for (provider <- providers) add(provider.getConfig)
      multicast()
    }

  /** Setter for the field <code>providers</code>.
    *
    */
  def setProviders(providers: List[PropertyConfig.Provider]): Unit = {
    this.providers.clear()
    this.providers ++= providers
  }
}
