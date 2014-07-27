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

import java.{ util => ju }

import org.beangle.commons.inject.Scope
import org.beangle.commons.inject.bind.Binder.{ Definition, DefinitionBinder, ReferenceValue, Injection, InjectPlaceHolder, PropertyPlaceHolder }

/**
 * Module interface.
 */
trait Module {

  /**
   * binding process
   */
  def configure(binder: Binder): Unit
}

/**
 * Abstract AbstractBindModule class.
 */
abstract class AbstractBindModule extends Module {

  protected var binder: Binder = _

  /**
   * Getter for the field <code>config</code>.
   */
  override def configure(binder: Binder): Unit = {
    this.binder = binder
    binding()
  }

  /**
   * bind class.
   */
  protected def bind(classes: Class[_]*): DefinitionBinder = binder.bind(classes: _*)

  /**
   * Returns a reference definition based on Name;
   */
  protected def ref(name: String): ReferenceValue = new ReferenceValue(name)

  protected def ref(clazz: Class[_]): ReferenceValue = new ReferenceValue(clazz.getName)

  /**
   * Return new map entry
   */
  protected def entry(key: Any, value: Any): Tuple2[_, _] = Tuple2(key, value)

  /**
   * Generate a inner bean definition
   */
  protected def bean(clazz: Class[_]): Definition = {
    val defn = new Definition(clazz.getName, clazz, Scope.Singleton.toString)
    defn.beanName = clazz.getName + "#" + Math.abs(System.identityHashCode(defn))
    defn
  }

  def inject[T](clazz: Class[T]): Injection[T] = {
    Injection(clazz)
  }

  def ? = InjectPlaceHolder

  def $(s: String) = PropertyPlaceHolder(s)
  /**
   * Generate a list property
   *
   * List singleton bean references with list(A.class,B.class) or list(ref("someBeanId"),C.class).<br>
   * List simple values with list("strValue1","strValue2")
   */
  protected def list(datas: AnyRef*): List[_] = {
    datas.map { obj =>
      obj match {
        case clazz: Class[_] => buildInnerReference(clazz)
        case _ => obj
      }
    }.toList
  }

  /**
   * Generate a list reference property
   */
  protected def listref(classes: Class[_]*): List[_] = classes.map(clazz => new ReferenceValue(clazz.getName)).toList

  /**
   * Generate a set property
   *
   * List singleton bean references with set(A.class,B.class) or set(ref("someBeanId"),C.class).<br>
   * List simple values with set("strValue1","strValue2")
   */
  protected def set(datas: AnyRef*): Set[_] = {
    datas.map { obj =>
      obj match {
        case clazz: Class[_] => buildInnerReference(clazz)
        case _ => obj
      }
    }.toSet
  }

  protected def map(entries: Tuple2[_, _]*): Map[_, _] = {
    entries.map {
      case (k, v) =>
        v match {
          case clazz: Class[_] => (k, buildInnerReference(clazz))
          case _ => (k, v)
        }
    }.toMap
  }

  protected def props(keyValuePairs: String*): ju.Properties = {
    val properties = new ju.Properties
    keyValuePairs foreach { pair =>
      val index = pair.indexOf('=')
      if (index > 0) properties.put(pair.substring(0, index), pair.substring(index + 1))
    }
    properties
  }

  /**
   * bind.
   */
  protected def bind(beanName: String, clazz: Class[_]): DefinitionBinder = {
    binder.bind(beanName, clazz)
  }

  /**
   * binding.
   */
  protected def binding(): Unit

  private def buildInnerReference(clazz: Class[_]): ReferenceValue = {
    val targetBean = binder.innerName(clazz)
    binder.add(new Definition(targetBean, clazz, Scope.Singleton.toString))
    new ReferenceValue(targetBean)
  }
}
