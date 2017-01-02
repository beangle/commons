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

import org.beangle.commons.event.{ DefaultEventMulticaster, EventMulticaster }
import org.beangle.commons.lang.ClassLoaders
import org.beangle.commons.lang.reflect.Reflections
import org.beangle.commons.lang.time.Stopwatch
import org.beangle.commons.logging.Logging
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.support.{ BeanDefinitionRegistryPostProcessor, DefaultListableBeanFactory }
import org.springframework.beans.factory.xml.{ ResourceEntityResolver, XmlBeanDefinitionReader }
import org.springframework.beans.support.ResourceEditorRegistrar
import org.springframework.core.convert.ConversionService
import org.springframework.core.env.StandardEnvironment
import org.springframework.core.io.{ DefaultResourceLoader, Resource }
import org.springframework.core.io.support.{ PathMatchingResourcePatternResolver, ResourcePatternResolver }
import org.springframework.util.ClassUtils

/**
 * Simple BeanFactory loader
 */
class BeanFactoryLoader extends DefaultResourceLoader with ResourcePatternResolver with ContextLoader with Logging {
  var environment = new StandardEnvironment()
  var eventMulticaster: EventMulticaster = _
  var resourcePatternResolver: ResourcePatternResolver = new PathMatchingResourcePatternResolver
  var classLoader = ClassUtils.getDefaultClassLoader()
  var result: BeanFactory = _

  override def load(id: String, contextClassName: String, configLocation: String, parent: BeanFactory): BeanFactory = {
    val watch = new Stopwatch(true)
    logger.info(s"$id starting")

    val result =
      if (null == contextClassName) new DefaultListableBeanFactory()
      else Reflections.newInstance(ClassLoaders.load(contextClassName)).asInstanceOf[DefaultListableBeanFactory]

    result.setAllowBeanDefinitionOverriding(false)
    result.setSerializationId(id)
    result.setParentBeanFactory(parent)
    loadBeanDefinitions(result, environment.resolveRequiredPlaceholders(configLocation))
    refresh(result)
    logger.info(s"$id started in $watch")
    result
  }

  protected def loadBeanDefinitions(beanFactory: DefaultListableBeanFactory, configLocation: String): Unit = {
    val reader = new XmlBeanDefinitionReader(beanFactory)
    reader.setEnvironment(environment)
    reader.setResourceLoader(this)
    reader.setEntityResolver(new ResourceEntityResolver(this))
    reader.setValidating(false)
    reader.loadBeanDefinitions(configLocation)
  }

  protected def refresh(beanFactory: DefaultListableBeanFactory): Unit = {
    prepareBeanFactory(beanFactory)
    invokeBeanFactoryPostProcessors(beanFactory)
    initApplicationEventMulticaster(beanFactory)
    finishBeanFactoryInitialization(beanFactory)
    eventMulticaster.multicast(new BeanFactoryRefreshedEvent(beanFactory))
  }
  /**
   * Initialize the ApplicationEventMulticaster.
   */
  protected def initApplicationEventMulticaster(beanFactory: ConfigurableListableBeanFactory): Unit = {
    val multicasters = beanFactory.getBeansOfType(classOf[EventMulticaster])
    if (multicasters.isEmpty()) {
      eventMulticaster = new DefaultEventMulticaster
    } else {
      eventMulticaster = multicasters.values.iterator().next()
    }
  }
  /**
   * Configure the factory's standard context characteristics,
   * such as the context's ClassLoader and post-processors.
   * @param beanFactory the BeanFactory to configure
   */
  protected def prepareBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
    beanFactory.setBeanClassLoader(classLoader)
    beanFactory.addPropertyEditorRegistrar(new ResourceEditorRegistrar(this, environment))

    beanFactory.registerResolvableDependency(classOf[BeanFactory], beanFactory)
    if (!beanFactory.containsLocalBean("environment")) {
      beanFactory.registerSingleton("environment", environment)
    }
    if (!beanFactory.containsLocalBean("systemProperties")) {
      beanFactory.registerSingleton("systemProperties", environment.getSystemProperties())
    }
    if (!beanFactory.containsLocalBean("systemEnvironment")) {
      beanFactory.registerSingleton("systemEnvironment", environment.getSystemEnvironment())
    }
  }

  /**
   * Instantiate and invoke all registered BeanFactoryPostProcessor beans,
   * respecting explicit order if given.
   * <p>Must be called before singleton instantiation.
   */
  protected def invokeBeanFactoryPostProcessors(beanFactory: DefaultListableBeanFactory): Unit = {
    val postProcessorNames = beanFactory.getBeanNamesForType(classOf[BeanDefinitionRegistryPostProcessor], true, false)
    postProcessorNames foreach { name =>
      val pp = beanFactory.getBean(name, classOf[BeanDefinitionRegistryPostProcessor])
      pp.postProcessBeanDefinitionRegistry(beanFactory)
      pp.postProcessBeanFactory(beanFactory)
    }
  }

  /**
   * Finish the initialization of this context's bean factory,
   * initializing all remaining singleton beans.
   */
  protected def finishBeanFactoryInitialization(beanFactory: ConfigurableListableBeanFactory) {
    val conversionServiceBeanName = "conversionService"
    if (beanFactory.containsBean(conversionServiceBeanName) &&
      beanFactory.isTypeMatch(conversionServiceBeanName, classOf[ConversionService])) {
      beanFactory.setConversionService(
        beanFactory.getBean(conversionServiceBeanName, classOf[ConversionService]))
    }

    beanFactory.setTempClassLoader(null)
    beanFactory.freezeConfiguration()
    beanFactory.preInstantiateSingletons()
  }

  override def getResources(locationPattern: String): Array[Resource] = {
    return this.resourcePatternResolver.getResources(locationPattern)
  }

  override def close(): Unit = {

  }
}
