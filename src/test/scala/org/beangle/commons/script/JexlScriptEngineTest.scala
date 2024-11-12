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

package org.beangle.commons.script

import org.beangle.commons.lang.reflect.BeanInfos
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class JexlScriptEngineTest extends AnyFunSpec with Matchers {

  describe("JexlScriptEngine evaluate expression") {
    BeanInfos.of(classOf[Depart])
    val evaluator = Jexl3.newEvaluator()
    val data = Map("depart" -> Depart("销售部", "三楼"), "score" -> 95)
    assert("三楼" == evaluator.eval("depart.office", data))
    val s = evaluator.eval("if (score >= 70) score else 70;", data)
    assert("95" == s.toString)
    assert("2" == evaluator.eval("size(depart.staffs)", data).toString)
    assert("zhangsan" == evaluator.eval("depart.staffs[0]", data))
    assert("champion" == evaluator.eval("depart.honors['sale']", data))
  }

}
