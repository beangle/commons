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

import org.beangle.commons.bean.meta.MetaModel.ClassMeta

import java.io.OutputStream
import scala.collection.mutable
import scala.quoted.*

/** 类元数据注册表：子类覆写 [[registering]] 模板方法、内部调用 [[register]] 注册类，
  * 收集后经 [[encode]] 将 ClassMeta 写入调用者指定的流（beaninfo.idx）。
  *
  * {{{
  * class AppRegistry extends MetaRegistry {
  *   override def registering(): Unit = {
  *     register(classOf[User], classOf[Role])
  *   }
  * }
  * val out = new FileOutputStream("beaninfo.idx")
  * new AppRegistry().encode(out)
  * }}}
  *
  * [[register]] 是 inline 宏：在子类调用点（编译期字面量）直接经 ClassMetaDigger
  * 挖掘 ClassMeta，保持泛型精度（如 Map[Int,X] 键、ctor 泛型参数）。
  */
abstract class MetaRegistry {

  /** 父类成员变量：已注册类的元数据（含 clazz）。 */
  private val metas = new mutable.ArrayBuffer[ClassMeta]

  private var registered = false

  /** 模板方法：子类覆写，内部调用 [[register]] 注册类。 */
  protected def registering(): Unit = ()

  /** 将类（编译期字面量）注册到本注册表（宏：编译期挖掘 ClassMeta，保持精度）。 */
  protected inline def register(inline clazzes: Class[_]*): Unit = ${ MetaRegistry.registerImpl('clazzes, 'this) }

  /** 收集所有已注册的类元数据（首次调用触发 registering 模板方法）。 */
  def collect(): Seq[ClassMeta] = {
    if !registered then
      registering()
      registered = true
    metas.toSeq
  }

  /** 将收集的类元数据编码写入指定流（beaninfo.idx）。 */
  def encode(out: OutputStream): Unit = MetaIndex.write(out, collect())

  /** 宏展开用：将挖掘出的 ClassMeta 加入成员变量。 */
  protected def addMetas(cms: Iterable[ClassMeta]): Unit = metas ++= cms
}

object MetaRegistry {

  /** 宏：挖掘 class 字面量列表并注册到 registry。 */
  def registerImpl(clazzes: Expr[Seq[Class[_]]], registry: Expr[MetaRegistry])(using Quotes): Expr[Unit] = {
    '{
      ${ registry }.addMetas(${ ClassMetaDigger.digInto(clazzes) })
    }
  }
}
