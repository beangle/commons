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

package org.beangle.commons.lang.annotation

import scala.annotation.StaticAnnotation

/** Signifies that a public API (public class, method or field) is subject to
  * incompatible changes, or even removal, in a future release. An API bearing
  * this annotation is exempt from any compatibility guarantees made by its
  * containing library. Note that the presence of this annotation implies nothing
  * about the quality or performance of the API in question, only the fact that
  * it is not "API-frozen."
  *
  * @author chaostone
  * @since 3.0.2
  */
class beta extends StaticAnnotation
