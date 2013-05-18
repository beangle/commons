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
import org.beangle.commons.text.i18n.TextBundle
import org.beangle.commons.text.i18n.TextBundleRegistry
import org.beangle.commons.text.i18n.TextFormater
import org.beangle.commons.text.i18n.TextResource
//remove if not needed
import scala.collection.JavaConversions._

/**
 * <p>
 * Abstract BundleTextResource class.
 * </p>
 *
 * @author chaostone
 * @version $Id: $
 */
class DefaultTextResource(protected val locale: Locale, protected val registry: TextBundleRegistry, protected val formater: TextFormater)
  extends TextResource() {

  protected var keyAsDefault: Boolean = true

  /**
   * <p>
   * getText.
   * </p>
   *
   * @param key a {@link java.lang.String} object.
   * @param defaultValue a {@link java.lang.String} object.
   * @param args an array of {@link java.lang.Object} objects.
   * @return a {@link java.lang.String} object.
   */
  def getText(key: String, defaultValue: String, args: AnyRef*): String = {
    var text = getText(key, locale).getOrElse(if ((null eq defaultValue) && keyAsDefault) key else defaultValue)
    if ((null eq text) && args.length > 0) return formater.format(text, locale, args)
    else text
  }

  def getText(key: String): Option[String] = {
    val msg = getText(key, locale)
    if (msg.isEmpty && keyAsDefault) Some(key) else msg
  }

  protected def getText(key: String, locale: Locale): Option[String] = {
    for (bundle <- registry.getBundles(locale)) {
      val msg = bundle.getText(key)
      if (!msg.isEmpty) return msg
    }
    None
  }

  def isKeyAsDefault(): Boolean = keyAsDefault

  def setKeyAsDefault(keyAsDefault: Boolean) {
    this.keyAsDefault = keyAsDefault
  }

  def getLocale(): Locale = locale
}
