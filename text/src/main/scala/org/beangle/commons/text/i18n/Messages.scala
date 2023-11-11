/*
 * Copyright (C) 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.beangle.commons.text.i18n

import java.util.Locale

object Messages {
  def apply(locale: Locale): Messages =
    new Messages(locale, new DefaultTextBundleRegistry(), new DefaultTextFormatter())
}

class Messages(locale: Locale, val registry: TextBundleRegistry, val format: TextFormatter) {
  def get(clazz: Class[_], key: String): String = {
    if key == "class" then
      val bundle = registry.load(locale, clazz.getPackage.getName + ".package")
      bundle.get(clazz.getSimpleName).orNull
    else
      new ClassTextFinder(locale, registry).find(clazz, key).orNull
  }
}
