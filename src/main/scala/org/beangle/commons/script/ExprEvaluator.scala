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

import javax.script.ScriptEngineManager

/** Expression evaluator.
 *
 * @author chaostone
 * @since 2012-03-05
 */
trait ExprEvaluator {

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

/** ExprEvaluator factory (Jexl3, JSR223). */
object ExprEvaluator {

  /** Jexl3 engine name for `get`. */
  final val Jexl3Engine = "jexl3"

  /** Gets evaluator by name (`Jexl3Engine` or JSR223 engine name).
   *
   * @param engineName engine name (e.g. `Jexl3Engine`)
   * @return ExprEvaluator instance
   */
  def get(engineName: String): ExprEvaluator = {
    engineName match
      case Jexl3Engine => new JexlExprEvaluator(Jexl3.newEngine())
      case _ =>
        val engine = new ScriptEngineManager().getEngineByName(engineName)
        require(engine != null, s"Cannot find script engine named ${engineName}")
        new JSR223ExprEvaluator(engine)
  }
}
