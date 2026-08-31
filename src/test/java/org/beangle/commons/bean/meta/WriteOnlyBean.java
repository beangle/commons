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

/** 含 write-only 属性（仅 setter、无 getter）的 JavaBean，对应 Spring setProxyInterfaces 一类。 */
public class WriteOnlyBean {

  private String secret;
  private boolean enabled;
  private Class<?>[] proxyInterfaces;
  private String name;

  public void setSecret(String secret) { this.secret = secret; }

  public void setEnabled(boolean enabled) { this.enabled = enabled; }

  /** 对应 Spring AbstractSingletonProxyFactoryBean#setProxyInterfaces 的 write-only 形态。 */
  public void setProxyInterfaces(Class<?>[] proxyInterfaces) { this.proxyInterfaces = proxyInterfaces; }

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
}
