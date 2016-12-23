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
package org.beangle.commons.template.freemarker

import java.io.{ File, IOException }

import org.beangle.commons.bean.Initializing
import org.beangle.commons.io.IOs
import org.beangle.commons.lang.ClassLoaders
import org.beangle.commons.lang.Strings.{ split, substringAfter }
import org.beangle.commons.lang.annotation.description

import freemarker.cache.{ FileTemplateLoader, MultiTemplateLoader, TemplateLoader }
import freemarker.template.{ Configuration, ObjectWrapper, TemplateExceptionHandler }

object Configurer {

  //must before configuration init
  //disable freemarker logging
  System.setProperty(freemarker.log.Logger.SYSTEM_PROPERTY_NAME_LOGGER_LIBRARY, freemarker.log.Logger.LIBRARY_NAME_NONE)

  def newConfig: Configuration = {
    val configurer = new Configurer
    configurer.init()
    configurer.config
  }

  def newConfig(templatePath: String): Configuration = {
    val configurer = new Configurer
    configurer.templatePath = templatePath;
    configurer.init()
    configurer.config
  }
}

@description("Freemarker配置提供者")
class Configurer extends Initializing {

  val config = new Configuration(Configuration.VERSION_2_3_24)

  var contentType: String = _

  var enableCache = true

  var templatePath: String = _

  override def init(): Unit = {
    config.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER)
    config.setDefaultEncoding("UTF-8")
    config.setLocalizedLookup(false)
    config.setWhitespaceStripping(true)

    val props = properties
    for ((key, value) <- props) {
      if (null != key && null != value) config.setSetting(key, value)
    }

    config.setObjectWrapper(createObjectWrapper(props))
    config.setTemplateLoader(createTemplateLoader(props))

    var content_type = config.getCustomAttribute("content_type").asInstanceOf[String]
    if (null == content_type) content_type = "text/html"
    if (!content_type.contains("charset"))
      content_type += "; charset=" + config.getDefaultEncoding
    contentType = content_type
  }

  /**
   * The default template loader is a MultiTemplateLoader which includes
   * BeangleClassTemplateLoader(classpath:) and a WebappTemplateLoader
   * (webapp:) and FileTemplateLoader(file:) . All template path described
   * in init parameter templatePath or TemplatePlath
   * <p/>
   * The ClassTemplateLoader will resolve fully qualified template includes that begin with a slash.
   * for example /com/company/template/common.ftl
   * <p/>
   */
  def createTemplateLoader(props: Map[String, String]): TemplateLoader = {
    if (null == templatePath) templatePath = props.getOrElse("template_path", "class://")
    val paths: Array[String] = split(templatePath, ",")
    val loaders = new collection.mutable.ListBuffer[TemplateLoader]
    for (path <- paths) {
      if (path.startsWith("class://")) {
        loaders += new BeangleClassTemplateLoader(substringAfter(path, "class://"))
      } else if (path.startsWith("file://")) {
        try {
          loaders += new FileTemplateLoader(new File(substringAfter(path, "file://")))
        } catch {
          case e: IOException =>
            throw new RuntimeException("templatePath: " + path + " cannot be accessed", e)
        }
      } else {
        throw new RuntimeException("templatePath: " + path
          + " is not well-formed. Use [class://|file://] seperated with ,")
      }
    }
    if (loaders.size == 1) loaders.head else new MultiTemplateLoader(loaders.toArray[TemplateLoader])
  }

  def createObjectWrapper(props: Map[String, String]): ObjectWrapper = {
    val wrapper = new BeangleObjectWrapper()
    wrapper.setUseCache(false)
    wrapper
  }
  /**
   * Load the multi settings from the /META-INF/freemarker.properties and
   * /freemarker.properties file on the classpath
   *
   * @see freemarker.template.Configuration#setSettings for the definition of valid settings
   */
  def properties: Map[String, String] = {
    val properties = new collection.mutable.HashMap[String, String]
    // 1. first META-INF/freemarker.properties
    for (url <- ClassLoaders.getResources("META-INF/freemarker.properties"))
      properties ++= IOs.readJavaProperties(url)

    // 2. second global freemarker.properties
    for (url <- ClassLoaders.getResources("freemarker.properties"))
      properties ++= IOs.readJavaProperties(url)

    // 3. system properties
    val sysProps = System.getProperties
    val sysKeys = sysProps.propertyNames
    while (sysKeys.hasMoreElements) {
      val key = sysKeys.nextElement.asInstanceOf[String]
      val value: String = sysProps.getProperty(key)
      if (key.startsWith("freemarker.")) properties.put(substringAfter(key, "freemarker."), value)
    }
    if (!enableCache) properties.put(Configuration.TEMPLATE_UPDATE_DELAY_KEY, "0")
    properties.toMap
  }

}
