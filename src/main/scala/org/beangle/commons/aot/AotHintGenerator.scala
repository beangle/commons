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
import org.beangle.commons.lang.ClassLoaders
import org.beangle.commons.lang.reflect.Reflections

import java.net.URLClassLoader
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}
import scala.collection.mutable

/** Generates GraalVM native-image configuration files from the
 * [[AotHintRegistrar]] implementations listed in a registrars file and the
 * by-name-loaded classes listed in a classes file (one class name per line,
 * `#` comments allowed).
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
    val (outputDir, classpath, registrarsFile, classesFile) = parseArgs(args)

    if (classpath.isEmpty) {
      val msg = "No classpath specified.\n"
      System.err.println(msg)
      printUsage()
      System.exit(1)
    }
    if (registrarsFile.isEmpty && classesFile.isEmpty) {
      val msg = "Missing --registrars/--classes: at least one class list file is required.\n"
      System.err.println(msg)
      printUsage()
      System.exit(1)
    }

    val classLoader = new URLClassLoader(
      classpath.map(p => Path.of(p).toUri.toURL).toArray,
      getClass.getClassLoader)

    try generateFromLists(registrarsFile, classesFile, outputDir, classLoader)
    finally classLoader.close()
  }

  /** 清单模式：--registrars 文件罗列的 AotHintRegistrar 类与 --classes 罗列的
   *  按名加载类（如 web initializer）均为契约，全部找到并注册才算成功。 */
  private def generateFromLists(registrarsFile: Option[Path], classesFile: Option[Path],
      outputDir: Path, classLoader: ClassLoader): Unit = {
    val merged = new AotHints
    val failures = new mutable.ListBuffer[String]
    val missing = new mutable.ListBuffer[String]
    var declaredCount = 0
    registrarsFile foreach { listFile =>
      if (!Files.isRegularFile(listFile)) {
        System.err.println(s"Registrars file does not exist: $listFile")
        System.exit(1)
      }
      val names = readLines(listFile)
      declaredCount += names.size
      names foreach { name =>
        Reflections.tryGetInstance[AotHintRegistrar](name, classLoader) match {
          case Some(registrar) =>
            try {
              registrar.registering()
              merged.addAll(registrar.aotHints)
              registerRegistrarClass(name, classLoader, merged)
              System.out.println(s"Imported hints from $name")
            } catch {
              // 编译未完成/引用类缺失：静默归类为缺失，交由调用方决定是否重试
              case _: ClassNotFoundException | _: LinkageError => missing += name
              case e: Throwable =>
                failures += s"$name failed while registering(): ${e.getClass.getName}: ${e.getMessage}"
            }
          case None =>
            ClassLoaders.get(name, classLoader) match {
              case Some(_) =>
                failures += s"$name is not an AotHintRegistrar"
              case None =>
                if (classExists(name, classLoader)) failures += s"$name is not an AotHintRegistrar"
                else missing += name
            }
        }
      }
    }
    classesFile foreach { listFile =>
      if (!Files.isRegularFile(listFile)) {
        System.err.println(s"Classes file does not exist: $listFile")
        System.exit(1)
      }
      val names = readLines(listFile)
      declaredCount += names.size
      names foreach { name =>
        if (!registerClass(name, classLoader, merged)) missing += name
      }
    }
    if (failures.nonEmpty) {
      val msg = "Failed to load declared AotHintRegistrar implementations:\n" + failures.mkString("\n")
      System.err.println(msg)
      System.exit(1)
    }
    if (missing.nonEmpty) {
      val msg = s"${missing.size} of $declaredCount declared classes not found" +
        " (compilation may still be in progress):\n" + missing.mkString("\n")
      System.err.println(msg)
      System.exit(2)
    }
    if (merged.isEmpty) {
      System.out.println("No hints registered; GraalVM config generation skipped")
      return
    }
    write(outputDir, merged)
    System.out.println(s"Generated GraalVM configs in $outputDir (${merged.getTypes.size} types, ${merged.getPatterns.size} patterns, ${merged.getProxies.size} proxies, ${merged.getSerializables.size} serializables, ${merged.getRuntimeInitialized.size} runtime-initialized)")
  }

  /** 注册 registrar 类自身，保证运行期按名实例化（Reflections.getInstance/tryGetInstance）
   *  在 native 镜像中可用：镜像只收录静态可达的类，仅靠 beangle.xml/清单里的字符串
   *  无法把 registrar 类带进闭包，必须显式登记。
   *
   *  普通类（无伴生）：运行时经 `getDeclaredConstructor().newInstance()` 实例化，注册构造器；
   *  Scala object（存在 `$` 伴生）：运行时经 `getDeclaredField("MODULE$").get(null)` 取单例，
   *  伴生类注册声明构造器 + 声明字段。类自身也注册构造器，覆盖 `$` 缺失的兜底路径。
   */
  private def registerRegistrarClass(name: String, classLoader: ClassLoader, merged: AotHints): Unit = {
    import AotPolicy.Category.*
    val companion = name + "$"
    ClassLoaders.get(companion, classLoader) foreach { clazz =>
      merged.registerType(clazz, AotPolicy(Set(DeclaredConstructors, DeclaredFields)))
    }
    ClassLoaders.get(name, classLoader) foreach { clazz =>
      merged.registerType(clazz, AotPolicy(Set(DeclaredConstructors)))
    }
  }

  /** 注册 --classes 清单中的按名加载类（如 web initializer）：不要求是
   *  AotHintRegistrar。用户声明的类名不带 `$`，但实际可能是 Scala object（运行期
   *  经伴生类 `MODULE$` 字段取单例），故伴生类（`name + "$"`）也要登记，否则 native
   *  镜像读不到单例入口。
   *
   *  主类登记 public 构造器（运行期 `getDeclaredConstructor().newInstance()` 实例化）；
   *  伴生类登记声明构造器 + 声明字段（`MODULE$` 字段反射读取）。类与伴生都缺失视为
   *  编译未完成，返回 false 交由调用方归类为可重试的缺失。
   */
  private[aot] def registerClass(name: String, classLoader: ClassLoader, merged: AotHints): Boolean = {
    import AotPolicy.Category.*
    var found = false
    ClassLoaders.get(name + "$", classLoader) foreach { clazz =>
      merged.registerType(clazz, AotPolicy(Set(DeclaredConstructors, DeclaredFields)))
      found = true
    }
    ClassLoaders.get(name, classLoader) foreach { clazz =>
      merged.registerType(clazz, AotPolicy(Set(PublicConstructors)))
      found = true
    }
    found
  }

  /** Scala object 伴生类（`$`）是否已存在：走到 case None 且 `name` 本身加载失败时，
   *  伴生类存在才是确定性的"不是 registrar"（companion-only 对象），伴生类缺失视为
   *  编译未完成，应归类为可重试的缺失。 */
  private def classExists(name: String, classLoader: ClassLoader): Boolean =
    ClassLoaders.exists(name + "$", classLoader)

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
    writeOrDelete(outDir.resolve("reflect-config.json"), hints.getTypePolicies.nonEmpty)(writeReflect(_, hints.getTypePolicies))
    writeOrDelete(outDir.resolve("resource-config.json"), hints.getPatterns.nonEmpty)(writeResource(_, hints.getPatterns))
    writeOrDelete(outDir.resolve("proxy-config.json"), hints.getProxies.nonEmpty)(writeProxy(_, hints.getProxies))
    writeOrDelete(outDir.resolve("serialization-config.json"), hints.getSerializables.nonEmpty)(writeSerializable(_, hints.getSerializables))
    writeOrDelete(outDir.resolve("native-image.properties"), hints.getRuntimeInitialized.nonEmpty)(writeNativeImageProperties(_, hints.getRuntimeInitialized))
  }

  private def writeOrDelete(file: Path, nonEmpty: Boolean)(write: Path => Unit): Unit = {
    if (nonEmpty) write(file)
    else Files.deleteIfExists(file)
  }

  /** Writes reflect-config.json for classes needing reflection access, one entry
   *  per type with the flags derived from its [[AotPolicy]]. */
  def writeReflect(out: Path, types: collection.Map[Class[_], AotPolicy]): Unit = {
    val entries = types.toSeq.sortBy(_._1.getName).map { case (clazz, policy) => reflectEntry(clazz, policy) }
    Files.write(out, JsonArray(entries *).toJson.getBytes(StandardCharsets.UTF_8))
  }

  /** Builds a reflect-config.json entry from the class and its policy. */
  private def reflectEntry(clazz: Class[_], policy: AotPolicy): JsonObject = {
    import AotPolicy.Category.*
    val entry = JsonObject("name" -> clazz.getName)
    policy.categories foreach {
      case PublicMethods            => entry.add("allPublicMethods", true)
      case DeclaredMethods          => entry.add("allDeclaredMethods", true)
      case PublicConstructors       => entry.add("allPublicConstructors", true)
      case DeclaredConstructors     => entry.add("allDeclaredConstructors", true)
      case PublicFields             => entry.add("allPublicFields", true)
      case DeclaredFields           => entry.add("allDeclaredFields", true)
      case QueryPublicMethods       => entry.add("queryAllPublicMethods", true)
      case QueryDeclaredMethods     => entry.add("queryAllDeclaredMethods", true)
      case QueryPublicConstructors  => entry.add("queryAllPublicConstructors", true)
      case QueryDeclaredConstructors => entry.add("queryAllDeclaredConstructors", true)
    }
    if (policy.unsafeAllocated) entry.add("unsafeAllocated", true)
    entry
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

  private def parseArgs(args: Array[String]): (Path, Seq[String], Option[Path], Option[Path]) = {
    var outputDir = Path.of("META-INF/native-image")
    var registrarsFile = Option.empty[Path]
    var classesFile = Option.empty[Path]
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
        case "-c" | "--classes" =>
          i += 1
          if (i < args.length) classesFile = Some(Path.of(args(i)))
          else {
            System.err.println("Missing value for --classes")
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

    (outputDir, classpath.toSeq, registrarsFile, classesFile)
  }

  private def printUsage(): Unit = {
    println(
      """Usage: AotHintGenerator [options] <classpath-entry> [classpath-entry...]
        |
        |Generates GraalVM native-image configuration files from:
        |  - AotHintRegistrar implementations declared in a registrars list file
        |    (hints for reflection, resources, proxies, serialization), and
        |  - classes loaded by name at runtime (e.g. web initializers) declared in
        |    a classes list file; they need not be AotHintRegistrar.
        |
        |Generated files:
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
        |  -c, --classes <file> List of classes loaded by name at runtime (web
        |                       initializers etc.), one per line; the class and its
        |                       Scala object companion (`$`) are registered into
        |                       reflect-config.json. All must be found.
        |  -h, --help           Show this help
        |
        |At least one of --registrars/--classes is required.
        |
        |Exit codes:
        |  0  success
        |  1  deterministic failure (bad declaration or loading error)
        |  2  declared class not found (compilation may still be in progress)
        |
        |Examples:
        |  AotHintGenerator --registrars aot-registrars.txt -o out target/classes
        |  AotHintGenerator --classes aot-classes.txt -o out target/classes
        |""".stripMargin)
  }
}
