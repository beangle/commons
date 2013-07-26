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
package org.beangle.commons.jpa.bean

import javax.persistence.CascadeType
import javax.persistence.FetchType
import javax.persistence.ManyToOne
import javax.persistence.MappedSuperclass
import javax.persistence.OneToMany
import javax.persistence.OrderBy
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size
import scala.collection.mutable
import org.beangle.commons.entity.Hierarchical
import org.beangle.commons.entity.Entity
/**
 * Hierarchical interface.
 *
 * @author chaostone
 */
@MappedSuperclass
trait HierarchicalBean[T <: Entity[_]] extends Hierarchical[T] {

  /** index no */
  @Size(max = 30)
  @NotNull
  var indexno: String

  /** 父级菜单 */
  @ManyToOne(fetch = FetchType.LAZY)
  var parent: Option[T]

  @OneToMany(mappedBy = "parent", cascade = Array(CascadeType.ALL))
  @OrderBy("indexno")
  var children: mutable.Seq[T] = new mutable.ListBuffer[T]
}
