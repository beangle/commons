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

import org.beangle.commons.cdi.Binding.*
import org.beangle.commons.collection.Collections
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.annotation.description

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object Binding {

  case class ReferenceValue(ref: String)

  case class Injection[T](clazz: Class[T])

  object InjectPlaceHolder

  object Variable {
    def apply(name: String): Variable = {
      val commaIdx = name.indexOf(':')
      if (commaIdx > 0 && commaIdx < name.length - 1) {
        Variable(name.substring(0, commaIdx), Some(name.substring(commaIdx + 1)))
      } else {
        Variable(name, None)
      }
    }
  }

  object PropertyPlaceHolder {
    val Prefix: String = "${"
    val Suffix: String = "}"

    def hasVariable(pattern: String): Boolean = {
      val startIdx = pattern.indexOf(Prefix)
      if (startIdx > -1) {
        val endIdx = pattern.indexOf(Suffix, startIdx)
        endIdx - startIdx > 2
      } else {
        false
      }
    }

    def apply(pattern: String): PropertyPlaceHolder = {
      var n = pattern
      val variables = Collections.newSet[Variable]
      while
        val v = Strings.substringBetween(n, Prefix, Suffix)
        val hasVar = Strings.isNotBlank(v)
        if (hasVar) {
          variables.addOne(Variable(v))
          n = Strings.replace(n, Prefix + v + Suffix, "")
        }
        hasVar
      do {}
      PropertyPlaceHolder(pattern, variables.toSet)
    }
  }

  case class Variable(name: String, defaultValue: Option[String])

  case class PropertyPlaceHolder(pattern: String, variables: Set[Variable])

  /**
   * Bean Definition
   */
  class Definition(var beanName: String, var clazz: Class[_], scopeName: String) {

    var scope: String = if (null == scopeName) "singleton" else scopeName

    var initMethod: String = _

    var destroyMethod: String = _

    var properties = new collection.mutable.HashMap[String, Any]

    var lazyInit: Boolean = _

    var abstractFlag: Boolean = _

    var primary: Boolean = _

    var parent: String = _

    var targetClass: Class[_] = _

    var constructorArgs: mutable.Buffer[Any] = _

    var description: String = _

    val nowires = Collections.newSet[String]

    val optionals = Collections.newSet[String]

    var wiredEagerly: Boolean = _

    var factoryBean: String = _

    var factoryMethod: String = _

    def isAbstract: Boolean = abstractFlag

    def property(property: String, value: AnyRef): Definition = {
      properties.put(property, value)
      this
    }

    def constructor(args: Any*): this.type = {
      this.constructorArgs = args.toBuffer
      this
    }

    def nowire(properties: String*): this.type = {
      nowires ++= properties
      this
    }

    def optional(properties: String*): this.type = {
      optionals ++= properties
      this
    }

    def wiredEagerly(newvalue: Boolean): this.type = {
      this.wiredEagerly = newvalue
      this
    }
  }

  class DefinitionBinder(val config: Binding, classes: Class[_]*) {

    private val beans = new ListBuffer[Definition]

    bind(classes: _*)

    def shortName(b: Boolean = true): this.type = {
      for (definition <- beans) definition.beanName = getBeanName(definition.clazz, b)
      this
    }

    def description(descs: String*): this.type = {
      require(descs.size == 1 || descs.size == beans.size)
      if (descs.size == 1) for (definition <- beans) definition.description = descs.head
      else {
        val beanIter = beans.iterator
        val descIter = descs.iterator
        while (beanIter.hasNext) {
          beanIter.next().description = descIter.next()
        }
      }
      this
    }

    def lazyInit(lazyInit: Boolean = true): this.type = {
      for (definition <- beans) definition.lazyInit = lazyInit
      this
    }

    def parent(parent: String): this.type = {
      for (definition <- beans) definition.parent = parent
      this
    }

    def proxy(property: String, clazz: Class[_]): this.type = {
      val targetBean = config.newInnerBeanName(clazz)
      val targetDefinition = new Definition(targetBean, clazz, Scope.Singleton.name)
      val an = clazz.getAnnotation(classOf[description])
      if (null != an) targetDefinition.description = an.value()
      config.add(targetDefinition)
      for (definition <- beans) {
        definition.targetClass = clazz
        definition.properties.put(property, ReferenceValue(targetBean))
      }
      this
    }

    def proxy(property: String, target: Definition): this.type = {
      config.add(target)
      for (definition <- beans) {
        definition.targetClass = target.clazz
        definition.properties.put(property, ReferenceValue(target.beanName))
      }
      this
    }

    def primary(): this.type = {
      for (definition <- beans) definition.primary = true
      this
    }

    def optional(properties: String*): this.type = {
      for (definition <- beans) {
        definition.optionals ++= properties
      }
      this
    }

    def wiredEagerly(newvalue: Boolean): this.type = {
      for (definition <- beans) {
        definition.wiredEagerly = newvalue
      }
      this
    }

    def setAbstract(): this.type = {
      for (definition <- beans) definition.abstractFlag = true
      this
    }

    def in(scope: Scope): this.type = {
      for (definition <- beans) definition.scope = scope.name
      this
    }

    def property(property: String, value: Any): this.type = {
      for (definition <- beans) definition.properties.put(property, value)
      this
    }

    def constructor(args: Any*): this.type = {
      for (definition <- beans) definition.constructorArgs = args.toBuffer
      this
    }

    def init(method: String): this.type = {
      for (definition <- beans) definition.initMethod = method
      this
    }

    def nowire(properties: String*): this.type = {
      for (definition <- beans) definition.nowire(properties: _*)
      this
    }

    def bind(classes: Class[_]*): this.type = {
      for (clazz <- classes) {
        val definition = new Definition(getBeanName(clazz, false), clazz, Scope.Singleton.name)
        val an = clazz.getAnnotation(classOf[description])
        if (null != an) definition.description = an.value()
        config.add(definition)
        beans += definition
      }
      this
    }

    def bind(name: String, clazz: Class[_]): this.type = {
      val definition = new Definition(name, clazz, Scope.Singleton.name)
      val an = clazz.getAnnotation(classOf[description])
      if (null != an) definition.description = an.value()
      config.add(definition)
      beans += definition
      this
    }

    private def getBeanName(clazz: Class[_], shortName: Boolean): String = {
      var className = clazz.getName
      if (shortName) className = Strings.uncapitalize(Strings.substringAfterLast(className, "."))
      className
    }

    def head: Definition = beans.head
  }

}

/**
 * Module's Binding Space
 *
 * @author chaostone
 */
class Binding(val module: String) {

  val definitions = new ListBuffer[Definition]

  val singletons = Collections.newMap[String, AnyRef]

  def newInnerBeanName(clazz: Class[_]): String = {
    clazz.getSimpleName + "#" + Math.abs(module.hashCode) + definitions.size
  }

  /**
   * bind class with a name.
   */
  def bind(beanName: String, clazz: Class[_]): DefinitionBinder = {
    new DefinitionBinder(this).bind(beanName, clazz)
  }

  /**
   * bind object with a name.
   */
  def bind(beanName: String, singleton: AnyRef): Unit = {
    singletons += (beanName -> singleton)
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
  protected[cdi] def add(definition: Definition): Unit = {
    definitions += definition
  }
}
