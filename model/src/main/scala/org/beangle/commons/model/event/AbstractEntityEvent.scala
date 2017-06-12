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
package org.beangle.commons.model.event

import org.beangle.commons.event.Event
import org.beangle.commons.model.Entity
import org.beangle.commons.model.annotation.config

/**
 * 实体操作相关事件
 *
 * @author chaostone
 */
abstract class AbstractEntityEvent[T <: Entity[_]](clazz: Class[T], source: Seq[T]) extends Event(source) {

  def entities: Seq[T] = source
}
