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
import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers

class MultiLevelSeqGeneratorTest extends FunSpec with ShouldMatchers{

  describe("MultiLevelSeqGenerator"){
    it("Generate multi level sequence"){
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
      sg.getSytle(1).next() should equal ("一")
      sg.getSytle(2).next() should equal ("(一)")
      sg.getSytle(3).next() should equal ("1")
      sg.getSytle(4).next() should equal ("1.1")
    }
  }
}
