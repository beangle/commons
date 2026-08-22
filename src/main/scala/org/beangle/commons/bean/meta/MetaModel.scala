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

import org.beangle.commons.lang.reflect.TypeInfo
import org.beangle.commons.lang.reflect.TypeInfo.OptionType

import scala.collection.immutable.ArraySeq
import scala.collection.mutable

/** MetaModel：单个类的元数据模型（properties / ctors / methods）的纯数据表示。
  *
  * 由 [[MetaCodec.parse]] 产出（v2 二进制解码，不含任何反射解析出的 Method）；
  * 从该结构构造 BeanInfo 的方法暂不提供——留待未来经 BeanInfo.Builder 反向构造
  * （addField/addCtor + build() 自行从 getMethods 接线 getter/setter）。
  */
object MetaModel {

  /** 类的元数据：属性/构造器/方法记录。 */
  case class ClassMeta(clazz: Class[_], properties: Seq[Property], ctors: Seq[Ctor], methods: Seq[Method])

  /** 属性记录：纯声明（名字 + 类型 + 标志），不含任何方法信息。
    * 访问器不在二进制中——构造 BeanInfo 时按命名约定（getX/setX/x_$eq）发现。
    * Option 属性：typeinfo 存**元素类型**（压平后无 option 包装），isOptional=true；
    * 构造时 `if isOptional then OptionType(typeinfo) else typeinfo`。
    */
  case class Property(name: String, typeinfo: TypeInfo, isTransient: Boolean, isOptional: Boolean)

  /** 构造器记录。 */
  case class Ctor(parameters: Seq[Param])

  /** 构造器参数记录。 */
  case class Param(name: String, typeinfo: TypeInfo, defaultValue: Option[Any])

  /** 方法记录：只保留方法名与参数擦除类名（池字符串），不解析 Method。 */
  case class Method(name: String, paramTypes: Seq[String])

  /** 构造器参数载体（[[Builder.addCtor]] 用）。 */
  class ParamHolder(name: String, typeinfo: Any, defaultValue: Option[Any]) {
    def this(name: String, typeInfo: Any) = this(name, typeInfo, None)

    /** Converts to Param. */
    def toParam: Param = Param(name, TypeInfo.convert(typeinfo), defaultValue)
  }

  /** ClassMeta 构建器——由编译期挖掘器 [[ClassMetaDigger]] 驱动（静态编译 + builder）。
    *
    * 与 BeanInfo.Builder 同构：宏在编译期解析字段/构造器/方法，运行时只做
    * 类型转换与组装。属性按名排序、方法按 (名, 参数) 排序，保证二进制输出确定。
    */
  class Builder(val clazz: Class[_]) {
    private val properties = new mutable.ArrayBuffer[Property]
    private val ctors = new mutable.ArrayBuffer[Ctor]
    private val methods = new mutable.ArrayBuffer[Method]

    /** Adds a property; Option type is peeled to its element type with isOptional=true
      * (same shape as [[BeanMetaConverter.from]]).
      */
    def addProperty(name: String, ti: Any, isTransient: Boolean): Unit = {
      TypeInfo.convert(ti) match
        case o: OptionType => properties += Property(name, o.elementType, isTransient, isOptional = true)
        case other         => properties += Property(name, other, isTransient, isOptional = false)
    }

    /** Adds a constructor with parameters (head of `build()` result is the primary one). */
    def addCtor(paramInfos: Array[ParamHolder]): Unit =
      ctors += Ctor(paramInfos.toSeq.map(_.toParam))

    /** Adds a method record with erased JVM param names (e.g. "java/lang/String", "int"). */
    def addMethod(name: String, paramTypes: Array[String]): Unit =
      methods += Method(name, ArraySeq.from(paramTypes))

    /** Assembles the ClassMeta (properties sorted by name, methods by name+params). */
    def build(): ClassMeta =
      ClassMeta(clazz, properties.toSeq.sortBy(_.name), ctors.toSeq,
        methods.toSeq.sortBy(m => (m.name, m.paramTypes.mkString("|"))))
  }
}
