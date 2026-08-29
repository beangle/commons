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

import org.beangle.commons.bean.meta.MetaModel.BeanMeta
import org.beangle.commons.cdi.ReconfigModule
import org.beangle.commons.lang.ClassLoaders
import org.beangle.commons.lang.reflect.Reflections

import java.io.{File, FileOutputStream}
import java.net.URLClassLoader
import java.nio.file.{Files, Path}
import scala.collection.mutable

/** Generates beanmeta.idx from the MetaRegistrar subclasses listed in a registrars
  * file (one class name per line, `#` comments allowed).
  *
  * Usage:
  * {{{
  * java -cp <classpath> org.beangle.commons.bean.meta.MetaGenerator \
  *   --registrars modules.txt -o /path/to/beanmeta.idx /path/to/classes
  * }}}
  *
  * For GraalVM native-image configuration, see [[org.beangle.commons.aot.AotHintGenerator]].
  */
object MetaGenerator {

  private val DefaultOutputPath = "META-INF/beangle/beanmeta.idx"

  def main(args: Array[String]): Unit = {
    val (classesDirs, outputPath, beangleXml) = parseArgs(args)

    if (classesDirs.isEmpty) {
      val msg = "No classes directory specified.\n"
      System.err.println(msg)
      printUsage()
      System.exit(1)
    }

    val listFile = beangleXml match {
      case Some(f) => f
      case None =>
        System.err.println("Missing --registrars: MetaRegistrar class list file is required.\n")
        printUsage()
        sys.exit(1)
    }
    generateFromList(listFile, outputPath, classesDirs)
  }

  /** 清单模式：以 --registrars 文件罗列的 MetaRegistrar 类（MappingModule/BindModule 等）为契约，
   * 全部找到并收集 BeanMeta 才算成功；清单为空时正常跳过。具体读取哪些类由调用方（sbt 插件）决定。
   */
  private def generateFromList(listFile: File, outputPath: String, classpath: Seq[String]): Unit = {
    val classNames = readLines(listFile)
    if (classNames.isEmpty) {
      System.out.println(s"No registrars declared in $listFile; beanmeta.idx generation skipped")
      return
    }
    writeMetas(outputPath, collectMetas(classNames, classpath))
  }

  /** 加载清单中的 MetaRegistrar（MappingModule/BindModule 等）并收集 BeanMeta；
   * 清单为空返回空列表，任一声明类加载失败时非零退出。 */
  def collectMetas(classNames: Seq[String], classpath: Seq[String]): Seq[BeanMeta] = {
    val classLoader = new URLClassLoader(classpath.map(p => Path.of(p).toUri.toURL).toArray, getClass.getClassLoader)
    val metas = new mutable.ArrayBuffer[BeanMeta]
    val failures = new mutable.ListBuffer[String]
    val missing = new mutable.ListBuffer[String]
    try {
      classNames foreach { name =>
        Reflections.tryGetInstance[MetaRegistrar](name, classLoader) match {
          case Some(registrar) =>
            try {
              registrar.registering()
              val m = registrar.metas
              metas ++= m
              System.out.println(s"Collected ${m.size} BeanMeta from $name")
            } catch {
              // 编译未完成/引用类缺失：静默归类为缺失，交由调用方决定是否重试
              case _: ClassNotFoundException | _: LinkageError => missing += name
              case e: Throwable =>
                failures += s"$name failed while registering(): ${e.getClass.getName}: ${e.getMessage}"
            }
          case None =>
            ClassLoaders.get(name, classLoader) match {
              case Some(clazz) if classOf[ReconfigModule].isAssignableFrom(clazz) =>
                System.out.println(s"Skipped runtime-only ReconfigModule $name (no BeanMeta)")
              case Some(_) =>
                failures += s"$name is not a MetaRegistrar"
              case None =>
                if (classExists(name, classLoader)) failures += s"$name is not a MetaRegistrar"
                else missing += name
            }
        }
      }
    } finally classLoader.close()
    if (failures.nonEmpty) {
      val msg = "Failed to load declared MetaRegistrar implementations:\n" + failures.mkString("\n")
      System.err.println(msg)
      System.exit(1)
    }
    if (missing.nonEmpty) {
      val msg = s"${missing.size} of ${classNames.size} declared MetaRegistrar classes not found" +
        " (compilation may still be in progress):\n" + missing.mkString("\n")
      System.err.println(msg)
      System.exit(2)
    }
    metas.toSeq
  }

  /** Scala object 伴生类（`$`）是否已存在：走到 case None 且 `name` 本身加载失败时，
   *  伴生类存在才是确定性的"不是 registrar"（companion-only 对象），伴生类缺失视为
   *  编译未完成，应归类为可重试的缺失。 */
  private def classExists(name: String, classLoader: ClassLoader): Boolean =
    ClassLoaders.exists(name + "$", classLoader)

  /** 读取清单文件：每行一个类名，# 开头为注释，忽略空行。 */
  private def readLines(file: File): Seq[String] = {
    if (!file.isFile) {
      System.err.println(s"Registrars file does not exist: $file")
      System.exit(1)
    }
    val lines = java.nio.file.Files.readAllLines(file.toPath, java.nio.charset.StandardCharsets.UTF_8)
    val result = new mutable.ListBuffer[String]
    val it = lines.iterator()
    while (it.hasNext) {
      val line = it.next().trim
      if (line.nonEmpty && !line.startsWith("#")) result += line
    }
    result.toSeq
  }

  /** 写 beanmeta.idx；metas 为空时仍写出（调用方保证非空）。 */
  private def writeMetas(outputPath: String, metas: Seq[BeanMeta]): Unit = {
    val out = if (outputPath == "-") System.out
    else {
      val path = Path.of(outputPath)
      Files.createDirectories(path.getParent)
      new FileOutputStream(path.toFile)
    }

    try {
      MetaIndex.write(out, metas)
      if (outputPath != "-") {
        System.out.println(s"Generated $outputPath with ${metas.size} BeanMeta entries")
      }
    } finally {
      if (out ne System.out) out.close()
    }
  }

  /** Parses command-line arguments. */
  private def parseArgs(args: Array[String]): (Seq[String], String, Option[File]) = {
    var outputPath = DefaultOutputPath
    var beangleXml = Option.empty[File]
    val classesDirs = new mutable.ListBuffer[String]

    var i = 0
    while (i < args.length) {
      args(i) match {
        case "-o" | "--output" =>
          i += 1
          if (i < args.length) outputPath = args(i)
        case "-r" | "--registrars" =>
          i += 1
          if (i < args.length) beangleXml = Some(new File(args(i)))
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
        case dir =>
          classesDirs += dir
      }
      i += 1
    }

    (classesDirs.toSeq, outputPath, beangleXml)
  }

  private def printUsage(): Unit = {
    println("""Usage: MetaGenerator [options] <classpath-entry> [classpath-entry...]
              |
              |Generates beanmeta.idx from MetaRegistrar subclasses listed in a registrars
              |file.
              |
              |Options:
              |  -o, --output <path>    Output path (default: META-INF/beangle/beanmeta.idx)
              |  -r, --registrars <file>  List of MetaRegistrar class names, one per line
              |                           (# comments allowed). All listed classes must be
              |                           found, otherwise exit with a non-zero code.
              |  -h, --help              Show this help
              |
              |Examples:
              |  MetaGenerator --registrars modules.txt -o output.idx target/classes
              |""".stripMargin)
  }
}
