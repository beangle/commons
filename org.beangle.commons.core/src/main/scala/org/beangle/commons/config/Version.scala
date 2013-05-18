/*
 * Beangle, Agile Java/Scala Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2013, Beangle Software.
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
package org.beangle.commons.config

import scala.beans.BeanProperty

/**
 * <p>
 * Version class.
 * </p>
 *
 * @author chaostone
 * @version $Id: $
 * @since 3.2.0
 */
class Version {

  @BeanProperty
  var name: String = _

  @BeanProperty
  var vendor: String = _

  @BeanProperty
  var version: String = _

  @BeanProperty
  var majorVersion: Int = _

  @BeanProperty
  var minorVersion: Int = _
}
