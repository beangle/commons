/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2014, Beangle Software.
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
package org.beangle.commons.lang.testbean

class TestBean {

  var id: java.lang.Integer = _

  var name: String = _

  var intValue: Int = _

  var testEnum: TestEnum.Val = _

  def methodWithManyArguments(i: Int,
    f: Float,
    I: java.lang.Integer,
    F: java.lang.Float,
    c: TestBean,
    c1: TestBean,
    c2: TestBean): String = "test"
}

trait Animal {

  def getAge(): Number
}

class Dog extends Animal {

  def getAge(): java.lang.Integer = 0
}

trait Entity[ID] {
  def id: ID
}

class NumIdBean[ID] extends Entity[ID] {

  var id: ID = _
}

class StringIdBean extends Entity[String] {
  var id: String = _
}

class Book extends NumIdBean[java.lang.Long] {

}

class BookPrimitiveId extends NumIdBean[Long] {
}

class BookStore extends StringIdBean

class AbstractEntity[ID](val id: ID)

class NumberIdBean[T <:Number](id : T) extends AbstractEntity[T](id)

class Author(id:Integer) extends NumberIdBean[Integer](id)
