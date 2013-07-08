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

import SeqNumStyle._

object SeqNumStyle {

  /**
   中文数字
   */
  val HANZI = new HanZiSeqStyle()

  /**
   数字
   */
  val ARABIC = new ArabicSeqStyle()
}

/**
 * <p>
 * SeqNumStyle interface.
 * </p>
 *
 * @author chaostone
 * @version $Id: $
 */
trait SeqNumStyle {

  /**
   * <p>
   * build.
   * </p>
   *
   * @param seq a int.
   * @return a {@link java.lang.String} object.
   */
  def build(seq: Int): String
}
