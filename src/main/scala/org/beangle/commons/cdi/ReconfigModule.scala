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

package org.beangle.commons.cdi

import org.beangle.commons.cdi.Binding.ReferenceValue

import java.util as ju

abstract class ReconfigModule {
  var cfg: Reconfig = _
  var configUrl: String = _
  var ignoreMissing: Boolean = true

  final def configure(reconfig: Reconfig): Unit = {
    this.cfg = reconfig
    config()
    this.cfg.ignoreMissing = this.ignoreMissing

  }

  protected final def update(name: String): Reconfig.Definition = {
    cfg.definitions.get(name) match {
      case Some(d) => d
      case None =>
        val rd = new Reconfig.Definition(name, Reconfig.ReconfigType.Update, new Binding.Definition(name, null, null))
        cfg.definitions.put(name, rd)
        rd
    }
  }

  protected final def remove(name: String): Unit = {
    val rd = new Reconfig.Definition(name, Reconfig.ReconfigType.Remove, null)
    this.cfg.definitions.put(name, rd)
  }

  protected final def list(datas: AnyRef*): List[_] = {
    datas.toList
  }

  protected final def listref(classes: Class[_]*): List[_] = {
    classes.map(clazz => ReferenceValue(clazz.getName)).toList
  }

  protected final def set(datas: AnyRef*): Set[_] = {
    datas.toSet
  }

  protected final def map(entries: (_, _)*): Map[_, _] = {
    entries.toMap
  }

  protected final def props(keyValuePairs: String*): ju.Properties = {
    val properties = new ju.Properties
    keyValuePairs foreach { pair =>
      val index = pair.indexOf('=')
      if (index > 0) properties.put(pair.substring(0, index), pair.substring(index + 1))
    }
    properties
  }

  protected def config(): Unit
}
