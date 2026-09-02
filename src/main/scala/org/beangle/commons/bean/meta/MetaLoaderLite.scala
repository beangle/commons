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
import org.beangle.commons.bean.meta.MetaLoader.{Accessor, buildProperties, isFineMethod, processMethod, typeof}
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
  *  - Scala 属性经"setter 触发"识别：`var target` 的字节码是参数less 的 `target()` 与
  *    `target_$eq` 赋值器；setter 的出现证明同名参数less 方法是字段访问器，随即按 getter 注册
  *    （getter 支持 getX/isX/属性名三种形态）；只读的 Scala `val`/普通参数less `def`
  *    （无 setter）仍不识别——字节码层无法与 `size()` 式方法区分，且 lite 不扫描 declared 字段；
  *  - 无字段信息，isTransient 仅按 setter/主构造器推断；
  *  - 构造器无默认参数值（defaultValue = None），不反射伴生对象；
  *  - 对纯 JavaBean 类结果与 [[MetaLoader]] 一致。
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

    // getMethods 已含继承的 public 方法（含接口），无需手动遍历层级
    clazz.getMethods foreach { m =>
      processMethod(isCase, m, getters, setters, fields, Map.empty)
    }

    // Scala 属性补齐：第一遍只识别了 JavaBean 形态（getX/isX 与 setX/x_$eq）；对仅有 setter
    // 的属性，若存在同名参数less public 方法（Scala var 生成的字段访问器），即其 getter，注册之。
    val setterOnlyNames = setters.keySet -- getters.keySet
    if (setterOnlyNames.nonEmpty) {
      clazz.getMethods foreach { m =>
        if (m.getParameterCount == 0 && m.getReturnType != classOf[Unit] &&
          setterOnlyNames.contains(m.getName) && isFineMethod(isCase, m)) {
          getters.put(m.getName, Accessor(m, typeof(m.getReturnType, m.getGenericReturnType, Map.empty)))
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
