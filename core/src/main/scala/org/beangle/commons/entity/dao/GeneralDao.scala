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
package org.beangle.commons.entity.dao

/**
 * dao 查询辅助类
 *
 * @author chaostone
 */
trait GeneralDao {
  /**
   * 查询指定id的对象
   *
   * @param clazz 类型
   * @param id 唯一标识
   * @return null
   */
  def get[T, ID](clazz: Class[T], id: ID): T

  /**
   * find T by id.
   */
  def find[T, ID](clazz: Class[T], id: ID): Option[T]

  /**
   * search T by id.
   */
  def find[T, ID](clazz: Class[T], first: ID, ids: ID*): List[T]

  /**
   * save or update entities
   */
  def saveOrUpdate[T](first: T, entities: T*)

  /**
   * save or update entities
   */
  def saveOrUpdate[T](entities: collection.Seq[T])

  /**
   * remove entities.
   */
  def remove[T](entities: collection.Seq[T])

  /**
   * remove entities.
   */
  def remove[T](first: T, entities: T*)

  /**
   * remove entities by id
   */
  def remove[T, ID](clazz: Class[T], id: ID, ids: ID*)

  def search[T](query: Query[T]): List[T]

  /**
   * 在同一个session保存、删除
   */
  def execute(opts: Operation*)

  /**
   * 执行一个操作构建者提供的一系列操作
   *
   * @param builder
   */
  def execute(builder: Operation.Builder)

  // 容器相关
  def evict(entity: AnyRef)

  /**
   * Initialize entity whenever session close or open
   *
   * @param <T>
   * @param entity
   */
  def initialize[T](entity: T): T

  def refresh[T](entity: T): T

  def count(entityClass: Class[_], keyName: String, value: Any): Long

  def exists(entityClass: Class[_], attr: String, value: Any): Boolean

  def duplicate(entityClass: Class[_], id: Any, params: Map[String, Any]): Boolean

}
