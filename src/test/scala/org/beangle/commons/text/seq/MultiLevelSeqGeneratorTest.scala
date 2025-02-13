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

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class MultiLevelSeqGeneratorTest extends AnyFunSpec, Matchers {

  describe("MultiLevelSeqGenerator") {
    it("Generate multi level sequence") {
      val sg = new MultiLevelSeqGenerator
      sg.add(new SeqPattern(SeqNumStyle.HANZI, "{1}"))
      sg.add(new SeqPattern(SeqNumStyle.HANZI, "({2})"))
      sg.add(new SeqPattern(SeqNumStyle.ARABIC, "{3}"))
      sg.add(new SeqPattern(SeqNumStyle.ARABIC, "{3}.{4}"))
      sg.add(new SeqPattern(SeqNumStyle.ARABIC, "{3}.{4}.{5}"))
      sg.add(new SeqPattern(SeqNumStyle.ARABIC, "{3}.{4}.{5}.{6}"))
      sg.add(new SeqPattern(SeqNumStyle.ARABIC, "{3}.{4}.{5}.{6}.{7}"))
      sg.add(new SeqPattern(SeqNumStyle.ARABIC, "{3}.{4}.{5}.{6}.{7}.{8}"))
      sg.add(new SeqPattern(SeqNumStyle.ARABIC, "{3}.{4}.{5}.{6}.{7}.{8}.{9}"))
      sg.getPattern(1).next() should equal("一")
      sg.getPattern(2).next() should equal("(一)")
      sg.getPattern(3).next() should equal("1")
      sg.getPattern(4).next() should equal("1.1")
    }
  }
}
