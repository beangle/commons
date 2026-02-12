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

import org.beangle.commons.lang.{Numbers, Strings}

import java.util.Collections

/** Sequence pattern with style and placeholder levels.
 *
 * @author chaostone
 */
class SeqPattern(val seqNumStyle: SeqNumStyle, val pattern: String) {

  /** Parent generator for nested levels. */
  var generator: MultiLevelSeqGenerator = _

  /** Current nesting level. */
  var level: Int = 0

  private var seq: Int = 0

  private val params = buildParams(pattern);

  private def buildParams(pattern: String): List[Int] = {
    var remainder = pattern
    val paramBuffer = new collection.mutable.ListBuffer[Int]
    var hasNext = true;
    while (hasNext && Strings.isNotEmpty(remainder)) {
      val p = Strings.substringBetween(remainder, "{", "}")
      if Strings.isEmpty(p) then hasNext = false
      else if Numbers.isDigits(p) then paramBuffer += Numbers.toInt(p)
      remainder = Strings.substringAfter(remainder, "{" + p + "}")
    }
    val rs = paramBuffer.sorted
    level = rs.last
    rs.take(rs.size - 1).toList
  }

  /** Returns current sequence as formatted string. */
  def curSeqText(): String = seqNumStyle.build(seq)

  /** Advances sequence and returns formatted string. */
  def next(): String = {
    seq += 1
    var text = pattern
    for (paramLevel <- params)
      text = Strings.replace(text, "{" + paramLevel + "}", generator.getPattern(paramLevel).curSeqText())
    Strings.replace(text, "{" + level + "}", seqNumStyle.build(seq))
  }

  /** Resets sequence to 0. */
  def reset(): Unit =
    seq = 0
}
