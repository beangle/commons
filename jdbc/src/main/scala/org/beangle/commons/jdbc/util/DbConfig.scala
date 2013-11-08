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
package org.beangle.commons.jdbc.util

import org.beangle.commons.jdbc.dialect.Dialect
import org.beangle.commons.bean.Initializing
import org.beangle.commons.lang.Strings
import java.util.Properties



class DbConfig(val dialect: Dialect) extends Initializing{
  var url: String = _
  var user: String = _
  var password: String = _
  var driver: String = _
  var props: Properties = new Properties
  var schema: String = _
  var catalog:String=_

  def init() {
    if (Strings.isEmpty(schema)){
      schema=dialect.defaultSchema
      if(schema=="$user") schema=user
    }
    if(driver.endsWith("OracleDriver")) schema=schema.toUpperCase()
  }
}
