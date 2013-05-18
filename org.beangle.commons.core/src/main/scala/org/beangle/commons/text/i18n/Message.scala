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
package org.beangle.commons.text.i18n

import scala.beans.BeanProperty

/**
 * <p>
 * Message class.
 * </p>
 *
 * @author chaostone
 * @version $Id: $
 */
class Message(@BeanProperty val key: String, @BeanProperty val params: List[Any]) {

  /**
   * <p>
   * Constructor for Message.
   * </p>
   *
   * @param key a {@link java.lang.String} object.
   * @param objs an array of {@link java.lang.Object} objects.
   */
  def this(key: String, objs: Array[Any]) {
    this(key, objs.toList)
  }

  /**
   * <p>
   * Constructor for Message.
   * </p>
   *
   * @param key a {@link java.lang.String} object.
   */
  def this(key: String) {
    this(key, List.empty)
  }
}
