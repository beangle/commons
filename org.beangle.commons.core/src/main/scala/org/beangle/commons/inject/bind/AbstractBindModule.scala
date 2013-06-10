/*
 * Beangle, Agile Java/Scala Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2013, Beangle Software.
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

import java.util.Properties
import org.beangle.commons.inject.Scope
import org.beangle.commons.inject.bind.BeanConfig.Definition
import org.beangle.commons.inject.bind.BeanConfig.DefinitionBinder
import org.beangle.commons.inject.bind.BeanConfig.ReferenceValue
import org.beangle.commons.lang.Assert
import scala.collection.mutable.ListBuffer

/**
 * <p>
 * Abstract AbstractBindModule class.
 * </p>
 *
 * @author chaostone
 * @version $Id: $
 */
abstract class AbstractBindModule extends BindModule {

  protected var config: BeanConfig = _

  /**
   * <p>
   * Getter for the field <code>config</code>.
   * </p>
   *
   * @return a {@link org.beangle.commons.inject.bind.BeanConfig} object.
   */
  def getConfig(): BeanConfig = {
    if (null == config) {
      config = new BeanConfig(getClass.getName)
      doBinding()
    }
    config
  }

  /**
   * <p>
   * bind.
   * </p>
   *
   * @param classes a {@link java.lang.Class} object.
   * @return a {@link org.beangle.commons.inject.bind.BeanConfig.DefinitionBinder} object.
   */
  protected def bind(classes: Class[_]*): DefinitionBinder = config.bind(classes: _*)

  /**
   * Returns a reference definition based on Name;
   *
   * @param name
   */
  protected def ref(name: String): ReferenceValue = new ReferenceValue(name)

  protected def ref(clazz: Class[_]): ReferenceValue = new ReferenceValue(clazz.getName)

  /**
   * Return new map entry
   *
   * @param key
   * @param value
   */
  protected def entry(key: Any, value: Any): Pair[_, _] = Pair.apply(key, value)

  /**
   * Generate a inner bean definition
   *
   * @param clazz
   */
  protected def bean(clazz: Class[_]): Definition = {
    val definition = new Definition(clazz.getName, clazz, Scope.Singleton.toString)
    definition.beanName = clazz.getName + "#" + Math.abs(System.identityHashCode(definition))
    definition
  }

  /**
   * Generate a list property
   * <p>
   * List singleton bean references with list(A.class,B.class) or list(ref("someBeanId"),C.class).<br>
   * List simple values with list("strValue1","strValue2")
   *
   * @param datas
   */
  protected def list(datas: AnyRef*): List[_] = {
    val items = new ListBuffer[Any]
    for (obj <- datas) {
      if (obj.isInstanceOf[Class[_]]) {
        items += buildInnerReference(obj.asInstanceOf[Class[_]])
      } else {
        items += obj
      }
    }
    items.toList
  }

  /**
   * Generate a list reference property
   * <p>
   *
   * @param classes
   */
  protected def listref(classes: Class[_]*): List[_] = {
    val items = new ListBuffer[Any]
    for (clazz <- classes) {
      items += new ReferenceValue(clazz.getName)
    }
    items.toList
  }

  /**
   * Generate a set property
   * <p>
   * List singleton bean references with set(A.class,B.class) or set(ref("someBeanId"),C.class).<br>
   * List simple values with set("strValue1","strValue2")
   *
   * @param datas
   */
  protected def set(datas: AnyRef*): Set[_] = {
    val items = new collection.mutable.HashSet[Any]
    for (obj <- datas) {
      if (obj.isInstanceOf[Class[_]]) {
        items += buildInnerReference(obj.asInstanceOf[Class[_]])
      } else {
        items += obj
      }
    }
    items.toSet
  }

  private def buildInnerReference(clazz: Class[_]): ReferenceValue = {
    val targetBean = config.innerBeanName(clazz)
    config.add(new Definition(targetBean, clazz, Scope.Singleton.toString))
    new ReferenceValue(targetBean)
  }

  protected def map(entries: Pair[_, _]*): Map[_, _] = {
    val items = new collection.mutable.HashMap[Any, Any]
    for ((k, v) <- entries) {
      if (v.isInstanceOf[Class[_]]) items.put(k, buildInnerReference(v.asInstanceOf[Class[_]]))
      else items.put(k, v)
    }
    items.toMap
  }

  protected def props(keyValuePairs: String*): Properties = {
    val properties = new Properties()
    for (pair <- keyValuePairs) {
      val index = pair.indexOf('=')
      index > 0
      properties.put(pair.substring(0, index), pair.substring(index + 1))
    }
    properties
  }

  /**
   * <p>
   * bind.
   * </p>
   *
   * @param beanName a {@link java.lang.String} object.
   * @param clazz a {@link java.lang.Class} object.
   * @return a {@link org.beangle.commons.inject.bind.BeanConfig.DefinitionBinder} object.
   */
  protected def bind(beanName: String, clazz: Class[_]): DefinitionBinder = config.bind(beanName, clazz)

  /**
   * <p>
   * doBinding.
   * </p>
   */
  protected def doBinding(): Unit

  /**
   * <p>
   * getObjectType.
   * </p>
   *
   * @return a {@link java.lang.Class} object.
   */
  def getObjectType(): Class[_] = classOf[BeanConfig]

  /**
   * <p>
   * isSingleton.
   * </p>
   *
   * @return a boolean.
   */
  def isSingleton(): Boolean = true
}
