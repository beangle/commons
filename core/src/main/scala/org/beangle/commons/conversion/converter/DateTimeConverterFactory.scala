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

package org.beangle.commons.conversion.converter

import org.beangle.commons.conversion.Converter
import org.beangle.commons.conversion.impl.ConverterFactory

import java.time.*
import java.time.temporal.Temporal

/** Convert LocalDateTime to Instant.
 */
object DateTimeConverterFactory extends ConverterFactory[LocalDateTime, Temporal] {

  object ToInstant extends Converter[LocalDateTime, Instant] {
    override def apply(dt: LocalDateTime): Instant = dt.atZone(ZoneId.systemDefault).toInstant
  }

  object ToZonedDateTime extends Converter[LocalDateTime, ZonedDateTime] {
    override def apply(dt: LocalDateTime): ZonedDateTime = dt.atZone(ZoneId.systemDefault)
  }

  object ToOffsetDateTime extends Converter[LocalDateTime, OffsetDateTime] {
    override def apply(dt: LocalDateTime): OffsetDateTime = dt.atZone(ZoneId.systemDefault).toOffsetDateTime
  }

  register(classOf[Instant], ToInstant)
  register(classOf[ZonedDateTime], ToZonedDateTime)
  register(classOf[OffsetDateTime], ToOffsetDateTime)
}
