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
package org.beangle.data.dao

import org.beangle.commons.model.Entity
import org.beangle.commons.collection.page.PageLimit
import scala.collection.immutable.Seq
/**
 * dao 查询辅助类
 *
 * @author chaostone
 */
trait EntityDao {
  /**
   * 查询指定id的对象
   *
   * @param clazz 类型
   * @param id 唯一标识
   */
  def get[T <: Entity[ID], ID](clazz: Class[T], id: ID): T

  def getAll[T <: Entity[_]](clazz: Class[T]): Seq[T]

  /**
   * find T by id.
   */
  def find[T <: Entity[ID], ID](clazz: Class[T], id: ID): Option[T]

  def find[T <: Entity[ID], ID](clazz: Class[T], ids: Iterable[ID]): Seq[T]

  def findBy[T <: Entity[_]](entityClass: Class[T], keyName: String, values: Iterable[_]): Seq[T]

  def findBy[T <: Entity[_]](entityName: String, keyName: String, values: Iterable[_]): Seq[T]

  /**
   * save or update entities
   */
  def saveOrUpdate[E](first: E, entities: E*): Unit

  /**
   * save or update entities
   */
  def saveOrUpdate[E](entities: Iterable[E]): Unit

  /**
   * remove entities.
   */
  def remove[E](entities: Iterable[E]): Unit

  /**
   * remove entities.
   */
  def remove[E](first: E, entities: E*): Unit

  /**
   * remove entities by id
   */
  def remove[T <: Entity[ID], ID](clazz: Class[T], id: ID, ids: ID*): Unit

  /**
   * Search by QueryBuilder
   */
  def search[T](builder: QueryBuilder[T]): Seq[T]
  /**
   * Search Query
   */
  def search[T](query: Query[T]): Seq[T]

  def search[T](query: String, params: Any*): Seq[T]

  def search[T](queryString: String, params: collection.Map[String, _]): Seq[T]

  def search[T](queryString: String, params: collection.Map[String, _], limit: PageLimit, cacheable: Boolean): Seq[T]

  /**
   * Search Unique Result
   */
  def uniqueResult[T](builder: QueryBuilder[T]): T

  /**
   * 在同一个session保存、删除
   */
  def execute(opts: Operation*): Unit

  /**
   * 执行一个操作构建者提供的一系列操作
   *
   * @param builder
   */
  def execute(builder: Operation.Builder): Unit

  def executeUpdate(queryString: String, parameterMap: collection.Map[String, _]): Int

  def executeUpdate(queryString: String, arguments: Any*): Int

  def executeUpdateRepeatly(queryString: String, arguments: Iterable[Iterable[_]]): List[Int]

  // 容器相关
  def evict(entity: AnyRef): Unit

  /**
   * Initialize entity whenever session close or open
   */
  def initialize[T](entity: T): T

  def refresh[T](entity: T): T

  def count(entityClass: Class[_], keyName: String, value: Any): Long

  def exists(entityClass: Class[_], attr: String, value: Any): Boolean

  def exists(entityName: String, attr: String, value: Any): Boolean

  def duplicate(entityClass: Class[_], id: Any, params: collection.Map[String, _]): Boolean

  def duplicate(entityName: String, id: Any, params: collection.Map[String, _]): Boolean

  def duplicate[T <: Entity[_]](clazz: Class[T], id: Any, codeName: String, codeValue: Any): Boolean
}
