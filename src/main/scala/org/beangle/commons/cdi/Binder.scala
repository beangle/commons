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

import org.beangle.commons.bean.{Disposable, Factory, Initializing}
import org.beangle.commons.cdi.Binder.*
import org.beangle.commons.collection.Collections
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.annotation.description

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object Binder {

  case class Reference(ref: String)

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

  case class Variable(name: String, defaultValue: Option[String])

  /** 注册项 */
  sealed abstract class RegistryItem {
    def clazz: Class[_]

    def beanName: String

    def beanClass: Class[_]

    var targetClass: Option[Class[_]] = None

    var condition: Condition = Condition.None

    var profile: Option[String] = None

    var module: Option[String] = None

    var primaryOf: Set[Class[_]] = Set.empty

    var description: Option[String] = None

    def on(con: Condition): this.type = {
      this.condition = con
      this
    }

    def activeOn(profile: String): this.type = {
      this.profile = Option(profile)
      this
    }

    def locateAt(module: String): this.type = {
      this.module = Option(module)
      this
    }
  }

  /** 注册为单例的注册项
   *
   * @param beanName  名称
   * @param singleton 对象
   */
  class Singleton(val beanName: String, val singleton: AnyRef) extends RegistryItem {
    override def beanClass: Class[_] = targetClass.getOrElse(singleton.getClass)

    override def clazz: Class[_] = singleton.getClass
  }

  /** 需要根据配置初始化的注册项
   * Bean Definition
   */
  class Definition(var beanName: String, var clazz: Class[_], scopeName: String) extends RegistryItem {

    var scope: String = if (null == scopeName) "singleton" else scopeName

    var initMethod: Option[String] = None

    var destroyMethod: Option[String] = None

    var properties = new mutable.HashMap[String, Any]

    var lazyInit: Boolean = _

    var abstractFlag: Boolean = _

    var parent: Option[String] = None

    var constructorArgs: mutable.Buffer[Any] = new mutable.ArrayBuffer[Any]

    val nowires = Collections.newSet[String]

    val optionals = Collections.newSet[String]

    var wiredEagerly: Boolean = _

    var factoryBean: Option[String] = None

    var factoryMethod: Option[String] = None

    def isAbstract: Boolean = abstractFlag

    override def beanClass: Class[_] = {
      targetClass.getOrElse(clazz)
    }

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

    def merge(patch: Reconfig.Definition): Unit = {
      // 当类型变化后,删除原有配置
      if (patch.clazz.nonEmpty && !patch.clazz.contains(this.clazz)) {
        this.clazz = patch.clazz.get
        this.properties.clear()
      }
      if patch.primaryOf.nonEmpty then this.primaryOf = patch.primaryOf

      patch.properties foreach { case (p, v) =>
        if (p.startsWith("+")) {
          this.properties.put(p.substring(1), v)
        } else if (p.startsWith("-")) {
          this.properties.remove(p.substring(1))
        } else {
          this.properties.put(p, v)
        }
      }
      if (null != patch.constructorArgs && patch.constructorArgs.nonEmpty) {
        this.constructorArgs = patch.constructorArgs
      }
    }

    override def toString: String = s"$beanName@${clazz.getName}"
  }

  trait Registry {

    /** 是否包含某个bean
     *
     * @param clazz
     * @return
     */
    def contains(clazz: Class[_]): Boolean

    /** Find bean name by type
     */
    def getBeanNames(clazz: Class[_]): List[String]

    /** Register bean definition
     *
     * @param items binding item
     */
    def register(items: Iterable[RegistryItem]): Unit

    /** Whether the bean is primary of given interface
     */
    def isPrimary(name: String, clazz: Class[_]): Boolean

  }

  /** 绑定单个bean或者批量的辅助类
   *
   * @param binder
   * @param classes
   */
  class BatchBinder(val binder: Binder, classes: Class[_]*) {

    private val beans = new ListBuffer[Definition]

    bind(classes: _*)

    def shortName(b: Boolean = true): this.type = {
      for (definition <- beans) definition.beanName = getBeanName(definition.clazz, b)
      this
    }

    def description(descs: String*): this.type = {
      require(descs.size == 1 || descs.size == beans.size)
      if (descs.size == 1) for (definition <- beans) definition.description = Some(descs.head)
      else {
        val beanIter = beans.iterator
        val descIter = descs.iterator
        while (beanIter.hasNext) {
          beanIter.next().description = Some(descIter.next())
        }
      }
      this
    }

    def lazyInit(lazyInit: Boolean = true): this.type = {
      for (definition <- beans) definition.lazyInit = lazyInit
      this
    }

    def parent(parent: String): this.type = {
      for (definition <- beans) definition.parent = Option(parent)
      this
    }

    def proxy(property: String, clazz: Class[_]): this.type = {
      val targetBean = binder.newInnerBeanName(clazz)
      val targetDefinition = new Definition(targetBean, clazz, Scope.Singleton.name)
      val an = clazz.getAnnotation(classOf[description])
      if (null != an) targetDefinition.description = Some(an.value())
      binder.add(targetDefinition)
      for (definition <- beans) {
        definition.targetClass = Some(clazz)
        definition.properties.put(property, Reference(targetBean))
      }
      this
    }

    def proxy(property: String, target: Definition): this.type = {
      binder.add(target)
      for (definition <- beans) {
        definition.targetClass = Some(target.clazz)
        definition.properties.put(property, Reference(target.beanName))
      }
      this
    }

    def primaryOf(clz: Class[_]*): this.type = {
      for (definition <- beans) definition.primaryOf = clz.toSet
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

    def onMissing(): this.type = {
      for (definition <- beans) definition.on(Condition.missing(definition.clazz))
      this
    }

    def onMissing(clazz: Class[_]): this.type = {
      val depends = Condition.missing(clazz)
      for (definition <- beans) definition.on(depends)
      this
    }

    def on(condition: Condition): this.type = {
      for (definition <- beans) definition.on(condition)
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
      for (definition <- beans) definition.initMethod = Some(method)
      this
    }

    def nowire(properties: String*): this.type = {
      if (properties.isEmpty) {
        for (definition <- beans) definition.nowire("*")
      } else {
        for (definition <- beans) definition.nowire(properties: _*)
      }
      this
    }

    def bind(classes: Class[_]*): this.type = {
      for (clazz <- classes) {
        bind(getBeanName(clazz, false), clazz)
      }
      this
    }

    def bind(name: String, clazz: Class[_]): this.type = {
      val dfn = new Definition(name, clazz, Scope.Singleton.name)
      if (classOf[Factory[_]].isAssignableFrom(clazz)) {
        dfn.targetClass = Some(Factory.getObjectType(clazz))
      }
      val an = clazz.getAnnotation(classOf[description])
      if (null != an) dfn.description = Some(an.value())
      if classOf[Initializing].isAssignableFrom(clazz) then dfn.initMethod = Some("init")
      if classOf[Disposable].isAssignableFrom(clazz) then dfn.destroyMethod = Some("destroy")

      binder.add(dfn)
      beans += dfn
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
class Binder(val module: String) {

  val definitions = Collections.newBuffer[Definition]

  val singletons = Collections.newBuffer[Singleton]

  def newInnerBeanName(clazz: Class[_]): String = {
    clazz.getSimpleName + "#" + Math.abs(module.hashCode) + definitions.size
  }

  /**
   * bind class with a name.
   */
  def bind(beanName: String, clazz: Class[_]): BatchBinder = {
    new BatchBinder(this).bind(beanName, clazz)
  }

  /**
   * bind object with a name.
   */
  def bind(beanName: String, singleton: AnyRef): Singleton = {
    val holder = new Singleton(beanName, singleton)
    val clazz = singleton.getClass
    if (singleton.isInstanceOf[Factory[_]]) {
      holder.targetClass = Some(Factory.getObjectType(clazz))
    }
    val an = clazz.getAnnotation(classOf[description])
    if (null != an) holder.description = Some(an.value())

    singletons += holder
    holder
  }

  def bind(classes: Class[_]*): BatchBinder = {
    new BatchBinder(this, classes: _*)
  }

  protected[cdi] def add(definition: Definition): Unit = {
    definitions += definition
  }
}
