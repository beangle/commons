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

import org.beangle.commons.script.{EvaluationException, ExpressionEvaluator}

import javax.script.{ScriptEngine, ScriptEngineManager, SimpleBindings}

class JSR223ExpressionEvaluator(engineName: String) extends ExpressionEvaluator {
  var scriptEngine = new ScriptEngineManager().getEngineByName(engineName)

  def parse(exp: String): Unit = {}

  def eval(exp: String, root: AnyRef): AnyRef = {
    val ctx = new SimpleBindings
    root match {
      case sm: collection.Map[String, Any] => sm foreach (x => ctx.put(x._1, x._2))
      case jm: java.util.Map[String, Any] => ctx.putAll(jm)
      case _ => ctx.put("root", root)
    }
    scriptEngine.eval(exp, ctx)
  }

  def eval[T](exp: String, root: AnyRef, resultType: Class[T]): T = {
    eval(exp, root).asInstanceOf[T]
  }
}
