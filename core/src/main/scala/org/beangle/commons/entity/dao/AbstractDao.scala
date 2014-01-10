/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2014, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.commons.entity.dao

import org.beangle.commons.entity.Entity

abstract class AbstractDao[T <: Entity[ID], ID](val entityClass: Class[T], val generalDao: GeneralDao) extends Dao[T, ID] {

  /**
   * get T by id.
   */
  def get(id: ID): T = generalDao.get(entityClass, id)

  /**
   * search T by id.
   */
  def find(id: ID): Option[T] = generalDao.find(entityClass, id)

  /**
   * search T by id.
   */
  def find(first: ID, ids: ID*): Seq[T] = generalDao.find(entityClass, first, ids: _*)

  /**
   * save or update entities
   */
  def saveOrUpdate(first: T, entities: T*) {
    generalDao.saveOrUpdate(first, entities)
  }

  /**
   * save or update entities
   */
  def saveOrUpdate(entities: Seq[T]) {
    generalDao.saveOrUpdate(entities)
  }

  /**
   * remove entities.
   */
  def remove(entities: Seq[T]) {
    generalDao.saveOrUpdate(entities)
  }

  /**
   * remove entities.
   */
  def remove(first: T, entities: T*) {
    generalDao.remove(first, entities)
  }

  /**
   * remove entities by id
   */
  def remove(id: ID, ids: ID*) {
    generalDao.remove(entityClass, id, ids)
  }

}
