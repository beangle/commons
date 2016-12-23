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

import scala.util.matching.Regex

import org.springframework.beans.{ PropertyEditorRegistry, PropertyEditorRegistrar }

/**
 * Property editor registrar for Scala property editors.
 *
 */
class ScalaEditorRegistrar extends PropertyEditorRegistrar {

  def registerCustomEditors(registry: PropertyEditorRegistry) {
    // Types
    registry.registerCustomEditor(classOf[Regex], new RegexEditor())

    // Seq
    registry.registerCustomEditor(classOf[collection.Seq[Any]], new ScalaCollectionEditor(collection.Seq.newBuilder[Any] _))
    registry.registerCustomEditor(classOf[collection.immutable.Seq[Any]], new ScalaCollectionEditor(collection.immutable.Seq.newBuilder[Any] _))
    registry.registerCustomEditor(classOf[collection.mutable.Seq[Any]], new ScalaCollectionEditor(collection.mutable.Seq.newBuilder[Any] _))

    // IndexedSeq
    registry.registerCustomEditor(classOf[collection.IndexedSeq[Any]], new ScalaCollectionEditor(collection.IndexedSeq.newBuilder[Any] _))
    registry.registerCustomEditor(classOf[collection.immutable.IndexedSeq[Any]], new ScalaCollectionEditor(collection.immutable.IndexedSeq.newBuilder[Any] _))
    registry.registerCustomEditor(classOf[collection.mutable.IndexedSeq[Any]], new ScalaCollectionEditor(collection.mutable.IndexedSeq.newBuilder[Any] _))

    // ResizableArray
    registry.registerCustomEditor(classOf[collection.mutable.ResizableArray[Any]], new ScalaCollectionEditor(collection.mutable.ResizableArray.newBuilder[Any] _))

    // LinearSeq
    registry.registerCustomEditor(classOf[collection.LinearSeq[Any]], new ScalaCollectionEditor(collection.LinearSeq.newBuilder[Any] _))
    registry.registerCustomEditor(classOf[collection.immutable.LinearSeq[Any]], new ScalaCollectionEditor(collection.immutable.LinearSeq.newBuilder[Any] _))
    registry.registerCustomEditor(classOf[collection.mutable.LinearSeq[Any]], new ScalaCollectionEditor(collection.mutable.LinearSeq.newBuilder[Any] _))

    // Buffer
    registry.registerCustomEditor(classOf[collection.mutable.Buffer[Any]], new ScalaCollectionEditor(collection.mutable.Buffer.newBuilder[Any] _))
    registry.registerCustomEditor(classOf[collection.mutable.ListBuffer[Any]], new ScalaCollectionEditor(collection.mutable.ListBuffer.newBuilder[Any] _))

    // Set
    registry.registerCustomEditor(classOf[collection.Set[Any]], new ScalaCollectionEditor(collection.Set.newBuilder[Any] _))
    registry.registerCustomEditor(classOf[collection.immutable.Set[Any]], new ScalaCollectionEditor(collection.immutable.Set.newBuilder[Any] _))
    registry.registerCustomEditor(classOf[collection.mutable.Set[Any]], new ScalaCollectionEditor(collection.mutable.Set.newBuilder[Any] _))

    // Map
    registry.registerCustomEditor(classOf[collection.Map[Any, Any]], new ScalaCollectionEditor(collection.Map.newBuilder[Any, Any] _))
    registry.registerCustomEditor(classOf[collection.immutable.Map[Any, Any]], new ScalaCollectionEditor(collection.immutable.Map.newBuilder[Any, Any] _))
    registry.registerCustomEditor(classOf[collection.mutable.Map[Any, Any]], new ScalaCollectionEditor(collection.mutable.Map.newBuilder[Any, Any] _))
    registry.registerCustomEditor(classOf[collection.mutable.HashMap[Any, Any]], new ScalaCollectionEditor(collection.mutable.HashMap.newBuilder[Any, Any] _))
  }
}
