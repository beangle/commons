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

package org.beangle.commons.cdi

import org.beangle.commons.collection.Collections

import scala.collection.mutable

/** Bean reconfiguration definitions. */
object Reconfig {

  /** Bean reconfiguration definition. */
  class Definition(val beanName: String, var configType: ReconfigType) {
    var clazz: Option[Class[_]] = None
    var properties: mutable.Map[String, Any] = Collections.newMap[String, Any]
    var constructorArgs: mutable.Buffer[Any] = Collections.newBuffer[Any]
    var primaryOf: Set[Class[_]] = Set.empty

    /** Sets the target class. */
    def setClass(clazz: Class[_]): this.type = {
      this.clazz = Some(clazz)
      this
    }

    /** Marks this bean as primary for the given types. */
    def primaryOf(clazz: Class[_]*): Unit = {
      this.primaryOf = clazz.toSet
    }

    /** Sets a property. */
    def set(property: String, value: AnyRef): Definition = {
      this.properties.put(property, value)
      this
    }

    /** Removes a property. */
    def remove(property: String): Definition = {
      this.properties.remove(property)
      this.properties.put("-" + property, "--")
      this
    }

    /** Merges a property (adds to existing). */
    def merge(property: String, value: AnyRef): Definition = {
      this.properties.remove(property)
      this.properties.put("+" + property, value)
      this
    }
  }

  enum ReconfigType {
    case Update, Remove
  }
}

/** Runtime CDI reconfiguration (update/remove bean definitions). */
class Reconfig {

  var definitions = Collections.newMap[String, Reconfig.Definition]
  var ignoreMissing: Boolean = true
}
