/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2016, Beangle Software.
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
package org.beangle.commons.cdi.spring.config

import org.beangle.commons.cdi.bind.BindRegistry
import org.beangle.commons.lang.ClassLoaders
import org.beangle.commons.lang.reflect.Reflections
import org.beangle.commons.logging.Logging
import org.springframework.beans.factory.FactoryBean
import org.springframework.beans.factory.config.{ BeanDefinition, BeanDefinitionHolder, RuntimeBeanReference }
import org.springframework.beans.factory.support.{ AbstractBeanDefinition, AbstractBeanFactory, BeanDefinitionRegistry }
import org.beangle.commons.bean.Factory
import org.springframework.beans.factory.HierarchicalBeanFactory
import org.springframework.beans.factory.config.SingletonBeanRegistry

object SpringBindRegistry {

  def getBeanClass(registry: BeanDefinitionRegistry, name: String): Class[_] = {
    val bd = registry.getBeanDefinition(name)
    var clazz: Class[_] = getBeanClass(bd)
    if (null == clazz) {
      var currDef = bd
      while (null == clazz && null != currDef && null != currDef.getParentName) {
        val parentDef = registry.getBeanDefinition(bd.getParentName)
        clazz = getBeanClass(parentDef)
        currDef = parentDef
      }
    }
    if (null == clazz) {
      val factoryBeanName = bd.getFactoryBeanName
      val factoryMethodName = bd.getFactoryMethodName
      if (null != factoryBeanName && null != factoryMethodName) {
        var factoryClass = getBeanClass(registry, factoryBeanName)
        if (classOf[FactoryBean[_]].isAssignableFrom(factoryClass)) {
          factoryClass = factoryClass.newInstance().asInstanceOf[FactoryBean[_]].getObjectType()
        } else if (classOf[Factory[_]].isAssignableFrom(factoryClass)) {
          factoryClass = Reflections.getGenericParamType(factoryClass, classOf[Factory[_]]).values.head
        }
        clazz = factoryClass.getMethod(factoryMethodName).getReturnType
      }
    }
    clazz
  }

  def getBeanClass(bd: BeanDefinition): Class[_] = {
    var clazz: Class[_] = null
    if (bd.isInstanceOf[AbstractBeanDefinition]) {
      val abd = bd.asInstanceOf[AbstractBeanDefinition]
      if (abd.hasBeanClass()) clazz = abd.getBeanClass()
    }
    if (null == clazz) {
      clazz = if (null != bd.getBeanClassName()) ClassLoaders.load(bd.getBeanClassName()) else null
    }
    clazz
  }

}
/**
 * SpringBindRegistry class.
 *
 * @author chaostone
 */
class SpringBindRegistry(val registry: BeanDefinitionRegistry) extends BindRegistry with Logging {

  private val nameTypes = new collection.mutable.HashMap[String, Class[_]]

  private val typeNames = new collection.mutable.HashMap[Class[_], List[String]]

  private val primaries = new collection.mutable.HashSet[String]

  registerExists()

  def beanNames: Set[String] = nameTypes.keySet.toSet

  /**
   * Register exists spring bean definition in  PARENT and current context.
   */
  private def registerExists() {
    registry match {
      case hfactory: HierarchicalBeanFactory =>
        hfactory.getParentBeanFactory match {
          case p: BeanDefinitionRegistry => registerDefinitions(p)
          case _                         =>
        }
      case _ =>
    }
    registerDefinitions(registry)
    logger.debug(s"Find ${beanNames.size} beans")
  }

  private def registerDefinitions(registry: BeanDefinitionRegistry): Unit = {
    //register singletons
    val singletonRegistry = registry.asInstanceOf[SingletonBeanRegistry]
    singletonRegistry.getSingletonNames foreach { singtonName =>
      nameTypes.put(singtonName, singletonRegistry.getSingleton(singtonName).getClass)
    }

    import SpringBindRegistry._
    //register definitions
    for (name <- registry.getBeanDefinitionNames()) {
      val bd = registry.getBeanDefinition(name)
      if (bd.isPrimary) primaries += name
      val beanClass = if (bd.isAbstract()) null else getBeanClass(registry, name)
      if (null != beanClass) {
        try {
          if (classOf[FactoryBean[_]].isAssignableFrom(beanClass)) {
            nameTypes.put("&" + name, beanClass)
            if (bd.isPrimary) primaries += ("&" + name)
            var objectClass: Class[_] = null
            val objectTypePV = bd.getPropertyValues().getPropertyValue("objectType")
            if (null != objectTypePV) {
              objectClass = objectTypePV.getValue match {
                case clazz: Class[_]   => clazz
                case className: String => ClassLoaders.load(className)
              }
            } else {
              objectClass = bd.getPropertyValues().getPropertyValue("target") match {
                case null =>
                  try {
                    beanClass.newInstance().asInstanceOf[FactoryBean[_]].getObjectType
                  } catch {
                    case e: Throwable => null
                  }
                case pv =>
                  pv.getValue match {
                    case bdh: BeanDefinitionHolder => ClassLoaders.load(bdh.getBeanDefinition.getBeanClassName)
                    case rbr: RuntimeBeanReference => getBeanClass(registry, rbr.getBeanName)
                    case _                         => null
                  }
              }
            }
            if (null == objectClass) throw new RuntimeException("Cannot guess object type of " + bd)
            else nameTypes.put(name, objectClass)
          } else if (classOf[Factory[_]].isAssignableFrom(beanClass)) {
            nameTypes.put("&" + name, beanClass)
            if (bd.isPrimary) primaries += ("&" + name)
            val objectClass = Reflections.getGenericParamType(beanClass, classOf[Factory[_]]).values.head.asInstanceOf[Class[_]]
            nameTypes.put(name, objectClass)
          } else {
            nameTypes.put(name, beanClass)
          }
        } catch {
          case e: Exception => logger.error("class not found", e)
        }
      }
    }
  }

  /**
   * Get bean name list according given type
   */
  def getBeanNames(clazz: Class[_]): List[String] = {
    if (typeNames.contains(clazz)) return typeNames(clazz)
    val names = for ((name, ty) <- nameTypes if (clazz.isAssignableFrom(ty) && !name.contains("#"))) yield name
    val rs = names.toList
    typeNames.put(clazz, rs)
    rs
  }

  def getBeanType(beanName: String): Class[_] = {
    nameTypes(beanName)
  }

  def contains(beanName: String): Boolean = {
    nameTypes.contains(beanName)
  }

  override def register(name: String, clazz: Class[_]): Unit = {
    require(null != name, "class'name is null")
    nameTypes.put(name, clazz)
  }

  def register(name: String, obj: AnyRef): Unit = {
    nameTypes.put(name, obj.getClass)
    registry.asInstanceOf[SingletonBeanRegistry].registerSingleton(name, obj)
  }
  /**
   * register bean definition
   */
  override def register[T](name: String, clazz: Class[_], definition: T): Unit = {
    require(null != name, "class'name is null")
    var bd = definition.asInstanceOf[BeanDefinition]
    // 注册bean的name和别名
    if (registry.containsBeanDefinition(name)) registry.removeBeanDefinition(name)
    registry.registerBeanDefinition(name, bd)
    if (null == clazz) {
      if (!bd.isAbstract) throw new RuntimeException("Concrete bean should has class.")
    } else {
      // for list(a.class,b.class) binding usage
      val alias = clazz.getName
      if (bd.isSingleton && !name.startsWith("&") && !bd.isAbstract && !name.equals(alias) && !registry.isBeanNameInUse(alias)) {
        registry.registerAlias(name, alias)
      }
      if (bd.isPrimary) primaries += name
      nameTypes.put(name, clazz)
    }
  }

  def setPrimary[T](beanName: String, isPrimary: Boolean, definition: T): Unit = {
    definition.asInstanceOf[BeanDefinition].setPrimary(isPrimary)
    if (isPrimary) primaries.add(beanName)
    else primaries.remove(beanName)
  }

  def isPrimary(name: String): Boolean = {
    primaries.contains(name)
  }
}
