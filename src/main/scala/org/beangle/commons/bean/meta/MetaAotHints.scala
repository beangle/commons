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

package org.beangle.commons.bean.meta

import org.beangle.commons.aot.AotHintRegistrar

/** beanmeta.idx 的 GraalVM native-image 资源提示。
  *
  * MetaPlugin 生成的 `META-INF/beangle/beanmeta.idx` 在运行期由
  * [[MetaModels]] 通过 `classpath*:` 加载，因此 native 镜像需要内嵌该资源。
  * 此处集中注册资源 pattern，使用方无需在 resource-config.json 中手写。
  */
class MetaAotHints extends AotHintRegistrar {
  override def registering(): Unit = {
    hints.registerPattern("META-INF/beangle/beanmeta.idx")
  }
}
