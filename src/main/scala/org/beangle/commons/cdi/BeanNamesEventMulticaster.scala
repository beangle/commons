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

import org.beangle.commons.bean.Initializing
import org.beangle.commons.event.{DefaultEventMulticaster, EventListener}
import org.beangle.commons.lang.annotation.description

@description("依据名称查找监听者的事件广播器")
class BeanNamesEventMulticaster(listenerNames: Seq[String]) extends DefaultEventMulticaster, Initializing {

  var container: Container = _

  override def init(): Unit = {
    listenerNames foreach { beanName =>
      if (container.contains(beanName)) addListener(container.getBean[EventListener[_]](beanName).get)
    }
  }
}
