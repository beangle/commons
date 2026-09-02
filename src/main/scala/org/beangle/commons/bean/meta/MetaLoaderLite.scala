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

/** 轻量运行时反射加载器：仅读取 public 构造器与 public 方法。
  *
  * [[MetaLoader]] 的全量反射需要遍历继承链的 declared 字段/方法、反射伴生对象取默认参数，
  * 在 GraalVM native-image 下要求注册 `allDeclaredFields` + `queryAllDeclaredMethods`（递归）。
  * 本加载器只使用 `getMethods`/`getConstructors`（public，天然含继承的 public 成员），
  * 对应注册策略 [[org.beangle.commons.aot.AotPolicy.default]]
  * （`allPublicMethods` + `allPublicConstructors`），显著降低 native 下的反射配置要求。
  *
  * 与 [[MetaLoader]] 的行为差异：
  *  - 只读属性识别更宽松：public 参数less 非 Unit 方法都注册为只读 getter。`getX`/`isX`
  *    前缀转 JavaBean 属性名（`getObjectType`→`objectType`、`isEmpty`→`empty`），其余保留
  *    方法名（`size`/`pageIndex`/`items` 原样作为属性）；bridge 方法放行——泛型集合继承的
  *    `isEmpty`/`isTraversableAgain` 在子类字节码里以 bridge 呈现；
  *  - setter 仍限 JavaBean 形态：`setX` 与 Scala 赋值器 `x_$eq`/`x_=`，纯 write-only
  *    （无同名 getter）不成属性；
  *  - 无字段信息，isTransient 仅按 setter/主构造器推断；
  *  - 构造器无默认参数值（defaultValue = None），不反射伴生对象；
  *  - 对纯 JavaBean 类结果与 [[MetaLoader]] 一致；Scala 类上 lite 比 MetaLoader 多识别
  *    无字段的参数less 方法（如 `size`/`length`）。
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

    // getMethods 已含继承的 public 方法（含接口），无需手动遍历层级。
    // 访问器判定不沿用 MetaLoader.processMethod（其要求 JavaBean getter 或字段名回退且排除 bridge）：
    //  - 任何 public 参数less 非 Unit 方法都视为只读 getter；
    //  - setter 只认 JavaBean 形态：setX、x_$eq、x_=；
    //  - bridge 放行，否则泛型集合继承的 isEmpty 等进不来；
    //  - 同名属性冲突时 JavaBean 命名（getX/isX）优先，保证 isEmpty 胜过 empty() 这类默认方法。
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
