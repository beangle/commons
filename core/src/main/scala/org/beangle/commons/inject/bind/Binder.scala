/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2014, Beangle Software.
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
package org.beangle.commons.inject.bind

import org.beangle.commons.lang.Strings
import Binder._
import scala.collection.mutable.ListBuffer
import org.beangle.commons.inject.Scope

object Binder {

  case class ReferenceValue(ref: String)

  /**
   * Bean Definition
   */
  class Definition(var beanName: String, val clazz: Class[_], scopeName: String) {

    var scope: String = if (null == scopeName) "Singleton" else scopeName

    var initMethod: String = _

    var properties = new collection.mutable.HashMap[String, Any]

    var lazyInit: Boolean = true

    var abstractFlag: Boolean = false

    var primary: Boolean = false

    var parent: String = _

    var targetClass: Class[_] = _

    var constructorArgs: Seq[_] = _

    def isAbstract(): Boolean = abstractFlag

    def property(property: String, value: AnyRef): Definition = {
      properties.put(property, value)
      this
    }
  }

  //FIXME
  class DefinitionBinder(val config: Binder, classes: Class[_]*) {

    private val beans = new ListBuffer[Definition]

    bind(classes: _*)

    def shortName(): this.type = shortName(true)

    def shortName(b: Boolean): this.type = {
      for (definition <- beans) definition.beanName = getBeanName(definition.clazz, b)
      this
    }

    def lazyInit(): this.type = {
      lazyInit(true)
    }

    def lazyInit(lazyInit: Boolean): this.type = {
      for (definition <- beans) definition.lazyInit = lazyInit
      this
    }

    def parent(parent: String): this.type = {
      for (definition <- beans) definition.parent = parent
      this
    }

    def proxy(property: String, clazz: Class[_]): this.type = {
      val targetBean = config.innerBeanName(clazz)
      config.add(new Definition(targetBean, clazz, Scope.Singleton.toString))
      for (definition <- beans) {
        definition.targetClass = clazz
        definition.properties.put(property, new ReferenceValue(targetBean))
      }
      this
    }

    def proxy(property: String, target: Definition): this.type = {
      config.add(target)
      for (definition <- beans) {
        definition.targetClass = target.clazz
        definition.properties.put(property, new ReferenceValue(target.beanName))
      }
      this
    }

    def primary(): this.type = {
      for (definition <- beans) definition.primary = true
      this
    }

    def setAbstract(): this.type = {
      for (definition <- beans) definition.abstractFlag = true
      this
    }

    def in(scope: Scope.Val): this.type = {
      for (definition <- beans) definition.scope = scope.toString
      this
    }

    def property(property: String, value: AnyRef): this.type = {
      for (definition <- beans) definition.properties.put(property, value)
      this
    }

    def constructor(args: Any*): this.type = {
      for (definition <- beans) definition.constructorArgs = args;
      this
    }
    /**
     * Assign init method
     */
    def init(method: String): this.type = {
      for (definition <- beans) definition.initMethod = method
      this
    }

    def bind(classes: Class[_]*): this.type = {
      for (clazz <- classes) {
        val definition = new Definition(getBeanName(clazz, false), clazz, Scope.Singleton.toString)
        config.add(definition)
        beans += definition
      }
      this
    }

    def bind(name: String, clazz: Class[_]): this.type = {
      val definition = new Definition(name, clazz, Scope.Singleton.toString)
      config.add(definition)
      beans += definition
      this
    }

    private def getBeanName(clazz: Class[_], shortName: Boolean): String = {
      var className = clazz.getName
      if (shortName) className = Strings.uncapitalize(Strings.substringAfterLast(className, "."))
      className
    }
  }
}

/**
 * Binder class.
 *
 * @author chaostone
 */
class Binder(val module: String) {

  val definitionBuffer = new ListBuffer[Definition]

  def definitions: List[Definition] = definitionBuffer.toList

  def innerBeanName(clazz: Class[_]): String = {
    clazz.getName + "#" + Math.abs(module.hashCode) + definitions.size
  }

  /**
   * bind.
   */
  def bind(beanName: String, clazz: Class[_]): DefinitionBinder = {
    new DefinitionBinder(this).bind(beanName, clazz)
  }

  /**
   * bind.
   */
  def bind(classes: Class[_]*): DefinitionBinder = {
    new DefinitionBinder(this, classes: _*)
  }
  /**
   * add.
   */
  protected[bind] def add(definition: Definition) {
    definitionBuffer += definition
  }
}
