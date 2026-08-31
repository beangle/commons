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

package org.beangle.commons.aot

/** commons 内置机制面的 GraalVM native-image 提示。
  *
  * 集中注册不归属任何特定子库的 commons 机制项：
  *  - `META-INF/services/` 下文件：`ServiceLoader`/`spi` 机制下 classpath 上的 SPI 文件；
  *  - `beangle.xml`：`XmlConfigs`/`XmlProfileProvider` 经 `classpath*:` 读取的 cdi 模块声明；
  *  - `description` 注解：cdi `Binder` 绑定 bean 时经 `getAnnotation(classOf[description])` 读取；
  *  - `*.zh_CN`：i18n message bundle（`Messages` 加载的本地化资源）；
  *  - mime 类型表：`MediaTypes` 经 `Resources.load` 加载的
  *    `org/beangle/commons/activation/mime.types` 与 `mime-default.types`。
  *
  * 使用方无需在 resource-config.json/reflect-config.json 中手写这些项。
  */
class BuiltinAotHints extends AotHintRegistrar {

  override def registering(): Unit = {
    hints.registerPattern("META-INF/services/.*")
    hints.registerPattern("beangle\\.xml")
    hints.registerPattern(".*\\.zh_CN")
    hints.registerPattern("org/beangle/commons/activation/mime\\.types")
    hints.registerPattern("org/beangle/commons/activation/mime-default\\.types")

    hints.registerType(classOf[org.beangle.commons.lang.annotation.description])
    hints.registerType(classOf[org.beangle.commons.lang.annotation.value])
    hints.registerType(classOf[org.beangle.commons.lang.annotation.spi])
    hints.registerType(classOf[org.beangle.commons.lang.annotation.default_value])

    // 转换器对象：ConverterRegistry.add 经 converter.getClass.getMethods 反射解析
    // apply 参数/返回类型对，native 下需注册对象类的 public 方法（含 apply）。
    hints.registerType(
      org.beangle.commons.conversion.string.BooleanConverter.getClass,
      org.beangle.commons.conversion.string.NumberConverters.getClass,
      org.beangle.commons.conversion.string.DateConverter.getClass,
      org.beangle.commons.conversion.string.TemporalConverter.getClass,
      org.beangle.commons.conversion.string.TimeConverter.getClass,
      org.beangle.commons.conversion.string.JavaEnumConverters.getClass,
      org.beangle.commons.conversion.string.EnumConverters.getClass,
      org.beangle.commons.conversion.string.LocaleConverter.getClass,
      org.beangle.commons.conversion.string.ToStringConverter.getClass,
      org.beangle.commons.conversion.string.JsonConverter.getClass,
      org.beangle.commons.conversion.string.DurationConverter.getClass,
      org.beangle.commons.conversion.converter.Number2NumberConverter.getClass,
      org.beangle.commons.conversion.converter.IterableConverterFactory.getClass,
      org.beangle.commons.conversion.converter.DateTimeConverterFactory.getClass)
  }
}
