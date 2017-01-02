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

import freemarker.template.{ ObjectWrapper, TemplateCollectionModel, TemplateModel, TemplateModelIterator, TemplateSequenceModel }
import freemarker.template.TemplateHashModel
import org.beangle.commons.bean.Properties

class SeqModel(val seq: Seq[_], objectWrapper: ObjectWrapper) extends TemplateCollectionModel with TemplateSequenceModel with TemplateHashModel {

  def get(key: String): TemplateModel = {
    objectWrapper.wrap(Properties.get(seq, key))
  }

  def isEmpty: Boolean = {
    seq.isEmpty
  }

  def get(index: Int): TemplateModel = {
    objectWrapper.wrap(seq(index))
  }

  override def size: Int = {
    seq.size
  }

  override def iterator: TemplateModelIterator = {
    new SeqTemplateModelIterator(seq.iterator, objectWrapper)
  }

  class SeqTemplateModelIterator(val iter: Iterator[_], objectWrapper: ObjectWrapper) extends TemplateModelIterator {
    override def next(): TemplateModel = {
      objectWrapper.wrap(iter.next)
    }

    def hasNext: Boolean = {
      iter.hasNext
    }
  }
}