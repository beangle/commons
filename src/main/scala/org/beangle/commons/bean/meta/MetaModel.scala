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

/** MetaModel：单个类的元数据模型（properties / ctors / methods）的纯数据表示。
  *
  * 由 [[MetaModelCodec.parse]] 产出（v2 二进制解码，不含任何反射解析出的 Method）；
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
}
