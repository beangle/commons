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

/** CDI binding DSL (Reference, Injection, Variable, RegistryItem, BatchBinder). */
object Binder {

  case class Reference(ref: String)

  case class Injection[T](clazz: Class[T])

  object InjectPlaceHolder

  object Variable {
    /** Creates a Variable for profile/module binding (name or "name:default"). */
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

  /** Base class for container registration items. */
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

    /** Sets the condition for this item to be active.
     *
     * @param con the condition
     * @return this for chaining
     */
    def on(con: Condition): this.type = {
      this.condition = con
      this
    }

    /** Activates this item only when the given profile is active.
     *
     * @param profile the profile name
     * @return this for chaining
     */
    def activeOn(profile: String): this.type = {
      this.profile = Option(profile)
      this
    }

    /** Associates this item with a module.
     *
     * @param module the module name
     * @return this for chaining
     */
    def locateAt(module: String): this.type = {
      this.module = Option(module)
      this
    }
  }

  /** Registry item for a singleton bean.
   *
   * @param beanName  the bean name
   * @param singleton the singleton instance
   */
  class Singleton(val beanName: String, val singleton: AnyRef) extends RegistryItem {
    override def beanClass: Class[_] = targetClass.getOrElse(singleton.getClass)

    override def clazz: Class[_] = singleton.getClass
  }

  /** Registry item for a bean that requires configuration-based initialization (Bean Definition). */
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

    /** Sets a property value for this definition.
     *
     * @param property the property name
     * @param value    the value
     * @return this for chaining
     */
    def property(property: String, value: AnyRef): Definition = {
      properties.put(property, value)
      this
    }

    /** Sets constructor arguments for this definition.
     *
     * @param args the constructor arguments
     * @return this for chaining
     */
    def constructor(args: Any*): this.type = {
      this.constructorArgs = args.toBuffer
      this
    }

    /** Excludes properties from autowiring.
     *
     * @param properties property names to exclude
     * @return this for chaining
     */
    def nowire(properties: String*): this.type = {
      nowires ++= properties
      this
    }

    /** Marks properties as optional (no error if bean not found).
     *
     * @param properties property names
     * @return this for chaining
     */
    def optional(properties: String*): this.type = {
      optionals ++= properties
      this
    }

    /** Controls whether dependencies are wired eagerly.
     *
     * @param newvalue true for eager wiring
     * @return this for chaining
     */
    def wiredEagerly(newvalue: Boolean): this.type = {
      this.wiredEagerly = newvalue
      this
    }

    /** Merges patch config into this definition (properties, constructor, etc.).
     *
     * @param patch the patch to apply
     */
    def merge(patch: Reconfig.Definition): Unit = {
      // Remove existing config when type changes
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

    /** Checks whether a bean of the given type is registered.
     *
     * @param clazz the bean class to check
     * @return true if a bean of this type exists
     */
    def contains(clazz: Class[_]): Boolean

    /** Finds bean names by type.
     *
     * @param clazz the bean class to look up
     * @return list of bean names
     */
    def getBeanNames(clazz: Class[_]): List[String]

    /** Register bean definition
     *
     * @param items binding item
     */
    def register(items: Iterable[RegistryItem]): Unit

    /** Checks whether the bean is primary for the given interface.
     *
     * @param name  the bean name
     * @param clazz the interface class
     * @return true if primary
     */
    def isPrimary(name: String, clazz: Class[_]): Boolean

  }

  /** Helper for binding single or multiple bean classes.
   *
   * @param binder  the Binder to register with
   * @param classes the bean classes to bind
   */
  class BatchBinder(val binder: Binder, classes: Class[_]*) {

    private val beans = new ListBuffer[Definition]

    bind(classes: _*)

    /** Uses short class name as bean name (e.g. "userService" instead of full FQN).
     *
     * @param b true for short name
     * @return this for chaining
     */
    def shortName(b: Boolean = true): this.type = {
      for (definition <- beans) definition.beanName = getBeanName(definition.clazz, b)
      this
    }

    /** Sets description(s) for bean(s); one or per-bean. */
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

    /** Sets lazy initialization for beans. */
    def lazyInit(lazyInit: Boolean = true): this.type = {
      for (definition <- beans) definition.lazyInit = lazyInit
      this
    }

    /** Sets parent bean name for beans. */
    def parent(parent: String): this.type = {
      for (definition <- beans) definition.parent = Option(parent)
      this
    }

    /** Wires property to inner bean of type clazz. */
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

    /** Wires property to target definition. */
    def proxy(property: String, target: Definition): this.type = {
      binder.add(target)
      for (definition <- beans) {
        definition.targetClass = Some(target.clazz)
        definition.properties.put(property, Reference(target.beanName))
      }
      this
    }

    /** Marks beans as primary for given types. */
    def primaryOf(clz: Class[_]*): this.type = {
      for (definition <- beans) definition.primaryOf = clz.toSet
      this
    }

    /** Marks properties as optional (no error if absent). */
    def optional(properties: String*): this.type = {
      for (definition <- beans) {
        definition.optionals ++= properties
      }
      this
    }

    /** Sets eager wiring for beans. */
    def wiredEagerly(newvalue: Boolean): this.type = {
      for (definition <- beans) {
        definition.wiredEagerly = newvalue
      }
      this
    }

    /** Marks beans as abstract (template only). */
    def setAbstract(): this.type = {
      for (definition <- beans) definition.abstractFlag = true
      this
    }

    /** Sets scope for beans. */
    def in(scope: Scope): this.type = {
      for (definition <- beans) definition.scope = scope.name
      this
    }

    /** Adds condition: bean's class missing from classpath. */
    def onMissing(): this.type = {
      for (definition <- beans) definition.on(Condition.missing(definition.clazz))
      this
    }

    /** Adds condition: clazz missing from classpath. */
    def onMissing(clazz: Class[_]): this.type = {
      val depends = Condition.missing(clazz)
      for (definition <- beans) definition.on(depends)
      this
    }

    /** Adds activation condition for beans. */
    def on(condition: Condition): this.type = {
      for (definition <- beans) definition.on(condition)
      this
    }

    /** Sets a property value for all bound definitions.
     *
     * @param property the property name
     * @param value    the value
     * @return this for chaining
     */
    def property(property: String, value: Any): this.type = {
      for (definition <- beans) definition.properties.put(property, value)
      this
    }

    /** Sets constructor arguments for all bound definitions.
     *
     * @param args the constructor arguments
     * @return this for chaining
     */
    def constructor(args: Any*): this.type = {
      for (definition <- beans) definition.constructorArgs = args.toBuffer
      this
    }

    /** Sets the init method name for all bound definitions.
     *
     * @param method the method name
     * @return this for chaining
     */
    def init(method: String): this.type = {
      for (definition <- beans) definition.initMethod = Some(method)
      this
    }

    /** Excludes properties from autowiring for all bound definitions.
     *
     * @param properties property names to exclude (or empty for all)
     * @return this for chaining
     */
    def nowire(properties: String*): this.type = {
      if (properties.isEmpty) {
        for (definition <- beans) definition.nowire("*")
      } else {
        for (definition <- beans) definition.nowire(properties: _*)
      }
      this
    }

    /** Binds classes (one bean per class, default name). */
    def bind(classes: Class[_]*): this.type = {
      for (clazz <- classes) {
        bind(getBeanName(clazz, false), clazz)
      }
      this
    }

    /** Binds a named bean. */
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

    /** Returns the first bean definition. */
    def head: Definition = beans.head
  }

}

/**
 * Module's Binding Space
 *
 * @author chaostone
 */
class Binder(val module: String) {

  /** Bean definitions for this binder. */
  val definitions = Collections.newBuffer[Definition]

  /** Singleton instances for this binder. */
  val singletons = Collections.newBuffer[Singleton]

  /** Creates a unique inner bean name for the given class.
   *
   * @param clazz the bean class
   * @return unique name (e.g. "ClassName#hashCode")
   */
  def newInnerBeanName(clazz: Class[_]): String = {
    clazz.getSimpleName + "#" + Math.abs(module.hashCode) + definitions.size
  }

  /** Binds a class with the given bean name.
   *
   * @param beanName the bean name
   * @param clazz    the bean class
   * @return BatchBinder for further configuration
   */
  def bind(beanName: String, clazz: Class[_]): BatchBinder = {
    new BatchBinder(this).bind(beanName, clazz)
  }

  /** Binds a singleton instance with the given bean name.
   *
   * @param beanName  the bean name
   * @param singleton the singleton instance
   * @return the Singleton registry item
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

  /** Binds multiple classes (using short class names as bean names).
   *
   * @param classes the bean classes
   * @return BatchBinder for further configuration
   */
  def bind(classes: Class[_]*): BatchBinder = {
    new BatchBinder(this, classes: _*)
  }

  protected[cdi] def add(definition: Definition): Unit = {
    definitions += definition
  }
}
