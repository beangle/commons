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
package org.beangle.commons.text.i18n.impl

import java.util.Locale
import org.beangle.commons.text.i18n.TextResource

/**
 * <p>
 * NullTextResource class.
 * </p>
 *
 * @author chaostone
 * @version $Id: $
 */
class NullTextResource extends TextResource {

  def getText(key: String): Option[String] = Some(key)

  def getText(key: String, defaultValue: String, obj: AnyRef*): String = key

  def getLocale(): Locale = null
}
