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
