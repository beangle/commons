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

object Reconfig {
  class Definition(val name: String, var configType: ReconfigType, var definition: Binding.Definition) {

    def setClass(clazz: Class[_]): this.type = {
      definition.clazz = clazz
      this
    }

    def primaryOf(clazz: Class[_]): Unit = {
      configType = ReconfigType.Primary
      definition.clazz = clazz
    }

    def set(property: String, value: AnyRef): Definition = {
      definition.properties.put(property, value)
      this
    }

    def remove(property: String): Definition = {
      definition.properties.remove(property)
      definition.properties.put("-" + property, "--")
      this
    }

    def merge(property: String, value: AnyRef): Definition = {
      definition.properties.remove(property)
      definition.properties.put("+" + property, value)
      this
    }
  }

  enum ReconfigType {
    case Update, Remove, Primary
  }
}

class Reconfig {

  var definitions = Collections.newMap[String, Reconfig.Definition]

  var properties = Collections.newMap[String, String]

  var ignoreMissing: Boolean = true
}
