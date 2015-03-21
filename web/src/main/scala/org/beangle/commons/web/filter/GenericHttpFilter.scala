/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2014, Beangle Software.
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
package org.beangle.commons.web.filter

import scala.collection.mutable

import org.beangle.commons.bean.{ Initializing, Properties }
import org.beangle.commons.lang.Strings

import javax.servlet.{ Filter, FilterConfig, ServletException }
/**
 * @author chaostone
 */
abstract class GenericHttpFilter extends Filter with Initializing {

  var filterConfig: FilterConfig = _

  /**
   * Standard way of initializing this filter. Map config parameters onto bean
   * properties of this filter, and invoke subclass initialization.
   */
  def init(filterConfig: FilterConfig) {
    this.filterConfig = filterConfig
    val filterName = filterConfig.getFilterName
    initParams(filterConfig, requiredProperties)
    init()
  }

  private final def initParams(config: FilterConfig, requiredProperties: Set[String]) {
    val missingProps = new mutable.HashSet[String]
    if ((requiredProperties != null && !requiredProperties.isEmpty)) missingProps ++= requiredProperties
    val en = config.getInitParameterNames
    while (en.hasMoreElements()) {
      val property = en.nextElement().asInstanceOf[String]
      val value = config.getInitParameter(property)
      Properties.copy(this, property, value)
      missingProps.remove(property)
    }
    if (missingProps.size > 0) {
      throw new ServletException("Initialization from FilterConfig for filter '" + config.getFilterName +
        "' failed; the following required properties were missing: " +
        Strings.join(missingProps, ", "))
    }
  }

  /**
   * Make the name of this filter available to subclasses.
   */
  protected def filterName: String = {
    (if (filterConfig != null) filterConfig.getFilterName else "None")
  }

  override def init() {
  }

  /**
   * Set of required properties (Strings) that must be supplied as config
   * parameters to this filter.
   */
  def requiredProperties: Set[String] = Set.empty

  def destroy() {
  }
}
