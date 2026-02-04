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

package org.beangle.commons.event

import java.util.EventObject

/** Abstract Event class.
 *
 * @author chaostone
 */
class Event(src: Any) extends EventObject(src)

/** BusinessEvent
 *
 * @author chaostone
 * @since 3.0.0
 */
class BusinessEvent(source: AnyRef) extends Event(source) {
  /** System time when the event happened */
  val timestamp = System.currentTimeMillis()

  /** event subject */
  var subject: String = _

  /** event details */
  var detail: String = _

  /** resource where the event happened */
  var resource: String = _
}
