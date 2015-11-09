/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2015, Beangle Software.
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

import java.util.Collections
import org.beangle.commons.lang.Numbers
import org.beangle.commons.lang.Strings

/**
 * SeqPattern class.
 *
 * @author chaostone
 */
class SeqPattern(val seqNumStyle: SeqNumStyle, val pattern: String) {

  var generator: MultiLevelSeqGenerator = _

  var level: Int = 0

  private var seq: Int = 0

  private val params = buildParams(pattern);

  private def buildParams(pattern: String): List[Int] = {
    var remainder = pattern
    val paramBuffer = new collection.mutable.ListBuffer[Int]
    var hasNext = true;
    while (hasNext && Strings.isNotEmpty(remainder)) {
      val p = Strings.substringBetween(remainder, "{", "}")
      if (Strings.isEmpty(p)) {
        hasNext = false
      } else {
        if (Numbers.isDigits(p)) paramBuffer += Numbers.toInt(p)
      }
      remainder = Strings.substringAfter(remainder, "{" + p + "}")
    }
    val rs = paramBuffer.sorted
    level = rs(rs.size - 1)
    rs.take(rs.size - 1).toList
  }

  /**
   * <p>
   * curSeqText.
   * </p>
   *
   * @return a String object.
   */
  def curSeqText(): String = seqNumStyle.build(seq)

  /**
   * <p>
   * next.
   * </p>
   *
   * @return a String object.
   */
  def next(): String = {
    seq += 1
    var text = pattern
    for (paramLevel <- params) {
      text = Strings.replace(text, "{" + paramLevel + "}", generator.getPattern(paramLevel).curSeqText())
    }
    Strings.replace(text, "{" + level + "}", seqNumStyle.build(seq))
  }

  /**
   * <p>
   * reset.
   * </p>
   */
  def reset() {
    seq = 0
  }
}
