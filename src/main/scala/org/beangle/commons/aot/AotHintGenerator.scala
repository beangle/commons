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
import org.beangle.commons.lang.reflect.Reflections

import java.net.URLClassLoader
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}
import scala.collection.mutable

/** Generates GraalVM native-image configuration files from the
 * [[AotHintRegistrar]] implementations listed in a registrars file
 * (one class name per line, `#` comments allowed).
 *
 * Generated files:
 *  - `reflect-config.json` — classes needing reflection access
 *  - `resource-config.json` — resource patterns to include
 *  - `proxy-config.json` — JDK dynamic proxy interfaces
 *  - `serialization-config.json` — classes supporting Java serialization
 *  - `native-image.properties` — extra native-image args (runtime class initialization)
 *
 * Stale config files from previous runs are automatically deleted when the
 * corresponding hint category is empty.
 */
object AotHintGenerator {

  def main(args: Array[String]): Unit = {
    val (outputDir, classpath, registrarsFile) = parseArgs(args)

    if (classpath.isEmpty) {
      val msg = "No classpath specified.\n"
      System.err.println(msg)
      printUsage()
      System.exit(1)
    }

    val classLoader = new URLClassLoader(
      classpath.map(p => Path.of(p).toUri.toURL).toArray,
      getClass.getClassLoader)

    try {
      val listFile = registrarsFile match {
        case Some(f) => f
        case None =>
          System.err.println("Missing --registrars: AotHintRegistrar class list file is required.\n")
          printUsage()
          sys.exit(1)
      }
      generateFromList(listFile, outputDir, classLoader)
    } finally classLoader.close()
  }

  /** 清单模式：以 --registrars 文件罗列的 AotHintRegistrar 类为契约，全部找到并导入才算成功。 */
  private def generateFromList(listFile: Path, outputDir: Path, classLoader: ClassLoader): Unit = {
    if (!Files.isRegularFile(listFile)) {
      System.err.println(s"Registrars file does not exist: $listFile")
      System.exit(1)
    }
    val names = readLines(listFile)
    if (names.isEmpty) {
      System.out.println(s"No registrars declared in $listFile; GraalVM config generation skipped")
      return
    }
    val merged = new AotHints
    val failures = new mutable.ListBuffer[String]
    names foreach { name =>
      Reflections.tryGetInstance[AotHintRegistrar](name, classLoader) match {
        case Some(registrar) =>
          registrar.registering()
          merged.addAll(registrar.aotHints)
          System.out.println(s"Imported hints from $name")
        case None =>
          failures += s"$name is not an AotHintRegistrar"
      }
    }
    if (failures.nonEmpty) {
      val msg = "Failed to load declared AotHintRegistrar implementations:\n" + failures.mkString("\n")
      System.err.println(msg)
      System.exit(1)
    }
    write(outputDir, merged)
    System.out.println(s"Generated GraalVM configs in $outputDir (${merged.getTypes.size} types, ${merged.getPatterns.size} patterns, ${merged.getProxies.size} proxies, ${merged.getSerializables.size} serializables, ${merged.getRuntimeInitialized.size} runtime-initialized)")
  }

  /** 读取清单文件：每行一个类名，# 开头为注释，忽略空行。 */
  private def readLines(file: Path): Seq[String] = {
    val lines = Files.readAllLines(file, StandardCharsets.UTF_8)
    val result = new mutable.ListBuffer[String]
    val it = lines.iterator()
    while (it.hasNext) {
      val line = it.next().trim
      if (line.nonEmpty && !line.startsWith("#")) result += line
    }
    result.toSeq
  }

  /** Writes non-empty config files and deletes stale ones from previous runs. */
  def write(outDir: Path, hints: AotHints): Unit = {
    Files.createDirectories(outDir)
    writeOrDelete(outDir.resolve("reflect-config.json"), hints.getTypes.nonEmpty)(writeReflect(_, hints.getTypes))
    writeOrDelete(outDir.resolve("resource-config.json"), hints.getPatterns.nonEmpty)(writeResource(_, hints.getPatterns))
    writeOrDelete(outDir.resolve("proxy-config.json"), hints.getProxies.nonEmpty)(writeProxy(_, hints.getProxies))
    writeOrDelete(outDir.resolve("serialization-config.json"), hints.getSerializables.nonEmpty)(writeSerializable(_, hints.getSerializables))
    writeOrDelete(outDir.resolve("native-image.properties"), hints.getRuntimeInitialized.nonEmpty)(writeNativeImageProperties(_, hints.getRuntimeInitialized))
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
    Files.write(out, JsonArray(entries *).toJson.getBytes(StandardCharsets.UTF_8))
  }

  /** Writes resource-config.json for resource inclusion patterns. */
  def writeResource(out: Path, patterns: collection.Set[String]): Unit = {
    // patterns are regexes: keep escapes intact (e.g. ".*\\.zh_CN"); do not rewrite backslashes
    val includes = patterns.toSeq.sorted.map(p => JsonObject("pattern" -> p))
    val json = JsonObject(
      "resources" -> JsonObject(
        "includes" -> JsonArray(includes *),
        "excludes" -> JsonArray()),
      "bundles" -> JsonArray())
    Files.write(out, json.toJson.getBytes(StandardCharsets.UTF_8))
  }

  /** Writes proxy-config.json for JDK dynamic proxy interfaces. */
  def writeProxy(out: Path, proxies: collection.Set[List[Class[_]]]): Unit = {
    val entries = proxies.toSeq.sortBy(_.headOption.map(_.getName).getOrElse("")).map { ifaces =>
      JsonObject("interfaces" -> JsonArray(ifaces.map(c => c.getName) *))
    }
    Files.write(out, JsonArray(entries *).toJson.getBytes(StandardCharsets.UTF_8))
  }

  /** Writes serialization-config.json for classes supporting Java serialization. */
  def writeSerializable(out: Path, classes: collection.Set[Class[_]]): Unit = {
    val entries = classes.toSeq.sortBy(_.getName).map { clazz =>
      JsonObject("name" -> clazz.getName)
    }
    Files.write(out, JsonArray(entries *).toJson.getBytes(StandardCharsets.UTF_8))
  }

  /** Writes native-image.properties carrying extra build args, e.g.
   *  `--initialize-at-run-time` for classes whose static initializers
   *  must run at runtime (SecureRandom users etc.). */
  def writeNativeImageProperties(out: Path, classes: collection.Set[Class[_]]): Unit = {
    val args = "--initialize-at-run-time=" + classes.toSeq.sortBy(_.getName).map(_.getName).mkString(",")
    Files.write(out, s"Args = $args\n".getBytes(StandardCharsets.UTF_8))
  }

  private def parseArgs(args: Array[String]): (Path, Seq[String], Option[Path]) = {
    var outputDir = Path.of("META-INF/native-image")
    var registrarsFile = Option.empty[Path]
    val classpath = new mutable.ListBuffer[String]

    var i = 0
    while (i < args.length) {
      args(i) match {
        case "-o" | "--output" =>
          i += 1
          if (i < args.length) outputDir = Path.of(args(i))
        case "-r" | "--registrars" =>
          i += 1
          if (i < args.length) registrarsFile = Some(Path.of(args(i)))
          else {
            System.err.println("Missing value for --registrars")
            printUsage()
            System.exit(1)
          }
        case "-h" | "--help" =>
          printUsage()
          System.exit(0)
        case arg if arg.startsWith("-") =>
          System.err.println(s"Unknown option: $arg")
          printUsage()
          System.exit(1)
        case entry =>
          classpath += entry
      }
      i += 1
    }

    (outputDir, classpath.toSeq, registrarsFile)
  }

  private def printUsage(): Unit = {
    println(
      """Usage: AotHintGenerator [options] <classpath-entry> [classpath-entry...]
        |
        |Generates GraalVM native-image configuration files from AotHintRegistrar
        |implementations declared in a registrars list file:
        |  reflect-config.json       (reflection metadata)
        |  resource-config.json      (resource inclusion)
        |  proxy-config.json         (dynamic proxy interfaces)
        |  serialization-config.json (Java serialization)
        |
        |Options:
        |  -o, --output <dir>   Output directory (default: META-INF/native-image)
        |  -r, --registrars <file>  List of AotHintRegistrar class names, one per line
        |                           (# comments allowed). All listed classes must be
        |                           found, otherwise exit with a non-zero code.
        |  -h, --help           Show this help
        |
        |Examples:
        |  AotHintGenerator --registrars aot-registrars.txt -o out target/classes
        |""".stripMargin)
  }
}
