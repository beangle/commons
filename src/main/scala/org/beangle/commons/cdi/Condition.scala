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

package org.beangle.commons.cdi

import org.beangle.commons.config.Enviroment
import org.beangle.commons.lang.ClassLoaders

/** CDI binding conditions. */
object Condition {

  /** Condition that always meets (true). */
  object None extends Condition {
    override def meet(registry: Binder.Registry): Boolean = true
  }

  /** Condition: registry does not contain the given class.
   *
   * @param clazz the bean class to check
   * @return the Condition
   */
  def missing(clazz: Class[_]): Condition = new MissingByClass(clazz)

  /** Condition: classpath has the given resource.
   *
   * @param path the resource path
   * @return the Condition
   */
  def hasResource(path: String): Condition = new HasResource(path)

  /** Condition: system/env has property with the given value (empty value = truthy check).
   *
   * @param name  the property name
   * @param value the expected value
   * @return the Condition
   */
  def hasProperty(name: String, value: String): Condition = new HasProperty(name, value)

  private class MissingByClass(beanClass: Class[_]) extends Condition {
    override def meet(registry: Binder.Registry): Boolean = {
      !registry.contains(beanClass)
    }

    override def toString: String = {
      s"Missing bean ${beanClass.getName}"
    }
  }

  private abstract class EagerCondition extends Condition {
    private val meeted = evaluate()

    final override def meet(registry: Binder.Registry): Boolean = meeted

    protected def evaluate(): Boolean
  }

  private class HasProperty(name: String, value: String) extends EagerCondition {

    protected override def evaluate(): Boolean = {
      val v = Enviroment.Default.getValue(name).getOrElse("false")
      if (value == "") {
        v != "false"
      } else {
        v == value
      }
    }

    override def toString: String = s"Has property ${name}"
  }

  private class HasResource(path: String) extends EagerCondition {
    protected override def evaluate(): Boolean = {
      ClassLoaders.getResource(path).nonEmpty
    }

    override def toString: String = s"Has resource ${path}"
  }
}

/** Condition for conditional bean registration. */
trait Condition {
  /** Returns true if the condition is met for the given registry. */
  def meet(registry: Binder.Registry): Boolean
}
