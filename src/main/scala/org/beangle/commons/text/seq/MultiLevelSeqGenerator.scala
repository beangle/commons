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

package org.beangle.commons.text.seq

import scala.collection.mutable

/** Manages SeqPatterns by level for multi-level numbering.
 *
 * @author chaostone
 */
class MultiLevelSeqGenerator {

  private val patterns = new mutable.HashMap[Int, SeqPattern]

  /** Returns the SeqPattern for the given level. */
  def getPattern(level: Int): SeqPattern = patterns(level)

  /** Advances and returns the next sequence string at level. */
  def next(level: Int): String = getPattern(level).next()

  /** Adds pattern and registers this as its generator. */
  def add(style: SeqPattern): Unit = {
    style.generator = this
    patterns.put(style.level, style)
  }

  /** Resets pattern at level. */
  def reset(level: Int): Unit =
    getPattern(level).reset()
}
