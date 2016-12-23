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
package org.beangle.commons.cdi.spring.context

import org.beangle.commons.event.EventListener
import org.beangle.commons.cdi.{ Container, ContainerListener }
import org.beangle.commons.lang.annotation.description
import org.beangle.commons.event.Event
import org.beangle.commons.cdi.spring.config.BindModuleProcessor
import org.springframework.beans.factory.{ BeanFactory, BeanFactoryAware, InitializingBean, NoSuchBeanDefinitionException }
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import collection.JavaConverters
/**
 * Spring based IOC Container
 *
 * @author chaostone
 * @since 3.1.0
 */
@description("Spring提供的Bean容器")
class SpringContainer extends BindModuleProcessor with Container with InitializingBean
    with BeanFactoryAware with EventListener[BeanFactoryEvent] {

  var parent: Container = _

  var context: ConfigurableListableBeanFactory = _

  var listeners: List[ContainerListener] = Nil

  override def getType(key: Any): Option[Class[_]] = {
    try {
      val clazz = context.getType(key.toString)
      if (null == clazz) None else Some(clazz)
    } catch {
      case e: NoSuchBeanDefinitionException => None
    }
  }

  override def getDefinition(key: Any): Any = {
    context.getBeanDefinition(key.toString)
  }

  override def contains(key: Any): Boolean = {
    context.containsBean(key.toString)
  }

  override def getBean[T](key: Any): Option[T] = {
    try {
      Some(context.getBean(key.toString).asInstanceOf[T])
    } catch {
      case e: NoSuchBeanDefinitionException => None
    }
  }

  override def getBean[T](clazz: Class[T]): Option[T] = {
    try {
      Some(context.getBean(clazz).asInstanceOf[T])
    } catch {
      case e: NoSuchBeanDefinitionException => None
    }
  }

  override def getBeans[T](clazz: Class[T]): Map[Any, T] = {
    JavaConverters.mapAsScalaMap(context.getBeansOfType(clazz)).toMap
  }

  override def keys: Set[_] = {
    context.getBeanDefinitionNames().toSet
  }

  override def setBeanFactory(beanFactory: BeanFactory): Unit = {
    context = beanFactory.asInstanceOf[ConfigurableListableBeanFactory]
  }

  /**
   * Move temporary hooks into myself
   * PS. for SpringContainer is a BeanDefinitionRegistryPostProcessor, so when context initializing, the bean
   *     is inited before others,so using spring native InitializingBean,not beangle's Initializing interface.
   */
  override def afterPropertiesSet(): Unit = {
    if (null == context.getParentBeanFactory) {
      if (null == Container.ROOT) Container.ROOT = this
    } else {
      parent = context.getParentBeanFactory.getBean(classOf[SpringContainer])
    }
    Container.containers.add(this)
    this.listeners = Container.listeners
    Container.listeners = Nil
  }

  /**
   * Handle an application event.
   */
  def onEvent(event: BeanFactoryEvent): Unit = {
    val c = event.getSource.asInstanceOf[ConfigurableListableBeanFactory]
    //for child application context issue a event to parent,so we should take a look.
    if (c == context) {
      event match {
        case cre: BeanFactoryRefreshedEvent =>
          getListeners(c) foreach (l => l.onStarted(this))
        case cce: BeanFactoryClosedEvent =>
          if (Container.ROOT == this) Container.ROOT = null
          Container.containers.remove(this)
          getListeners(c) foreach (l => l.onStopped(this))
        case _ =>
      }
    }
  }

  private def getListeners(factory: ConfigurableListableBeanFactory): Iterable[ContainerListener] = {
    val listenerSet = new collection.mutable.HashSet[ContainerListener]
    listenerSet ++= listeners
    listenerSet ++= JavaConverters.collectionAsScalaIterable(factory.getBeansOfType(classOf[ContainerListener]).values())
    listenerSet
  }
  /**
   * Determine whether this listener actually supports the given event type.
   */
  def supportsEventType(eventType: Class[_ <: Event]): Boolean = {
    classOf[BeanFactoryRefreshedEvent] == eventType
  }

  /**
   * Determine whether this listener actually supports the given source type.
   */
  def supportsSourceType(sourceType: Class[_]): Boolean = {
    true
  }
}
