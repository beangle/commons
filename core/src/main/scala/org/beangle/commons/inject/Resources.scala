/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2015, Beangle Software.
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
package org.beangle.commons.inject

import java.net.URL

/**
 * ConfigResource class.
 *
 * @author chaostone
 */
class Resources(val global: URL, val locals: List[URL], val user: URL) {

  def paths: List[URL] = {
    val all = new collection.mutable.ListBuffer[URL]
    if (null != global) all += global
    if (null != locals) all ++= locals
    if (null != user) all += user
    all.toList
  }

  def isEmpty: Boolean = {
    null == global && null == user && (null == locals || locals.isEmpty)
  }

  override def toString(): String = "{global:" + global + "  locals:" + locals + "  user:" + user + "}"
}
