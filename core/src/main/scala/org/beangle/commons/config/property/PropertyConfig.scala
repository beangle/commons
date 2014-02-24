/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2014, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.commons.config.property

import java.util.Properties
import PropertyConfig._

object PropertyConfig {

  trait Provider {

    def getConfig(): Properties
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
   * @param properties a {@link java.util.Properties} object.
   */
  def add(properties: Properties): Unit

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