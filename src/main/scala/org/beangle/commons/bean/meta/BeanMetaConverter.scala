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

package org.beangle.commons.bean.meta

import org.beangle.commons.bean.meta.MetaModel.{ClassMeta, Ctor, Method, Param, Property}
import org.beangle.commons.lang.reflect.{BeanInfo, TypeInfo}
import org.beangle.commons.lang.reflect.TypeInfo.OptionType

/** Converts between BeanInfo and [[MetaModel.ClassMeta]].
  *
  * `from` turns a BeanInfo (compile-time dig via `BeanInfos.of`, or runtime loader)
  * into the reflection-free meta model — Option properties are peeled to their
  * element type with `isOptional = true`, method params become JVM internal names.
  * The reverse direction (ClassMeta → BeanInfo via `BeanInfo.Builder`) is future work.
  */
object BeanMetaConverter {

  /** Converts a BeanInfo into a ClassMeta. */
  def from(bi: BeanInfo): ClassMeta = {
    val properties = bi.properties.values.toSeq.sortBy(_.name).map { p =>
      p.typeinfo match
        case o: OptionType => Property(p.name, o.elementType, p.isTransient, isOptional = true)
        case other => Property(p.name, other, p.isTransient, isOptional = false)
    }
    val ctors = bi.ctors.map(c => Ctor(c.parameters.map(pm => Param(pm.name, pm.typeinfo, pm.defaultValue))))
    val methods = bi.methods.values.flatten.toSeq.map(m =>
      Method(m.getName, m.getParameterTypes.toSeq.map(t => t.getName.replace('.', '/'))))
    ClassMeta(bi.clazz, properties, ctors, methods)
  }
}
