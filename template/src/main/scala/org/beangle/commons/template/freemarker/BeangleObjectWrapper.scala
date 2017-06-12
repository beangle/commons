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

import java.beans.PropertyDescriptor
import java.lang.reflect.{ Method, Modifier }
import java.{ util => ju }

import scala.collection.JavaConverters._

import org.beangle.commons.lang.Strings.{ substringAfter, uncapitalize }

import freemarker.core.CollectionAndSequence
import freemarker.ext.beans.{ BeansWrapper, BeansWrapperConfiguration, MapModel, MethodAppearanceFineTuner }
import freemarker.ext.beans.BeansWrapper.MethodAppearanceDecision
import freemarker.ext.beans.BeansWrapper.MethodAppearanceDecisionInput
import freemarker.template.{ AdapterTemplateModel, Configuration, DefaultObjectWrapper, DefaultObjectWrapperConfiguration, SimpleCollection, SimpleDate }
import freemarker.template.{ TemplateModelAdapter, SimpleNumber, SimpleScalar, SimpleSequence, TemplateBooleanModel, TemplateCollectionModel, TemplateHashModelEx }
import freemarker.template.{ TemplateMethodModelEx, TemplateModel }

object BeangleObjectWrapper {
  def wrapperConfig(): BeansWrapperConfiguration = {
    val config = new DefaultObjectWrapperConfiguration(Configuration.VERSION_2_3_24) {}
    config.setMethodAppearanceFineTuner(new ScalaMethodAppearanceFineTuner)
    config
  }
}

class BeangleObjectWrapper extends DefaultObjectWrapper(BeangleObjectWrapper.wrapperConfig, false) {

  override def unwrap(model: TemplateModel, hint: Class[_]): AnyRef = {
    model match {
      case sm: SeqModel => sm.seq
      case _            => super.unwrap(model, hint)
    }
  }

  override def wrap(obj: AnyRef): TemplateModel = {
    if (null == obj || None == obj) return null
    obj match {
      //basic types
      case s: String   => new SimpleScalar(s)
      case num: Number => new SimpleNumber(num)
      case date: ju.Date => {
        date match {
          case sdate: java.sql.Date           => new SimpleDate(sdate)
          case stime: java.sql.Time           => new SimpleDate(stime)
          case stimestamp: java.sql.Timestamp => new SimpleDate(stimestamp)
          case _                              => new SimpleDate(date, getDefaultDateType())
        }
      }
      case b: java.lang.Boolean         => if (b) TemplateBooleanModel.TRUE else TemplateBooleanModel.FALSE

      //wrap types
      case Some(p)                      => wrap(p.asInstanceOf[Object])
      case tm: TemplateModel            => tm

      // scala collections
      case seq: collection.Seq[_]       => new SeqModel(seq, this)
      case set: collection.Set[_]       => new SimpleSequence(setAsJavaSet(set), this)
      case map: collection.Map[_, _]    => new FriendlyMapModel(mapAsJavaMap(map), this)
      case iter: Iterable[_]            => new SimpleSequence(asJavaCollection(iter), this)

      // java collections
      case array: Array[_]              => new SimpleSequence(ju.Arrays.asList(array: _*), this)
      case collection: ju.Collection[_] => new SimpleSequence(collection, this)
      case map: ju.Map[_, _]            => new FriendlyMapModel(map, this)
      case iter: ju.Iterator[_]         => new SimpleCollection(iter, this)
      // misc
      case tma: TemplateModelAdapter    => tma.getTemplateModel
      case node: org.w3c.dom.Node       => wrapDomNode(node)
      case _                            => new StringModel(obj, this)
    }
  }
}

/**
 * Attempting to get the best of both worlds of FM's MapModel and
 * simplemapmodel, by reimplementing the isEmpty(), keySet() and values()
 * methods. ?keys and ?values built-ins are thus available, just as well as
 * plain Map methods.
 */
class FriendlyMapModel(map: ju.Map[_, _], wrapper: BeansWrapper) extends MapModel(map, wrapper) with TemplateHashModelEx
    with TemplateMethodModelEx with AdapterTemplateModel {

  // Struts2将父类的&& super.isEmpty()省去了，原因不知
  override def isEmpty(): Boolean = {
    `object`.asInstanceOf[ju.Map[_, _]].isEmpty()
  }

  // 此处实现与MapModel不同，MapModel中复制了一个集合,同时不要复制object中的keys
  override protected def keySet(): ju.Set[_] = {
    `object`.asInstanceOf[ju.Map[_, _]].keySet()
  }

  // add feature
  override def values(): TemplateCollectionModel = {
    new CollectionAndSequence(new SimpleSequence((`object`.asInstanceOf[ju.Map[_, _]]).values(), wrapper))
  }
}

class ScalaMethodAppearanceFineTuner extends MethodAppearanceFineTuner {

  def process(in: MethodAppearanceDecisionInput, decision: MethodAppearanceDecision): Unit = {
    val clazz = in.getContainingClass
    val method = in.getMethod
    val name = method.getName
    if (name.equals("hashCode") || name.equals("toString")) return
    propertyName(method) foreach { propertyName =>
      val pd = new PropertyDescriptor(propertyName, method, null)
      decision.setExposeAsProperty(pd)
      decision.setExposeMethodAs(name)
      decision.setMethodShadowsProperty(false)
    }
  }

  private def propertyName(m: Method): Option[String] = {
    val name = m.getName
    if (m.getParameterTypes().length == 0 && classOf[Unit] != m.getReturnType() && Modifier.isPublic(m.getModifiers)
      && !Modifier.isStatic(m.getModifiers) && !Modifier.isSynchronized(m.getModifiers)) {
      if (name.startsWith("get") && name.length > 3 && Character.isUpperCase(name.charAt(3))) Some(uncapitalize(substringAfter(name, "get")))
      else if (name.startsWith("is") && name.length > 2 && Character.isUpperCase(name.charAt(2))) Some(uncapitalize(substringAfter(name, "is")))
      else Some(name)
    } else {
      None
    }
  }
}
