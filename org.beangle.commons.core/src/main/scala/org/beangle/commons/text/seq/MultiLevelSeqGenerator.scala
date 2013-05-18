/*
 * Beangle, Agile Java/Scala Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2013, Beangle Software.
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
package org.beangle.commons.text.seq

import java.util.Map
import org.beangle.commons.collection.CollectUtils
//remove if not needed
import scala.collection.JavaConversions._

/**
 * <p>
 * MultiLevelSeqGenerator class.
 * </p>
 *
 * @author chaostone
 * @version $Id: $
 */
class MultiLevelSeqGenerator {

  private val patterns = CollectUtils.newHashMap[Int,SeqPattern]()

  /**
   * <p>
   * getSytle.
   * </p>
   *
   * @param level a int.
   * @return a {@link org.beangle.commons.text.seq.SeqPattern} object.
   */
  def getSytle(level: Int): SeqPattern = patterns.get(level)

  /**
   * <p>
   * next.
   * </p>
   *
   * @param level a int.
   * @return a {@link java.lang.String} object.
   */
  def next(level: Int): String = getSytle(level).next()

  /**
   * <p>
   * add.
   * </p>
   *
   * @param style a {@link org.beangle.commons.text.seq.SeqPattern} object.
   */
  def add(style: SeqPattern) {
    style.setGenerator(this)
    patterns.put(style.getLevel, style)
  }

  /**
   * <p>
   * reset.
   * </p>
   *
   * @param level a int.
   */
  def reset(level: Int) {
    patterns.get(level).reset()
  }
}
