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
package org.beangle.commons.cdi.bind

import java.{ util => ju }
import org.beangle.commons.cdi.Scope
import org.beangle.commons.cdi.bind.Binder.{ Definition, DefinitionBinder, ReferenceValue, Injection, InjectPlaceHolder, PropertyPlaceHolder }
import org.beangle.commons.lang.Strings

/**
 * Module interface.
 */
trait Module {

  /**
   * binding process
   */
  def configure(binder: Binder): Unit
}
object Module {
  final val profileProperty = "cdi.profiles"
}
/**
 * Abstract AbstractBindModule class.
 * The subclass can writed in /META-INF/beangle/cdi.xml
 * using modules=com.your.class
 */
abstract class AbstractBindModule extends Module {

  protected var binder: Binder = _

  /**
   * Getter for the field <code>config</code>.
   */
  override final def configure(binder: Binder): Unit = {
    this.binder = binder
    binding()
  }

  /**
   * bind class.
   */
  protected final def bind(classes: Class[_]*): DefinitionBinder = binder.bind(classes: _*)

  /**
   * Returns a reference definition based on Name;
   */
  protected final def ref(name: String): ReferenceValue = new ReferenceValue(name)

  protected final def ref(clazz: Class[_]): ReferenceValue = new ReferenceValue(clazz.getName)

  /**
   * Return new map entry
   */
  protected final def entry(key: Any, value: Any): Tuple2[_, _] = Tuple2(key, value)

  /**
   * Generate a inner bean definition
   */
  protected final def bean(clazz: Class[_]): Definition = {
    bind(binder.newInnerBeanName(clazz), clazz).head
  }

  final def inject[T](clazz: Class[T]): Injection[T] = {
    Injection(clazz)
  }

  final def ? = InjectPlaceHolder

  final def $(s: String, defaultValue: String = null) = PropertyPlaceHolder(s, defaultValue)
  /**
   * Generate a list property
   *
   * List singleton bean references with list(A.class,B.class) or list(ref("someBeanId"),C.class).<br>
   * List simple values with list("strValue1","strValue2")
   */
  protected final def list(datas: AnyRef*): List[_] = {
    datas.map { obj =>
      obj match {
        case clazz: Class[_] => buildInnerReference(clazz)
        case _               => obj
      }
    }.toList
  }

  /**
   * Generate a list reference property
   */
  protected final def listref(classes: Class[_]*): List[_] = classes.map(clazz => new ReferenceValue(clazz.getName)).toList

  /**
   * Generate a set property
   *
   * List singleton bean references with set(A.class,B.class) or set(ref("someBeanId"),C.class).<br>
   * List simple values with set("strValue1","strValue2")
   */
  protected final def set(datas: AnyRef*): Set[_] = {
    datas.map { obj =>
      obj match {
        case clazz: Class[_] => buildInnerReference(clazz)
        case _               => obj
      }
    }.toSet
  }

  protected final def map(entries: Tuple2[_, _]*): Map[_, _] = {
    entries.map {
      case (k, v) =>
        v match {
          case clazz: Class[_] => (k, buildInnerReference(clazz))
          case _               => (k, v)
        }
    }.toMap
  }

  protected final def props(keyValuePairs: String*): ju.Properties = {
    val properties = new ju.Properties
    keyValuePairs foreach { pair =>
      val index = pair.indexOf('=')
      if (index > 0) properties.put(pair.substring(0, index), pair.substring(index + 1))
    }
    properties
  }

  /**
   * bind class with a name.
   */
  protected final def bind(beanName: String, clazz: Class[_]): DefinitionBinder = {
    binder.bind(beanName, clazz)
  }

  /**
   * bind singleton with a name.
   */
  protected final def bind(beanName: String, singleton: AnyRef): Unit = {
    binder.bind(beanName, singleton)
  }

  /**
   * binding.
   */
  protected def binding(): Unit

  final def devEnabled: Boolean = {
    val profiles = System.getProperty(Module.profileProperty)
    null != profiles && Strings.split(profiles, ",").toSet.contains("dev")
  }

  private def buildInnerReference(clazz: Class[_]): ReferenceValue = {
    val targetBean = binder.newInnerBeanName(clazz)
    binder.add(new Definition(targetBean, clazz, Scope.Singleton.toString))
    new ReferenceValue(targetBean)
  }
}
