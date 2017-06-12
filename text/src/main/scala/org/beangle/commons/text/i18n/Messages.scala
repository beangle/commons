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
package org.beangle.commons.text.i18n

import java.util.Locale

object Messages {
  def apply(locale: Locale): Messages = {
    new Messages(locale, new DefaultTextBundleRegistry(), new DefaultTextFormater())
  }
}

class Messages(locale: Locale, val registry: TextBundleRegistry, val format: TextFormater) {
  def get(clazz: Class[_], key: String): String = {
    if (key == "class") {
      val bundle = registry.load(locale, clazz.getPackage.getName + ".package")
      bundle.get(clazz.getSimpleName).orNull
    } else {
      new HierarchicalTextResource(clazz, locale, registry, format)(key).orNull
    }
  }
}

