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

package org.beangle.commons.bean.meta

import org.beangle.commons.bean.meta.MetaModel.{BeanMeta, Ctor, Param}
import org.beangle.commons.bean.meta.MetaLoader.{Accessor, buildProperties, getPropertyName, isFineMethod, typeof}
import org.beangle.commons.lang.reflect.TypeInfo

import java.lang.reflect.Field
import scala.collection.mutable

/** 轻量反射加载器：仅用 public 构造器/方法，对应 [[org.beangle.commons.aot.AotPolicy.default]]，
  * 显著降低 GraalVM native 的反射配置（[[MetaLoader]] 需 declared 成员递归注册）。
  *
  * 与 [[MetaLoader]] 的差异：
  *  - getter 宽松：public 参数less 非 Unit 方法均视为只读属性，`getX`/`isX` 转属性名，
  *    其余保留原名（`size`/`pageIndex`），bridge 放行（`isEmpty`→`empty`）；
  *  - setter 仅认 `setX`/`x_$eq`/`x_=`，write-only 不成属性；
  *  - 无字段信息；构造器无默认参数值。
  */
object MetaLoaderLite {

  /** True when a class can be reflected into BeanMeta: application classes only. */
  def supports(clazz: Class[_]): Boolean = MetaLoader.supports(clazz)

  /** Reflects a class into BeanMeta via public constructors/methods only. */
  def load(clazz: Class[_]): BeanMeta = {
    if (!supports(clazz)) throw new RuntimeException("Cannot reflect class: " + clazz.getName)

    val isCase = TypeInfo.isCaseClass(clazz)
    val getters = new mutable.HashMap[String, Accessor]
    val setters = new mutable.HashMap[String, Accessor]
    val fields = new mutable.HashMap[String, Field]

    // getMethods 含继承的 public 方法；参数less 方法（含 bridge）即 getter，
    // JavaBean 命名（getX/isX）优先，setter 仅认 setX/x_$eq/x_=。
    clazz.getMethods foreach { m =>
      if (isFineMethod(isCase, m, allowBridge = true)) {
        val paramCount = m.getParameterCount
        if (paramCount == 0 && m.getReturnType != classOf[Unit]) {
          val name = getPropertyName(m.getName, getter = true)
          val javaBean = name != m.getName
          if (!getters.contains(name) || javaBean)
            getters.put(name, Accessor(m, typeof(m.getReturnType, m.getGenericReturnType, Map.empty)))
        } else if (paramCount == 1) {
          val name = getPropertyName(m.getName, getter = false)
          if (null != name && !name.contains("$"))
            setters.put(name, Accessor(m, typeof(m.getParameterTypes()(0), m.getGenericParameterTypes()(0), Map.empty)))
        }
      }
    }

    val ctors = clazz.getConstructors.toSeq.map { ctor =>
      val params = ctor.getParameters.toSeq.map { p =>
        Param(p.getName, typeof(p.getType, p.getParameterizedType, Map.empty), None)
      }
      Ctor(params)
    }
    val primaryCtorParamNames = ctors.headOption.map(_.parameters.map(_.name).toSet).getOrElse(Set.empty)

    BeanMeta(clazz, buildProperties(getters, setters, fields, primaryCtorParamNames, isCase), ctors)
  }
}
