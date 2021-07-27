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

package org.beangle.commons.lang.reflect

import java.lang.reflect.{Method, Modifier, ParameterizedType, TypeVariable}
import org.beangle.commons.collection.IdentityCache
import org.beangle.commons.lang.reflect.Reflections.deduceParamTypes

import scala.collection.mutable
import scala.collection.immutable.ArraySeq
import scala.quoted.*

object BeanInfos {

  /**
   * class info cache
   */
  private var cache = new IdentityCache[Class[_], BeanInfo]

  /**
    * Get ClassInfo from cache or load it by type.
    */
  def get(clazz: Class[_]): Option[BeanInfo] = {
    var exist = cache.get(clazz)
    if null == exist then None else Some(exist)
  }

  inline def of(inline clazzes:Class[_]*):List[BeanInfo] = ${ofImpl('clazzes)}

  inline def of[T](clazz:Class[T]): BeanInfo = ${ ofImpl('clazz);}

  def ofImpl(argsExpr:Expr[Seq[Class[_]]])(using Quotes):Expr[List[BeanInfo]]={
    import quotes.reflect.*
    argsExpr match{
      case Varargs(cls)=>
       val biList = cls.map { cl =>
           cl.asTerm match{
             case TypeApply(term,trees) => new BeanInfoDigger[quotes.type](trees.head.tpe).dig()
           }
        }
       '{
         val bis = ${Expr.ofList(biList)}
         bis.foreach{ bi=> BeanInfos.update(bi)}
         bis
       }
      case _=>
        report.error(s"Args must be explicit", argsExpr)
        '{???}
    }
  }

  def ofImpl[T:Type](ec:Expr[Class[T]])(implicit quotes: Quotes):Expr[BeanInfo]={
    import quotes.reflect.*
    val digger = new BeanInfoDigger[quotes.type](quotes.reflect.TypeRepr.of[T])
    '{
      val ci = ${digger.dig()}
      BeanInfos.update(ci)
    }
  }

  /** register classInfo
    *
    * @param bi
    */
  def update(bi: BeanInfo):BeanInfo={
    cache.put(bi.clazz,bi)
    bi
  }

  /**
   * Load ClassInfo using reflections
   */
  def load(clazz: Class[_]): BeanInfo = {
    var exist = cache.get(clazz)
    if(null!=exist) return exist
    val ci = BeanInfoLoader.load(clazz)
    cache.put(clazz,ci)
    ci
  }

}
