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

import org.beangle.commons.text.seq.SeqNumStyle.*

/** SeqNumStyle factory and built-in styles. */
object SeqNumStyle {

  /** Chinese numeral style (零, 一, 二, 三...). */
  val HANZI = new HanZiSeqStyle()

  /** Arabic numeral style (1, 2, 3...). */
  val ARABIC = new ArabicSeqStyle()
}

/** SeqNumStyle interface.
 *
 * @author chaostone
 */
trait SeqNumStyle {

  /** Builds the sequence string for the given index.
   *
   * @param seq the sequence index (1-based)
   * @return the formatted string (e.g. "一", "1")
   */
  def build(seq: Int): String
}
