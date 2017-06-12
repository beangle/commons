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
package org.beangle.commons.cdi.spring.beans

import java.beans.PropertyEditorSupport
import util.matching.Regex

/**
 * Editor for [[scala.util.matching.Regex]], to directly populate a `Regex` property.
 */
class RegexEditor extends PropertyEditorSupport {

  override def setAsText(text: String) {
    text match {
      case null => setValue(null)
      case s => setValue(s.r)
    }
  }

  override def getAsText: String = {
    getValue match {
      case null => ""
      case regex: Regex => regex.pattern.pattern()
    }
  }
}
