/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2016, Beangle Software.
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

import java.beans.Transient

import org.beangle.commons.collection.Collections
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.annotation.description

class TestBean {

  var id: Int = _

  var name: String = _

  var intValue: Int = _

  var testEnum: TestEnum.TestVal = _

  def methodWithManyArguments(i: Int,
    f: Float,
    I: java.lang.Integer,
    F: java.lang.Float,
    c: TestBean,
    c1: TestBean,
    c2: TestBean): String = "test"

  @description("method1")
  def method1(a: Long): Unit = {
  }
}

class TestChildBean extends TestBean {
  def method2(a: Long): Unit = {

  }
}
class TestChild2Bean extends TestChildBean {
  override def method1(a: Long): Unit = {

  }
  override def method2(a: Long): Unit = {

  }
}

trait Animal {

  def getAge(): Number
}

class Dog extends Animal {

  def getAge(): java.lang.Integer = 0
}

trait Entity[ID] {
  def id: ID
  /**
   * Return true if persisted
   */
  @Transient
  def persisted: Boolean = id != null

  def name: String
}

abstract class NumIdBean[ID] extends Entity[ID] {

  var id: ID = _
}

abstract class StringIdBean extends Entity[String] {
  var id: String = _
}

class Book extends NumIdBean[java.lang.Long] {

  def myId = id

  var name = "book"
  val version = "3.0"

  def isEmpty = false

  var authors: List[Author] = _
}

class BookPrimitiveId extends NumIdBean[Long] {
  var name = "BookPrimitiveId"
}

class BookStore extends StringIdBean {
  var name = "BookStore"
}

class AbstractEntity[ID](val id: ID)

class NumberIdBean[T <: Number](id: T) extends AbstractEntity[T](id)

class Author(id: Integer) extends NumberIdBean[Integer](id)

class BigBookStore(val department: Seq[Department], val books: Map[String, Book]) {
  var properties: java.util.Properties = _
  var prices: Range = _
  var properties2: org.beangle.commons.collection.Properties = _
}

trait Hierarchical[T] {

  /** index no */
  var indexno: String = _

  /** 父级菜单 */
  var parent: T = _

  var children = Collections.newBuffer[T]

  def depth: Int = {
    Strings.count(indexno, ".") + 1
  }
}

class Department extends NumIdBean[Long] with Hierarchical[Department] {
  var name = "department"
}

