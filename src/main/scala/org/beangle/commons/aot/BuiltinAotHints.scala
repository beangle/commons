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
      classOf[org.beangle.commons.conversion.string.BooleanConverter.type],
      classOf[org.beangle.commons.conversion.string.NumberConverters.type],
      classOf[org.beangle.commons.conversion.string.DateConverter.type],
      classOf[org.beangle.commons.conversion.string.TemporalConverter.type],
      classOf[org.beangle.commons.conversion.string.TimeConverter.type],
      classOf[org.beangle.commons.conversion.string.JavaEnumConverters.type],
      classOf[org.beangle.commons.conversion.string.EnumConverters.type],
      classOf[org.beangle.commons.conversion.string.LocaleConverter.type],
      classOf[org.beangle.commons.conversion.string.ToStringConverter.type],
      classOf[org.beangle.commons.conversion.string.JsonConverter.type],
      classOf[org.beangle.commons.conversion.string.DurationConverter.type],
      classOf[org.beangle.commons.conversion.converter.Number2NumberConverter.type],
      classOf[org.beangle.commons.conversion.converter.IterableConverterFactory.type],
      classOf[org.beangle.commons.conversion.converter.DateTimeConverterFactory.type])

    // bean 生命周期接口：Spring/cdi 通过 getDeclaredFields/getDeclaredMethods 反射
    // 检测实现类，native 下需注册接口及伴生对象的字段。
    hints.registerType(classOf[org.beangle.commons.bean.Disposable])
    hints.registerType(classOf[org.beangle.commons.bean.Factory[?]])
    hints.registerType(classOf[org.beangle.commons.bean.Initializing])
    hints.registerType(classOf[org.beangle.commons.bean.Refreshable])
    hints.registerType(classOf[org.beangle.commons.bean.Scheduled])
    hints.registerType(classOf[org.beangle.commons.bean.meta.MetaModels.type])

    // 事件机制：EventMulticaster 经 getDeclaredFields 扫描监听器字段
    hints.registerType(
      classOf[org.beangle.commons.event.DefaultEventMulticaster],
      classOf[org.beangle.commons.event.EventListener[?]],
      classOf[org.beangle.commons.event.EventMulticaster],
      classOf[org.beangle.commons.event.EventPublisher])

    // cdi 容器事件：Spring ApplicationListener 机制反射实例化
    hints.registerType(
      classOf[org.beangle.commons.cdi.Container])

    // 其他 commons 类：序列化、JSON、分页、配置等
    hints.registerType(
      classOf[org.beangle.commons.activation.MediaType],
      classOf[org.beangle.commons.cache.Cache[?, ?]],
      classOf[org.beangle.commons.cache.CacheManager],
      classOf[org.beangle.commons.collection.page.Page[?]],
      classOf[org.beangle.commons.collection.page.SinglePage[?]],
      classOf[org.beangle.commons.config.XmlConfigs],
      classOf[org.beangle.commons.io.BinarySerializer],
      classOf[org.beangle.commons.io.Serializer],
      classOf[org.beangle.commons.json.JsonArray],
      classOf[org.beangle.commons.json.JsonObject],
      classOf[org.beangle.commons.script.ExprEvaluator],
      classOf[org.beangle.commons.text.i18n.TextBundleLoader],
      classOf[org.beangle.commons.text.i18n.TextFormatter])
    hints.registerType(classOf[org.beangle.commons.lang.JVM.type])
  }
}
