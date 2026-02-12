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

package org.beangle.commons.lang

import java.io.{PrintWriter, StringWriter}

/** Static utility methods pertaining to instances of `Throwable`.
 *
 * @author chaostone
 * @since 3.0
 */
object Throwables {

  /** Propagates throwable as-is if it is an instance of declaredType.
   *
   * @param throwable    the throwable
   * @param declaredType the type to check
   */
  def propagateIfInstanceOf[X <: Throwable](throwable: Throwable, declaredType: Class[X]): Unit =
    if (throwable != null && declaredType.isInstance(throwable)) throw declaredType.cast(throwable)

  /** Propagates `throwable` as-is if it is an instance of `RuntimeException` or `Error`, or else as a last resort, wraps
   * it in a `RuntimeException` then propagates.
   * <p>
   * This method always throws an exception. The `RuntimeException` return type is only for
   * client code to make Java type system happy in case a return value is required by the enclosing
   * method. Example usage:
   *
   * {{{
   * def doSomething():T = {
   *   try {
   *     someMethodThatCouldThrowAnything();
   *   } catch {
   *     case e:IKnowWhatToDoWithThisException => handle(e)
   *     case t:Throwable=>throw Throwables.propagate(t)
   *   }
   * }
   * }}}
   *
   * @param throwable the Throwable to propagate
   * @return nothing will ever be returned; this return type is only for your
   *         convenience, as illustrated in the example above
   */
  def propagate(throwable: Throwable): RuntimeException = {
    propagateIfInstanceOf(throwable, classOf[Error])
    propagateIfInstanceOf(throwable, classOf[RuntimeException])
    throw new RuntimeException(throwable)
  }

  /** Returns toString plus full stack trace of the throwable.
   *
   * @param throwable the throwable
   * @return the formatted string
   */
  def stackTrace(throwable: Throwable): String = {
    val sw = new StringWriter()
    throwable.printStackTrace(new PrintWriter(sw))
    sw.toString
  }
}
