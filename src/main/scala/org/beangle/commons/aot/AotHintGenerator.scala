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

import org.beangle.commons.json.{JsonArray, JsonObject}
import org.beangle.commons.logging.Logging

import java.io.File
import java.net.URLClassLoader
import java.nio.charset.StandardCharsets
import java.nio.file.{FileVisitResult, Files, Path, SimpleFileVisitor}
import java.nio.file.attribute.BasicFileAttributes
import scala.collection.mutable

/** Scans classpath for [[AotHintRegistrar]] implementations and writes
  * GraalVM native-image configuration files.
  *
  * Generated files:
  *  - `reflect-config.json` — classes needing reflection access
  *  - `resource-config.json` — resource patterns to include
  *  - `proxy-config.json` — JDK dynamic proxy interfaces
  *  - `serialization-config.json` — classes supporting Java serialization
  *
  * Stale config files from previous runs are automatically deleted when the
  * corresponding hint category is empty.
  */
object AotHintGenerator extends Logging {

  def main(args: Array[String]): Unit = {
    val (outputDir, classpath) = parseArgs(args)

    if (classpath.isEmpty) {
      logger.error("No classpath specified.")
      printUsage()
      System.exit(1)
    }

    val classLoader = new URLClassLoader(
      classpath.map(p => Path.of(p).toUri.toURL).toArray,
      getClass.getClassLoader)

    var merged = new AotHints
    var count = 0

    try {
      findClassFiles(classLoader) foreach { className =>
        try {
          val clazz = classLoader.loadClass(className)
          if (classOf[AotHintRegistrar].isAssignableFrom(clazz) && !clazz.isInterface) {
            val registrar = clazz.getDeclaredConstructor().newInstance().asInstanceOf[AotHintRegistrar]
            registrar.registering()
            merged.addAll(registrar.aotHints)
            count += 1
            logger.info(s"Imported hints from $className")
          }
        } catch {
          case _: ClassNotFoundException =>
          case _: NoClassDefFoundError =>
          case e: Exception =>
            logger.warn(s"Error loading $className: ${e.getMessage}")
        }
      }
    } finally classLoader.close()

    if (count == 0) {
      logger.warn("No AotHintRegistrar implementations found.")
      return
    }

    logger.info(s"Found $count AotHintRegistrar implementations")

    if (merged.isEmpty) {
      logger.warn("No hints registered. Skipping config generation.")
    } else {
      write(outputDir, merged)
      logger.info(s"Generated GraalVM configs in $outputDir (${merged.getTypes.size} types, ${merged.getPatterns.size} patterns, ${merged.getProxies.size} proxies, ${merged.getSerializables.size} serializables)")
    }
  }

  /** Writes non-empty config files and deletes stale ones from previous runs. */
  def write(outDir: Path, hints: AotHints): Unit = {
    Files.createDirectories(outDir)
    writeOrDelete(outDir.resolve("reflect-config.json"), hints.getTypes.nonEmpty)(writeReflect(_, hints.getTypes))
    writeOrDelete(outDir.resolve("resource-config.json"), hints.getPatterns.nonEmpty)(writeResource(_, hints.getPatterns))
    writeOrDelete(outDir.resolve("proxy-config.json"), hints.getProxies.nonEmpty)(writeProxy(_, hints.getProxies))
    writeOrDelete(outDir.resolve("serialization-config.json"), hints.getSerializables.nonEmpty)(writeSerializable(_, hints.getSerializables))
  }

  private def writeOrDelete(file: Path, nonEmpty: Boolean)(write: Path => Unit): Unit = {
    if (nonEmpty) write(file)
    else Files.deleteIfExists(file)
  }

  /** Writes reflect-config.json for classes needing reflection access. */
  def writeReflect(out: Path, types: collection.Set[Class[_]]): Unit = {
    val entries = types.toSeq.sortBy(_.getName).map { clazz =>
      JsonObject(
        "name" -> clazz.getName,
        "allDeclaredFields" -> true,
        "allDeclaredConstructors" -> true,
        "allDeclaredMethods" -> true)
    }
    Files.write(out, JsonArray(entries*).toJson.getBytes(StandardCharsets.UTF_8))
  }

  /** Writes resource-config.json for resource inclusion patterns. */
  def writeResource(out: Path, patterns: collection.Set[String]): Unit = {
    val includes = patterns.toSeq.sorted.map(p => JsonObject("pattern" -> p.replace('\\', '/')))
    val json = JsonObject(
      "resources" -> JsonObject(
        "includes" -> JsonArray(includes*),
        "excludes" -> JsonArray()),
      "bundles" -> JsonArray())
    Files.write(out, json.toJson.getBytes(StandardCharsets.UTF_8))
  }

  /** Writes proxy-config.json for JDK dynamic proxy interfaces. */
  def writeProxy(out: Path, proxies: collection.Set[List[Class[_]]]): Unit = {
    val entries = proxies.toSeq.sortBy(_.headOption.map(_.getName).getOrElse("")).map { ifaces =>
      JsonObject("interfaces" -> JsonArray(ifaces.map(c => c.getName)*))
    }
    Files.write(out, JsonArray(entries*).toJson.getBytes(StandardCharsets.UTF_8))
  }

  /** Writes serialization-config.json for classes supporting Java serialization. */
  def writeSerializable(out: Path, classes: collection.Set[Class[_]]): Unit = {
    val entries = classes.toSeq.sortBy(_.getName).map { clazz =>
      JsonObject("name" -> clazz.getName)
    }
    Files.write(out, JsonArray(entries*).toJson.getBytes(StandardCharsets.UTF_8))
  }

  private def findClassFiles(classLoader: ClassLoader): Seq[String] = {
    val classes = new mutable.ListBuffer[String]
    val urls = classLoader.getResources("")
    while (urls.hasMoreElements) {
      val url = urls.nextElement()
      if (url.getProtocol == "file") {
        val dir = Path.of(url.toURI)
        if (Files.isDirectory(dir)) {
          Files.walkFileTree(dir, new SimpleFileVisitor[Path] {
            override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
              if (file.toString.endsWith(".class")) {
                val relative = dir.relativize(file).toString
                  .replace(File.separatorChar, '/')
                  .stripSuffix(".class")
                  .replace('/', '.')
                classes += relative
              }
              FileVisitResult.CONTINUE
            }
          })
        }
      }
    }
    classes.toSeq
  }

  private def parseArgs(args: Array[String]): (Path, Seq[String]) = {
    var outputDir = Path.of("META-INF/native-image")
    val classpath = new mutable.ListBuffer[String]

    var i = 0
    while (i < args.length) {
      args(i) match {
        case "-o" | "--output" =>
          i += 1
          if (i < args.length) outputDir = Path.of(args(i))
        case "-h" | "--help" =>
          printUsage()
          System.exit(0)
        case arg if arg.startsWith("-") =>
          logger.error(s"Unknown option: $arg")
          printUsage()
          System.exit(1)
        case entry =>
          classpath += entry
      }
      i += 1
    }

    (outputDir, classpath.toSeq)
  }

  private def printUsage(): Unit = {
    println("""Usage: AotHintGenerator [options] <classpath-entry> [classpath-entry...]
              |
              |Scans classpath for AotHintRegistrar implementations and generates
              |GraalVM native-image configuration files:
              |  reflect-config.json       (reflection metadata)
              |  resource-config.json      (resource inclusion)
              |  proxy-config.json         (dynamic proxy interfaces)
              |  serialization-config.json (Java serialization)
              |
              |Options:
              |  -o, --output <dir>   Output directory (default: META-INF/native-image)
              |  -h, --help           Show this help
              |
              |Examples:
              |  AotHintGenerator target/classes
              |  AotHintGenerator -o src/main/resources/META-INF/native-image target/classes
              |""".stripMargin)
  }
}
