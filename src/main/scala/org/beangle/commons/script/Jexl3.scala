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

package org.beangle.commons.script

import org.apache.commons.jexl3.internal.introspection.Uberspect
import org.apache.commons.jexl3.introspection.{JexlPermissions, JexlPropertyGet, JexlUberspect}
import org.apache.commons.jexl3.scripting.JexlScriptEngine
import org.apache.commons.jexl3.{JexlBuilder, JexlEngine, JexlException}
import org.beangle.commons.lang.Options
import org.beangle.commons.lang.reflect.BeanInfos

import java.lang.reflect.{InvocationTargetException, Method}
import java.util as ju
import scala.jdk.javaapi.CollectionConverters.asJava

/** Jexl3 expression evaluator factory. */
object Jexl3 {

  /** Creates a Jexl3 expression evaluator with Scala property support. */
  def newEvaluator(): ExpressionEvaluator = {
    val jexlBuilder = new JexlBuilder().cache(512).strict(true).silent(false)
    val uberspect = new ScalaJexlUberspect(JexlUberspect.JEXL_STRATEGY, JexlPermissions.UNRESTRICTED)
    jexlBuilder.uberspect(uberspect)
    JexlScriptEngine.setInstance(jexlBuilder.create())
    ExpressionEvaluator.jsr223("jexl3")
  }

  private class SimplePropertyGet(val clazz: Class[_], val method: Method, val property: String) extends JexlPropertyGet {

    override def invoke(obj: Any): AnyRef = method.invoke(obj)

    override def isCacheable: Boolean = false

    override def tryFailed(rval: Any): Boolean = rval == JexlEngine.TRY_FAILED

    override def tryInvoke(obj: Any, key: Any): AnyRef = {
      if (obj != null && property == key && clazz == obj.getClass) {
        try {
          method.invoke(obj)
        } catch {
          case xill@(_: IllegalAccessException | _: IllegalArgumentException) => JexlEngine.TRY_FAILED // fail
          case xinvoke: InvocationTargetException => throw JexlException.tryFailed(xinvoke) // throw
        }
      } else {
        JexlEngine.TRY_FAILED
      }
    }
  }

  private object NonePropertyGet extends JexlPropertyGet {
    override def invoke(obj: Any): AnyRef = null

    override def isCacheable: Boolean = true

    override def tryFailed(rval: Any): Boolean = false

    override def tryInvoke(obj: Any, key: Any): AnyRef = null
  }

  private class PropertyGetAdapter(get: JexlPropertyGet) extends JexlPropertyGet {
    override def invoke(obj: Any): AnyRef = {
      unwrap(get.invoke(obj))
    }

    private def unwrap(v: Any): AnyRef = {
      val value = Options.unwrap(v).asInstanceOf[AnyRef]
      value match
        case null => null
        case map: collection.Map[_, _] => asJava(map)
        case seq: collection.Seq[_] => asJava(seq)
        case ib: collection.Iterable[_] => asJava(ib)
        case ir: collection.Iterator[_] => asJava(ir)
        case _ => value
    }

    override def isCacheable: Boolean = get.isCacheable

    override def tryFailed(rval: Any): Boolean = {
      get.tryFailed(rval)
    }

    override def tryInvoke(obj: Any, key: Any): AnyRef = {
      val rs = get.tryInvoke(obj, key)
      if rs == JexlEngine.TRY_FAILED then JexlEngine.TRY_FAILED else unwrap(rs)
    }
  }

  class ScalaJexlUberspect(sty: JexlUberspect.ResolverStrategy, perms: JexlPermissions) extends Uberspect(null, sty, perms) {

    override def getPropertyGet(resolvers: ju.List[JexlUberspect.PropertyResolver], obj: AnyRef, identifier: AnyRef): JexlPropertyGet = {
      val get = super.getPropertyGet(resolvers, obj, identifier)
      if (null == get) {
        obj match
          case null => NonePropertyGet
          case None => NonePropertyGet
          case Some(v) => buildPropertyGet(v.asInstanceOf[AnyRef], identifier.toString)
          case _ => buildPropertyGet(obj, identifier.toString)
      } else {
        new PropertyGetAdapter(get)
      }
    }

    private def buildPropertyGet(obj: AnyRef, property: String): JexlPropertyGet = {
      val clazz = obj.getClass
      if (BeanInfos.cached(clazz)) {
        val info = BeanInfos.get(clazz)
        info.getGetter(property) match
          case Some(m) => new PropertyGetAdapter(new SimplePropertyGet(clazz, m, property))
          case None => null
      } else {
        null
      }
    }
  }
}
