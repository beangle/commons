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

import org.beangle.commons.cdi.Binder.*
import org.beangle.commons.config.{Enviroment, PlaceHolder}
import org.beangle.commons.lang.reflect.*

import java.util as ju
import scala.quoted.*

/** Compile-time binding macros. */
object BindModule {

  /** Binds the given classes to the binder.
   *
   * @param clazzesExpr  the classes to bind
   * @param binder       the Binder
   * @param wiredEagerly whether to wire eagerly
   * @return the BatchBinder
   */
  def bind(clazzesExpr: Expr[Seq[Class[_]]],
           binder: Expr[Binder], wiredEagerly: Expr[Boolean])
          (implicit quotes: Quotes): Expr[BatchBinder] = {
    '{
      ${ BeanInfoDigger.digInto(clazzesExpr, '{ BeanInfos.cache }) }
      ${ binder }.bind(${ clazzesExpr }: _*).wiredEagerly(${ wiredEagerly })
    }
  }

  /** Binds the given class with the specified bean name.
   *
   * @param beanName     the bean name
   * @param clazz        the class to bind
   * @param binder       the Binder
   * @param wiredEagerly whether to wire eagerly
   * @return the BatchBinder
   */
  def bind[T: Type](beanName: Expr[String], clazz: Expr[Class[T]],
                    binder: Expr[Binder], wiredEagerly: Expr[Boolean])
                   (implicit quotes: Quotes): Expr[BatchBinder] = {
    '{
      ${ BeanInfoDigger.digInto(clazz, '{ BeanInfos.cache }) }
      ${ binder }.bind(${ beanName }, ${ clazz }).wiredEagerly(${ wiredEagerly })
    }
  }

  /** Creates a bean definition for the given class.
   *
   * @param clazz        the class
   * @param binder       the Binder
   * @param wiredEagerly whether to wire eagerly
   * @return the Definition
   */
  def bean[T: Type](clazz: Expr[Class[T]],
                    binder: Expr[Binder], wiredEagerly: Expr[Boolean])
                   (implicit quotes: Quotes): Expr[Definition] = {
    '{
      ${ BeanInfoDigger.digInto(clazz, '{ BeanInfos.cache }) }
      ${ binder }.bind(${ binder }.newInnerBeanName(${ clazz }), ${ clazz }).head.wiredEagerly(${ wiredEagerly })
    }
  }

}

/** Abstract CDI binding module. Subclasses can be registered in /META-INF/beangle/cdi.xml via modules=com.your.Class. */
abstract class BindModule {

  private var binder: Binder = _

  private var wiredEagerly: Boolean = _

  /** Configures this module with the given binder. */
  final def configure(binder: Binder): Unit = {
    this.binder = binder
    binding()
  }

  /** Sets whether beans are wired eagerly. */
  def wiredEagerly(newvalue: Boolean): Unit = {
    this.wiredEagerly = newvalue
  }

  /** Binds the given classes. */
  protected inline def bind(inline classes: Class[_]*): BatchBinder = ${ BindModule.bind('classes, 'binder, 'wiredEagerly) }

  /** Returns a reference to a bean by name. */
  protected final def ref(name: String): Reference = Reference(name)

  /** Returns an injection by class. */
  protected final def ref(clazz: Class[_]): Injection[_] = Injection(clazz)

  /** Creates a map entry. */
  protected final def entry(key: Any, value: Any): (_, _) = Tuple2(key, value)

  /** Creates an inner bean definition. */
  protected inline def bean[T](clazz: Class[T]): Definition = ${ BindModule.bean('clazz, 'binder, 'wiredEagerly) }

  protected final def inject[T](clazz: Class[T]): Injection[T] = {
    Injection(clazz)
  }

  protected final def ? = InjectPlaceHolder

  protected final def $(s: String): PlaceHolder = {
    if (PlaceHolder.hasVariable(s)) {
      PlaceHolder(s)
    } else {
      val v = Variable(s)
      PlaceHolder(PlaceHolder.Prefix + v.name + PlaceHolder.Suffix, Set(v))
    }
  }

  /** Builds a list property. Use list(A.class, B.class) or list(ref("id"), C.class) for beans;
   * list("a", "b") for simple values.
   *
   * @param datas the items (Class for bean ref, or value)
   * @return the list
   */
  protected final def list(datas: AnyRef*): List[_] = {
    datas.map {
      case clazz: Class[_] => buildInnerReference(clazz)
      case obj: Any => obj
    }.toList
  }

  /** Builds a list of bean references. */
  protected final def listref(classes: Class[_]*): List[_] = {
    classes.map(clazz => Injection(clazz)).toList
  }

  /** Builds a set property. Use set(A.class, B.class) for beans; set("a", "b") for values. */
  protected final def set(datas: AnyRef*): Set[_] = {
    datas.map {
      case clazz: Class[_] => buildInnerReference(clazz)
      case obj: Any => obj
    }.toSet
  }

  /** Builds a map from key-value entries. */
  protected final def map(entries: (_, _)*): Map[_, _] = {
    entries.map {
      case (k, v) =>
        v match {
          case clazz: Class[_] => (k, buildInnerReference(clazz))
          case _ => (k, v)
        }
    }.toMap
  }

  /** Builds Properties from key=value strings. */
  protected final def props(keyValuePairs: String*): ju.Properties = {
    val properties = new ju.Properties
    keyValuePairs foreach { pair =>
      val index = pair.indexOf('=')
      if (index > 0) properties.put(pair.substring(0, index), pair.substring(index + 1))
    }
    properties
  }

  /** Binds the class with the given bean name. */
  protected inline def bind[T](beanName: String, clazz: Class[T]): BatchBinder =
    ${ BindModule.bind('beanName, 'clazz, 'binder, 'wiredEagerly) }

  /** Binds a singleton instance with the given name. */
  protected final def bind(beanName: String, singleton: AnyRef): Singleton = {
    binder.bind(beanName, singleton)
  }

  /** Override to perform binding. */
  protected def binding(): Unit

  final def devEnabled: Boolean = {
    Enviroment.isDevMode
  }

  private def buildInnerReference(clazz: Class[_]): Reference = {
    val targetBean = binder.newInnerBeanName(clazz)
    binder.add(new Definition(targetBean, clazz, Scope.Singleton.name))
    Reference(targetBean)
  }

  /** Condition: class missing from classpath. */
  def missing(clazz: Class[_]): Condition = {
    Condition.missing(clazz)
  }

  /** Condition: system property exists (optionally with value). */
  def hasProperty(name: String, value: String = ""): Condition = {
    Condition.hasProperty(name, value)
  }

  /** Condition: resource exists at path. */
  def hasResource(path: String): Condition = {
    Condition.hasResource(path)
  }
}
