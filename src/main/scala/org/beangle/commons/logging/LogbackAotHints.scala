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

package org.beangle.commons.logging

import org.beangle.commons.aot.AotHintRegistrar

/** Logback/SLF4J 的 GraalVM native-image 反射提示。
  *
  * 集中注册 logback-classic/core 与 slf4j-api 在 native 下需要的反射点：
  *  - `org.slf4j.spi.LocationAwareLogger`：jboss-logging 选定 SLF4J 后端时经
  *    `LocationAwareLogger.class.getDeclaredMethods()` 反射查找 `log` 方法，漏注册会抛
  *    `NoSuchMethodError` 并被静默捕获回退 JUL（`JDKLoggerProvider`）；
  *  - `ch.qos.logback.classic.Logger`：jboss-logging 探测日志后端用的探测类；
  *  - Joran 按 `class=` 属性反射实例化的 appender/encoder/layout，以及
  *    `DefaultJoranConfigurator`/`BasicConfigurator`。
  *
  * 生成物随 beangle-commons.jar 内嵌的 `META-INF/native-image/beangle` 被 GraalVM
  * 自动发现并合并，使用方无需手写 logback/slf4j 反射项。
  */
class LogbackAotHints extends AotHintRegistrar {
  override def registering(): Unit = {
    hints.registerPattern("logback\\.xml")
    hints.registerType(
      classOf[ch.qos.logback.classic.Logger],
      classOf[ch.qos.logback.classic.BasicConfigurator],
      classOf[ch.qos.logback.classic.encoder.PatternLayoutEncoder],
      classOf[ch.qos.logback.classic.util.DefaultJoranConfigurator],
      classOf[ch.qos.logback.core.ConsoleAppender[?]],
      classOf[ch.qos.logback.core.OutputStreamAppender[?]],
      classOf[ch.qos.logback.core.encoder.Encoder[?]],
      classOf[ch.qos.logback.core.encoder.LayoutWrappingEncoder[?]],
      classOf[ch.qos.logback.core.pattern.PatternLayoutEncoderBase[?]],
      classOf[ch.qos.logback.core.spi.ContextAware],
      classOf[org.slf4j.Logger],
      classOf[org.slf4j.LoggerFactory],
      classOf[org.slf4j.spi.LocationAwareLogger],
    )
  }
}
