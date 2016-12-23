/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2016, Beangle Software.
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
package org.beangle.commons.cdi.spring.config

import org.beangle.commons.lang.Strings
import org.springframework.beans.factory.config.{BeanDefinition, BeanDefinitionHolder}
import org.springframework.beans.factory.support.GenericBeanDefinition

import ReconfigType.{Primary, Update}
/**
 * ReconfigBeanDefinitionHolder class.
 *
 * @author chaostone
 */
class ReconfigBeanDefinitionHolder(beanDefinition: BeanDefinition, beanName: String, aliases: Array[String])
  extends BeanDefinitionHolder(beanDefinition, beanName, aliases) {

  var configType = ReconfigType.Update

  /**
   * Constructor for ReconfigBeanDefinitionHolder.
   */
  def this(beanDefinition: BeanDefinition, beanName: String) {
    this(beanDefinition, beanName, null)
  }

  override def toString: String = {
    val sb = new StringBuilder
    sb.append(configType match {
      case Update => "UPDATE:"
      case Primary => "UPDATE(Primary):"
      case _ => "REMOVE:"
    })

    sb.append(this.getBeanName).append("'")
    if (null != getAliases && getAliases.length > 0) {
      sb.append(" aliases[").append(Strings.join(getAliases, ",")).append("]")
    }
    val bd = getBeanDefinition
    if (null != bd.getBeanClassName) {
      sb.append(" [").append(bd.getBeanClassName).append("]")
    }
    if (null != bd.getScope && !bd.getScope.equals("")) {
      sb.append(" scope=").append(bd.getScope)
    }
    if (bd.isAbstract) sb.append(" abstract=true")
    if (bd.isLazyInit) sb.append(" lazyInit=true")
    if (bd.isInstanceOf[GenericBeanDefinition]) {
      val gbd = bd.asInstanceOf[GenericBeanDefinition]
      if (gbd.getAutowireMode > 0) sb.append(" autowireMode=").append(gbd.getAutowireMode)
      if (null != gbd.getFactoryBeanName) sb.append(" factoryBeanName=").append(gbd.getFactoryBeanName)
      if (null != gbd.getFactoryMethodName) sb.append(" factoryMethodName=").append(gbd.getFactoryMethodName)
      if (null != gbd.getInitMethodName) sb.append(" initMethodName=").append(gbd.getInitMethodName)
      if (null != gbd.getDestroyMethodName) sb.append(" destroyMethodName=").append(gbd.getDestroyMethodName)
    }
    sb.toString
  }

}
