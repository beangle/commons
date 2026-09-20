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

package org.beangle.commons.lang.testbean

import org.beangle.commons.collection.Collections
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.annotation.{default_value, description, noreflect}

import java.beans.Transient

import scala.compiletime.uninitialized

class TestBean {
  var id: Int = uninitialized
  var name: String = uninitialized
  @default_value("1")
  var intValue: Int = uninitialized
  var age: Option[Int] = uninitialized
  var javaMap: java.util.Map[Int, String] = uninitialized
  var titles: Array[String] = uninitialized
  var testEnum: TestEnum = uninitialized
  var dogs: Iterable[Dog] = uninitialized
  var parent: Option[TestBean] = None
  var tags: Map[String, String] = Map.empty

  def methodWithManyArguments(
                               i: Int,
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
  def getAge: Number

  def name: String
}

class Dog extends Animal {
  var name: String = uninitialized

  def getAge: java.lang.Integer = 0

  def setColor(color: String): Unit = {

  }

  protected var character: String = uninitialized

  protected def skills: String = {
    "run"
  }
}

class QiutianDog extends Dog {
  protected override def skills: String = {
    "run and guard"
  }
}

trait Entity[ID] {
  def id: ID

  /** Return true if persisted
   */
  @Transient
  def persisted: Boolean = id != null

  def name: String
}

abstract class NumIdBean[ID] extends Entity[ID] {

  var id: ID = uninitialized
}

abstract class StringIdBean extends Entity[String] {
  var id: String = uninitialized
}

object AAA {
  private abstract class PrivateIdBean extends Entity[String] {
    var id: String = uninitialized
  }
  def main(args: Array[String]): Unit = {
    val c = classOf[PrivateIdBean]
    val methods = c.getMethods
    for(m <- methods){
      println(m)
    }
  }
}
class Book extends NumIdBean[java.lang.Long] {

  def myId = id

  var name = "book"

  var versions: List[Int] = uninitialized

  var versionSales: Map[Int, java.lang.Integer] = uninitialized
  var versionSales2: java.util.Map[Int, java.lang.Integer] = uninitialized

  def isEmpty = false

  var authors: List[Author] = uninitialized
}

class BookPrimitiveId extends NumIdBean[Long] {
  var name = "BookPrimitiveId"
}

class BookStore extends StringIdBean {
  var name = "BookStore"
}

class AbstractEntity[ID](val id: ID)

class NumberIdBean[T <: Number](id: T) extends AbstractEntity[T](id)

class Author(id: Integer) extends NumberIdBean[Integer](id) {
  var age: Option[Int] = uninitialized

  var `type`: String = uninitialized
}

class BigBookStore(val department: Seq[Department], val books: Map[String, Book]) {
  var properties: java.util.Properties = uninitialized
  var prices: Range = uninitialized
  var properties2: org.beangle.commons.collection.Properties = uninitialized
  @transient var tempName: String = uninitialized
}

trait Hierarchical[T] {

  /** index no */
  var indexno: String = uninitialized

  /** 父级菜单 */
  var parent: Option[T] = None

  var children = Collections.newBuffer[T]

  def depth: Int =
    Strings.count(indexno, ".") + 1
}

class Department extends NumIdBean[Long] with Hierarchical[Department] {
  var name = "department"
}

class Menu {
  var id: Long = uninitialized

  def getId: Long = id

  def childrenCount(): Int = 0

  def deepSize: Int = 1
}

case class Loader(name: String) {
  def load[T](clazz: Class[T]): T = clazz.getDeclaredConstructor().newInstance()
}

trait Factory[T] {
  def result: T
}

class LongFactory extends Factory[Long] {
  def result: Long = 9L

  @noreflect
  val typeName: String = getClass.getName

  def doSomeThing(): String = {
    println("slow operation")
    "ok"
  }

  class Inner {
    var name: String = uninitialized
  }
}

class Textbook extends NumIdBean[java.lang.Long] {
  def name: String = "textbook"
}

class Course(val id: Long) {
  def this(id: String) = {
    this(id.toLong)
  }

  var name: String = uninitialized
}

class Room(var id: Long = 0) {
  var name: String = uninitialized
}

class Teacher(var id: Long, name: String, genderId: Int = 1) {

  var depart: String = uninitialized

  def this(id: Long, department: Department, genderId: String) = {
    this(id, "", genderId.toInt)
  }

  def skills: String = {
    "teaching"
  }
}

class Professor(id: Long) extends Teacher(id, "professor", 2) {
  override def skills: String = "teaching,research"
}
