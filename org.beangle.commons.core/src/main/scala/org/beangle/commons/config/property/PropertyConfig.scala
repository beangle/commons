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

import java.util.Properties
import java.util.Set
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
 * @version $Id: $
 */
trait PropertyConfig {

  /**
   * <p>
   * get.
   * </p>
   *
   * @param name a {@link java.lang.String} object.
   * @return a {@link java.lang.Object} object.
   */
  def get(name: String): Any

  /**
   * <p>
   * set.
   * </p>
   *
   * @param name a {@link java.lang.String} object.
   * @param value a {@link java.lang.Object} object.
   */
  def set(name: String, value: Any): Unit

  /**
   * <p>
   * get.
   * </p>
   *
   * @param clazz a {@link java.lang.Class} object.
   * @param name a {@link java.lang.String} object.
   * @param <T> a T object.
   * @return a T object.
   */
  def get[T](clazz: Class[T], name: String): T

  /**
   * <p>
   * add.
   * </p>
   *
   * @param properties a {@link java.util.Properties} object.
   */
  def add(properties: Properties): Unit

  /**
   * <p>
   * getNames.
   * </p>
   *
   * @return a {@link java.util.Set} object.
   */
  def getNames(): Set[String]

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
