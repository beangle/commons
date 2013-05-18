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

import java.text.MessageFormat
import java.util.Locale
import java.util.Map
import org.beangle.commons.collection.CollectUtils
import org.beangle.commons.text.i18n.TextFormater
import java.util.concurrent.ConcurrentHashMap
/**
 * DefaultTextFormater with cache
 *
 * @author chaostone
 * @since 3.0.0
 */
class DefaultTextFormater extends TextFormater {

  protected val caches = new collection.mutable.HashMap[Locale, ConcurrentHashMap[String, MessageFormat]]

  def format(text: String, locale: Locale, args: AnyRef*): String = {
    var localeCache = caches.get(locale).orNull
    //double check
    if (null eq localeCache) {
      caches.synchronized {
        localeCache = caches.get(locale).orNull
        if (null eq localeCache) {
          localeCache = new ConcurrentHashMap[String, MessageFormat]
          caches.put(locale, localeCache)
        }
      }

    }
    var format = localeCache.get(text)
    if (null == format) {
      format = new MessageFormat(text)
      localeCache.put(text, format)
    }
    format.format(args)
  }
}
