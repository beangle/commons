/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2016, Beangle Software.
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
package org.beangle.commons.cdi.spring.beans

import java.beans.PropertyEditorSupport
import scala.collection.JavaConverters._
import scala.collection.mutable

/**
 * Property editor for Scala collections, converting any source collection to a given
 * target collection type.
 */
class ScalaCollectionEditor[T, U](val builderFunc: () => mutable.Builder[T, _], val nullAsEmpty: Boolean = false)
    extends PropertyEditorSupport {

  override def setAsText(text: String) {
    setValue(text)
  }

  override def setValue(value: AnyRef) {
    val builder = builderFunc()
    value match {
      case null if !nullAsEmpty => {
        super.setValue(null)
        return
      }
      case null if nullAsEmpty => {
        builder.clear()
      }
      case source: TraversableOnce[T] => {
        builder ++= source
      }
      case javaCollection: java.util.Collection[T] => {
        builder ++= collectionAsScalaIterable(javaCollection)
      }
      case javaMap: java.util.Map[T, U] => {
        val mapBuilder = builder.asInstanceOf[mutable.Builder[(T, U), _]]
        mapBuilder ++= mapAsScalaMap(javaMap)
      }
      case el => {
        builder += el.asInstanceOf[T]
      }
    }
    super.setValue(builder.result())
  }
}
