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

import org.apache.commons.jexl3.JexlBuilder
import org.apache.commons.jexl3.introspection.JexlPermissions
import org.apache.commons.jexl3.scripting.JexlScriptEngine
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class JexlScriptEngineTest extends AnyFunSpec with Matchers {

  describe("JexlScriptEngine evaluate expression") {
    val jexlBuilder = new JexlBuilder().cache(512).strict(true).silent(false)
    jexlBuilder.permissions(JexlPermissions.RESTRICTED.compose("org.beangle.*"))
    val jexl = jexlBuilder.create()
    JexlScriptEngine.setInstance(jexl)
    //    JexlScriptEngine.setPermissions(JexlPermissions.RESTRICTED.compose("org.beangle.*"))
    val evaluator = JSR223ExpressionEvaluator("jexl3")

    val data = Map("depart" -> Depart("销售部", "三楼"), "score" -> 95)
    assert("三楼" == evaluator.eval("depart.office()", data))
    val s = evaluator.eval("if (score >= 70) score else 70;", data)
    assert("95" == s.toString)
  }

}
