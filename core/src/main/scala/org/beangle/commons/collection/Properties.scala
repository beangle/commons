/*
 * Beangle, Agile Development Scaffold and Toolkits.
 *
 * Copyright © 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.commons.collection

import org.beangle.commons.bean.{Properties => BeanProperties}

class Properties extends collection.mutable.Map[String, Any] {

  private[this] val data = Collections.newMap[String, Any]

  def this(tuples: (String, _)*) = {
    this()
    tuples foreach { tuple =>
      this.put(tuple._1, tuple._2)
    }
  }

  def this(obj: Object, attrs: String*) = {
    this()
    for (attr <- attrs) {
      val idx = attr.indexOf("->")
      if (-1 == idx) {
        val value = BeanProperties.get[Any](obj, attr)
        if (null != value) this.put(attr, value)
      } else {
        val value = BeanProperties.get[Any](obj, attr.substring(0, idx))
        if (null != value) this.put(attr.substring(idx + 2), value)
      }
    }
  }

  def add(attr: String, obj: Object, nestedAttrs: String*): Unit = {
    if (null != obj)
      put(attr, new Properties(obj, nestedAttrs: _*))
  }

  override def iterator: Iterator[(String, Any)] = {
    data.iterator
  }

  override def addOne(elem: (String, Any)): this.type = {
    data.addOne(elem)
    this
  }

  override def subtractOne(k: String): this.type = {
    data.subtractOne(k)
    this
  }

  override def get(k: String): Option[Any] = {
    data.get(k)
  }
}
