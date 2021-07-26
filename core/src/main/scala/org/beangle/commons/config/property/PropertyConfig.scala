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

import org.beangle.commons.config.property.PropertyConfig._

object PropertyConfig {

  trait Provider {

    def getConfig: collection.Map[String, Any]
  }
}

/**
 * 系统属性
 *
 * @author chaostone
 */
trait PropertyConfig {

  /**
   * get.
   */
  def get(name: String): Option[Any]

  /**
   * set.
   *
   */
  def set(name: String, value: Any): Unit

  /**
   * get.
   */
  def get[T](clazz: Class[T], name: String): Option[T]

  /**
   * <p>
   * add.
   * </p>
   *
   */
  def add(properties: collection.Map[String, Any]): Unit

  /**
   * names.
   */
  def names: Set[String]

  /**
   * <p>
   * addConfigListener.
   * </p>
   *
   * @param listener a {@link org.beangle.commons.config.property.PropertyConfigListener} object.
   */
  def addListener(listener: PropertyConfigListener): Unit

  /**
   * <p>
   * removeConfigListener.
   * </p>
   *
   * @param listener a {@link org.beangle.commons.config.property.PropertyConfigListener} object.
   */
  def removeListener(listener: PropertyConfigListener): Unit

  /**
   * <p>
   * multicast.
   * </p>
   */
  def multicast(): Unit

  /**
   * <p>
   * reload.
   * </p>
   */
  def reload(): Unit

  /**
   * <p>
   * addConfigProvider.
   * </p>
   *
   * @param provider a {@link org.beangle.commons.config.property.PropertyConfig.Provider} object.
   */
  def addProvider(provider: Provider): Unit
}
