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
import org.beangle.commons.lang.reflect.*
import org.beangle.commons.lang.{JVM, Strings}

import java.util as ju
import scala.quoted.*

object BindModule {
  /**
   * bind class with a name.
   */
  def bind(clazzesExpr: Expr[Seq[Class[_]]],
           binder: Expr[Binding], wiredEagerly: Expr[Boolean])
          (implicit quotes: Quotes): Expr[DefinitionBinder] = {
    import quotes.reflect.*
    '{
      ${ BeanInfoDigger.digInto(clazzesExpr, '{ BeanInfos.cache }) }
      ${ binder }.bind(${ clazzesExpr }: _*).wiredEagerly(${ wiredEagerly })
    }
  }

  /**
   * bind class with a name.
   */
  def bind[T: Type](beanName: Expr[String], clazz: Expr[Class[T]],
                    binder: Expr[Binding], wiredEagerly: Expr[Boolean])
                   (implicit quotes: Quotes): Expr[DefinitionBinder] = {
    import quotes.reflect.*
    '{
      ${ BeanInfoDigger.digInto(clazz, '{ BeanInfos.cache }) }
      ${ binder }.bind(${ beanName }, ${ clazz }).wiredEagerly(${ wiredEagerly })
    }
  }

  /**
   * bind class with a name.
   */
  def bean[T: Type](clazz: Expr[Class[T]],
                    binder: Expr[Binding], wiredEagerly: Expr[Boolean])
                   (implicit quotes: Quotes): Expr[Definition] = {
    import quotes.reflect.*
    '{
      ${ BeanInfoDigger.digInto(clazz, '{ BeanInfos.cache }) }
      ${ binder }.bind(${ binder }.newInnerBeanName(${ clazz }), ${ clazz }).head.wiredEagerly(${ wiredEagerly })
    }
  }

}

/**
 * Abstract BindModule class.
 * The subclass can writed in /META-INF/beangle/cdi.xml
 * using modules=com.your.class
 */
abstract class BindModule {

  protected var binder: Binding = _

  private var wiredEagerly: Boolean = _

  /**
   * Getter for the field <code>config</code>.
   */
  final def configure(binder: Binding): Unit = {
    this.binder = binder
    binding()
  }

  def wiredEagerly(newvalue: Boolean): Unit = {
    this.wiredEagerly = newvalue
  }

  /**
   * bind class.
   */
  protected inline def bind(inline classes: Class[_]*): DefinitionBinder = ${ BindModule.bind('classes, 'binder, 'wiredEagerly) }

  /**
   * Returns a reference definition based on Name;
   */
  protected final def ref(name: String): ReferenceValue = ReferenceValue(name)

  protected final def ref(clazz: Class[_]): ReferenceValue = ReferenceValue(clazz.getName)

  /**
   * Return new map entry
   */
  protected final def entry(key: Any, value: Any): (_, _) = Tuple2(key, value)

  /**
   * Generate a inner bean definition
   */
  protected inline def bean[T](clazz: Class[T]): Definition = ${ BindModule.bean('clazz, 'binder, 'wiredEagerly) }

  final def inject[T](clazz: Class[T]): Injection[T] = {
    Injection(clazz)
  }

  final def ? = InjectPlaceHolder

  final def $(s: String, defaultValue: String = null): PropertyPlaceHolder = {
    PropertyPlaceHolder(s, defaultValue)
  }

  /**
   * Generate a list property
   *
   * List singleton bean references with list(A.class,B.class) or list(ref("someBeanId"),C.class).<br>
   * List simple values with list("strValue1","strValue2")
   */
  protected final def list(datas: AnyRef*): List[_] = {
    datas.map {
      case clazz: Class[_] => buildInnerReference(clazz)
      case obj: Any => obj
    }.toList
  }

  /**
   * Generate a list reference property
   */
  protected final def listref(classes: Class[_]*): List[_] = {
    classes.map(clazz => ReferenceValue(clazz.getName)).toList
  }

  /**
   * Generate a set property
   *
   * List singleton bean references with set(A.class,B.class) or set(ref("someBeanId"),C.class).<br>
   * List simple values with set("strValue1","strValue2")
   */
  protected final def set(datas: AnyRef*): Set[_] = {
    datas.map {
      case clazz: Class[_] => buildInnerReference(clazz)
      case obj: Any => obj
    }.toSet
  }

  protected final def map(entries: (_, _)*): Map[_, _] = {
    entries.map {
      case (k, v) =>
        v match {
          case clazz: Class[_] => (k, buildInnerReference(clazz))
          case _ => (k, v)
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
  protected inline def bind[T](beanName: String, clazz: Class[T]): DefinitionBinder =
    ${ BindModule.bind('beanName, 'clazz, 'binder, 'wiredEagerly) }

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
    val profiles = System.getProperty(BindRegistry.ProfileProperty)
    val enabled = null != profiles && Strings.split(profiles, ",").toSet.contains("dev")
    enabled || JVM.isDebugMode
  }

  private def buildInnerReference(clazz: Class[_]): ReferenceValue = {
    val targetBean = binder.newInnerBeanName(clazz)
    binder.add(new Definition(targetBean, clazz, Scope.Singleton.name))
    ReferenceValue(targetBean)
  }
}
