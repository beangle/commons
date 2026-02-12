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

import javax.script.{ScriptEngine, ScriptEngineManager, SimpleBindings}

/** Expression evaluator.
 *
 * @author chaostone
 * @since 2012-03-05
 */
trait ExpressionEvaluator {

  /** Parses the expression (optional, no-op for some engines).
   *
   * @param exp the expression string
   */
  def parse(exp: String): Unit

  /** Evaluates an expression within the given context.
   *
   * @param exp  the expression string
   * @param root context params (Map or object)
   * @return evaluation result
   */
  def eval(exp: String, root: AnyRef): AnyRef

  /** Evaluates an expression and returns the result as the specified type.
   *
   * @param exp        the expression string
   * @param root       context params
   * @param resultType the expected result type
   * @return typed result
   */
  def eval[T](exp: String, root: AnyRef, resultType: Class[T]): T
}

/** ExpressionEvaluator factory (Jexl3, JSR223). */
object ExpressionEvaluator {

  /** Gets evaluator by name (jexl3 or JSR223 engine name).
   *
   * @param engineName engine name (e.g. "jexl3")
   * @return ExpressionEvaluator instance
   */
  def get(engineName: String): ExpressionEvaluator = {
    engineName match
      case "jexl3" => Jexl3.newEvaluator()
      case _ => jsr223(engineName)
  }

  /** Creates JSR 223 ScriptEngine-based evaluator.
   *
   * @param engineName JSR 223 engine name (e.g. "javascript", "nashorn")
   * @return ExpressionEvaluator instance
   */
  def jsr223(engineName: String): ExpressionEvaluator = {
    val engine = new ScriptEngineManager().getEngineByName(engineName)
    require(engine != null, s"Cannot find script engine named ${engineName}")
    new JSR223ExpressionEvaluator(engine)
  }
}

/** ExpressionEvaluator implementation using JSR 223 ScriptEngine. */
class JSR223ExpressionEvaluator(engine: ScriptEngine) extends ExpressionEvaluator {
  def parse(exp: String): Unit = {}

  def eval(exp: String, root: AnyRef): AnyRef = {
    val ctx = new SimpleBindings
    root match {
      case sm: collection.Map[_, _] => sm foreach (x => ctx.put(x._1.toString, x._2))
      case jm: java.util.Map[_, _] =>
        val jmi = jm.entrySet().iterator()
        while (jmi.hasNext) {
          val i = jmi.next()
          ctx.put(i.getKey.toString, i.getValue)
        }
      case _ => ctx.put("root", root)
    }
    engine.eval(exp, ctx)
  }

  def eval[T](exp: String, root: AnyRef, resultType: Class[T]): T = {
    eval(exp, root).asInstanceOf[T]
  }
}
