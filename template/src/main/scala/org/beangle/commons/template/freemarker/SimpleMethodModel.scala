/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2017, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.commons.template.freemarker

import java.lang.reflect.Method

import freemarker.ext.beans.BeansWrapper
import freemarker.ext.beans._MethodUtil
import freemarker.template.TemplateMethodModelEx
import freemarker.template.TemplateModel
import freemarker.template.TemplateModelException
import org.beangle.commons.lang.reflect.MethodInfo

class SimpleMethodModel(obj: AnyRef, methodInfos: Seq[MethodInfo], wrapper: BeansWrapper)
    extends TemplateMethodModelEx {

  override def exec(arguments: java.util.List[_]): AnyRef = {
    try {

      val args = unwrapArguments(arguments, wrapper)
      methodInfos.find(x => x.parameterTypes.size == arguments.size) match {
        case Some(x) =>
          val method = x.method
          val retval = method.invoke(obj, args: _*)
          if (method.getReturnType == classOf[Unit])
            TemplateModel.NOTHING
          else wrapper.wrap(retval);
        case None => null
      }
    } catch {
      case e: TemplateModelException => throw e
      case e: Exception =>
        throw _MethodUtil.newInvocationTemplateModelException(obj, methodInfos.head.method, e);
    }
  }

  private def unwrapArguments(arguments: java.util.List[_], wrapper: BeansWrapper): Array[AnyRef] = {
    if (arguments eq null) return Array.empty[AnyRef]
    val args = new Array[AnyRef](arguments.size())
    var i = 0
    while (i < args.length) {
      args(i) = wrapper.unwrap(arguments.get(i).asInstanceOf[TemplateModel])
      i += 1
    }
    args
  }
}
