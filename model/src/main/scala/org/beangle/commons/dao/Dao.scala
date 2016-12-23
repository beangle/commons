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
package org.beangle.commons.dao

import org.beangle.commons.model.Entity

/**
 * <p>
 * Dao trait
 * [/p>
 *
 * @author chaostone
 */
trait Dao[T <: Entity[ID], ID] {

  /**
   * get T by id.
   */
  def get(id: ID): T

  /**
   * find T by id.
   */
  def find(id: ID): Option[T]

  /**
   * search T by id.
   */
  def find(first: ID, ids: ID*): Seq[T]

  /**
   * save or update entities
   */
  def saveOrUpdate(first: T, entities: T*)

  /**
   * save or update entities
   */
  def saveOrUpdate(entities: Seq[T]);

  /**
   * remove entities.
   */
  def remove(entities: Seq[T]);

  /**
   * remove entities.
   */
  def remove(first: T, others: T*);

  /**
   * remove entities by id
   */
  def remove(id: ID, ids: ID*);

  /**
   * get entity type
   */
  def entityClass: Class[T];

}
