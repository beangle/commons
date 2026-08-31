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

package org.beangle.commons.bean.meta;

/** Java 父类：标准可读 JavaBean 属性（getTitle/setTitle），用于验证 MetaDig
 *  将继承的可读属性合并进 Scala 子类的 BeanMeta（进入 beanmeta.idx）。 */
public class JavaParentBean {

  private String title;

  public String getTitle() { return title; }
  public void setTitle(String title) { this.title = title; }
}
