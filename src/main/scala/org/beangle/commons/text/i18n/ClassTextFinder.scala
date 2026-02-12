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

/** Finds text by traversing class hierarchy, interfaces, and package bundles. */
class ClassTextFinder(locale: Locale, registry: TextBundleRegistry) {

  /** Looks up message in class hierarchy: class bundle, interfaces, package, superclass. */
  def find(clazz: Class[_], key: String): Option[String] = {
    find(clazz, key, new collection.mutable.HashSet[String])
  }

  private def bundleName(clazz: Class[_]): String = {
    val classFullName = clazz.getName
    val dollarIdx = classFullName.indexOf('$')
    if (dollarIdx == -1) classFullName else classFullName.substring(0, dollarIdx)
  }

  /** Look for message in aClass' class hierarchy.
   * <ol>
   * <li>Look for the message in a resource bundle for aClass</li>
   * <li>If not found, look for the message in a resource bundle for any implemented interface</li>
   * <li>If not found, traverse up the Class' hierarchy and repeat from the first sub-step</li>
   * </ol>
   *
   * @param clazz   a class
   * @param key     key
   * @param checked checked set
   * @return
   */
  private def find(clazz: Class[_], key: String, checked: collection.mutable.Set[String]): Option[String] = {
    val className = bundleName(clazz)

    if (checked.contains(className)) return None
    checked.add(className)

    var msg = getClassMessage(className, key)
    if (msg.nonEmpty) return msg

    // check my package
    msg = getPackageMessage(clazz.getPackageName, key, checked)
    if (msg.nonEmpty) return msg

    // check all interfaces class and package
    val interfaces = new collection.mutable.HashSet[Class[_]]
    collectInterfaces(clazz, interfaces)
    for (ifc <- interfaces if msg.isEmpty) {
      msg = getClassMessage(ifc.getName, key)
    }
    if (msg.nonEmpty) return msg
    for (ifc <- interfaces if msg.isEmpty) {
      msg = this.getPackageMessage(ifc.getPackageName, key, checked)
    }
    if (msg.nonEmpty) return msg

    // traverse up hierarchy
    if (clazz.isInterface) {
      for (ifc <- clazz.getInterfaces if msg.isEmpty) {
        msg = this.find(ifc, key, checked)
      }
      if (msg.nonEmpty) return msg
    } else {
      val superClass = clazz.getSuperclass
      if (!superClass.equals(classOf[Object]) && !clazz.isPrimitive) {
        msg = this.find(superClass, key, checked)
        if (msg.nonEmpty) return msg
      }
    }
    None
  }

  private def collectInterfaces(me: Class[_], interfaces: collection.mutable.Set[Class[_]]): Unit = {
    for (ifc <- me.getInterfaces) {
      if (!ifc.getName.startsWith("java.") && !ifc.getName.startsWith("scala.")) interfaces.add(ifc)
      collectInterfaces(ifc, interfaces)
    }
  }

  /** Gets message from package resource bundle. */
  protected final def getPackageMessage(packageName: String, key: String, checked: collection.mutable.Set[String]): Option[String] = {
    if checked.contains(packageName) then
      None
    else
      checked.add(packageName)
      registry.load(locale, packageName + ".package").get(key)
  }

  /** Gets message from class resource bundle. */
  protected final def getClassMessage(className: String, key: String): Option[String] = {
    registry.load(locale, className).get(key)
  }
}
