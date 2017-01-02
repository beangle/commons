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
package org.beangle.commons.template.freemarker

import org.beangle.commons.lang.reflect.{ BeanInfos, ClassInfos }

import freemarker.core.CollectionAndSequence
import freemarker.ext.beans.BeansWrapper
import freemarker.ext.util.WrapperTemplateModel
import freemarker.template.{ SimpleSequence, TemplateCollectionModel, TemplateHashModelEx, TemplateModel, TemplateScalarModel }

class StringModel(obj: AnyRef, wrapper: BeansWrapper) extends TemplateHashModelEx
    with WrapperTemplateModel with TemplateScalarModel {

  override def get(key: String): TemplateModel = {
    if (key == "class") return wrapper.wrap(obj.getClass)
    else {
      BeanInfos.Default.get(obj.getClass).getGetter(key) match {
        case Some(s) =>
          wrapper.wrap(s.invoke(obj))
        case None =>
          val methods = ClassInfos.get(obj.getClass).getMethods(key)
          if (methods.isEmpty) wrapper.wrap(null) else new SimpleMethodModel(obj, methods, wrapper)
      }
    }
  }

  override def getWrappedObject(): AnyRef = {
    obj
  }

  override def size(): Int = {
    BeanInfos.Default.get(obj.getClass).properties.size
  }

  override def keys(): TemplateCollectionModel = {
    val properties = BeanInfos.Default.get(obj.getClass).properties
    val keySet = collection.JavaConverters.setAsJavaSet(properties.keySet)
    new CollectionAndSequence(new SimpleSequence(keySet, wrapper))
  }

  override def values(): TemplateCollectionModel = {
    val properties = BeanInfos.Default.get(obj.getClass).properties
    val values = new java.util.ArrayList[Any](properties.size)
    val it = keys().iterator();
    while (it.hasNext()) {
      val key = it.next().asInstanceOf[TemplateScalarModel].getAsString();
      values.add(get(key))
    }
    return new CollectionAndSequence(new SimpleSequence(values, wrapper))
  }

  override def isEmpty(): Boolean = {
    obj match {
      case null                 => true
      case s: String            => s.length() == 0
      case s: java.lang.Boolean => s == java.lang.Boolean.FALSE
      case _                    => false
    }
  }
  override def getAsString(): String = {
    obj.toString
  }
}
