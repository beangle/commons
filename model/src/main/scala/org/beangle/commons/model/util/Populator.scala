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
package org.beangle.commons.model.util

import org.beangle.commons.model.meta._
import org.beangle.commons.model._
/**
 * Populator interface.
 *
 * @author chaostone
 */
trait Populator {
  /**
   * populate.
   */
  def populate(target: Entity[_], EntityType: EntityType, params: collection.Map[String, Any]): Int

  /**
   *
   */
  def populate(target: Entity[_], EntityType: EntityType, attr: String, value: Any): Boolean

  /**
   * initProperty.
   */
  def init(target: Entity[_], t: EntityType, attr: String): (Any, Property)
}
