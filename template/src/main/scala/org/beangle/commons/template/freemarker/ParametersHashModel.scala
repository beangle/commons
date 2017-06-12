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
package org.beangle.commons.template.freemarker

import freemarker.template.TemplateHashModelEx
import freemarker.template.SimpleCollection
import freemarker.template.TemplateModel
import freemarker.template.TemplateCollectionModel
import freemarker.template.SimpleScalar
import java.{ util => ju }
import freemarker.template.ObjectWrapper

class ParametersHashModel(val params: Map[String, Any], wrapper: ObjectWrapper) extends TemplateHashModelEx {
  override def get(key: String): TemplateModel = {
    params.get(key) match {
      case Some(v) => {
        if (v.getClass.isArray) {
          new SimpleScalar(v.asInstanceOf[Array[_]](0).asInstanceOf[String])
        } else new SimpleScalar(v.asInstanceOf[String])
      }
      case None => null
    }
  }

  override def isEmpty: Boolean = {
    params.isEmpty
  }

  override def size: Int = {
    params.size
  }

  override def keys: TemplateCollectionModel = {
    import scala.collection.JavaConverters._
    new SimpleCollection(asJavaIterator(params.keys.iterator), wrapper)
  }

  override def values: TemplateCollectionModel = {
    val iter = params.keys.iterator
    val javaIter = new ju.Iterator[Any]() {
      override def hasNext: Boolean = {
        iter.hasNext
      }
      override def next: Any = {
        params(iter.next)
      }
      override def remove {
        throw new UnsupportedOperationException();
      }
    }

    new SimpleCollection(javaIter, wrapper)
  }
}
