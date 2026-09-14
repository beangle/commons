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
import org.beangle.commons.bean.meta.MetaLoader.{Accessor, annotatedPropertyName, buildProperties, getPropertyName, isExplicitProperty, isFineMethod, isJavaBeanGetter, isLibraryDeclared, typeof}
import org.beangle.commons.lang.reflect.TypeInfo

import java.lang.reflect.{Field, Method}
import scala.collection.mutable

/** 轻量反射加载器：仅用 public 构造器/方法，对应 [[org.beangle.commons.aot.AotPolicy.default]]，
  * 显著降低 GraalVM native 的反射配置（[[MetaLoader]] 需 declared 成员递归注册）。
  *
  * 与 [[MetaLoader]] 的差异：
  *  - getter 宽松：public 参数less 非 Unit 方法均视为只读属性，`getX`/`isX` 转属性名，
  *    其余保留原名（`pageIndex`），bridge 放行（`isEmpty`→`empty`）；
  *    `@property` 标注的零参方法按注解命名（value 为空取方法名），与 [[MetaLoader]] 一致；
  *  - 标准库收窄：scala./java. 声明的方法与 Scala 3 mixin forwarder 只在符合
  *    JavaBean getter 约定（getX/isX）或显式 `@property` 时才算属性，避免把集合 API
  *    （head/tail/size/seq/toList...）注册成 Bean 属性；setter 不创建属性，
  *    且 setX 本就是 JavaBean 约定，因此不受该收窄限制（保持与 [[MetaLoader]] 的读写器一致）；
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
    // JavaBean 命名（getX/isX）优先，setter 仅认 setX/x_$eq/x_=；
    // 标准库方法与 mixin forwarder 交给 acceptsInherited 收窄。
    clazz.getMethods foreach { m =>
      if (acceptsInherited(m) && (isFineMethod(isCase, m, allowBridge = true) || isExplicitProperty(m))) {
        val paramCount = m.getParameterCount
        if (paramCount == 0 && m.getReturnType != classOf[Unit]) {
          val annotated = annotatedPropertyName(m)
          val name = annotated.getOrElse(getPropertyName(m.getName, getter = true))
          // 显式注解与 JavaBean 命名（getX/isX）均可覆盖同名的弱 getter
          if (!getters.contains(name) || annotated.isDefined || name != m.getName)
            getters.put(name, Accessor(m, typeof(m.getReturnType, m.getGenericReturnType, Map.empty)))
        } else if (paramCount == 1) {
          // 注解只作用于零参方法，setter 仍按 setX/x_$eq/x_= 规则识别
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

  /** 标准库方法与 Scala 3 生成的 mixin forwarder 只在符合 JavaBean getter 约定（或显式 @property）
    * 时才算属性，避免把集合 API（head/tail/size/seq/toList...）注册成 Bean 属性。
    * setter 不产生新属性，只回填已有 getter 的写访问器，因此放行以保持与 [[MetaLoader]] 一致。
    *
    * Scala 3 会为继承自 trait 的方法在本类生成 bridge 形式的 forwarder（如 Seq 的 head/size），
    * 其 declaringClass 是本类，因此必须单独按 bridge 判定。 */
  private def acceptsInherited(method: Method): Boolean = {
    if (method.getParameterCount == 1) true
    else if (!method.isBridge && !isLibraryDeclared(method)) true
    else isJavaBeanGetter(method) || annotatedPropertyName(method).isDefined
  }
}
