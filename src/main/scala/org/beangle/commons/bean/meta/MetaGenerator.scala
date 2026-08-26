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
import org.beangle.commons.lang.reflect.Reflections
import org.beangle.commons.logging.Logging

import java.io.{File, FileOutputStream}
import java.net.URLClassLoader
import java.nio.file.{FileVisitResult, Files, Path, SimpleFileVisitor}
import java.nio.file.attribute.BasicFileAttributes
import scala.collection.mutable

/** Tool for scanning MetaRegistrar subclasses from classes directory and generating beanmeta.idx.
  *
  * Usage:
  * {{{
  * // Scan classes directory and generate beanmeta.idx
  * java -cp <classpath> org.beangle.commons.bean.meta.MetaGenerator /path/to/classes
  *
  * // Generate to custom output path
  * java -cp <classpath> org.beangle.commons.bean.meta.MetaGenerator -o /path/to/output.idx /path/to/classes
  * }}}
  *
  * The tool scans the specified classes directory, finds all MetaRegistrar subclasses,
  * collects their BeanMeta entries, and generates beanmeta.idx file.
  *
  * For GraalVM native-image configuration, see [[org.beangle.commons.aot.AotHintGenerator]].
  */
object MetaGenerator extends Logging {

  private val DefaultOutputPath = "META-INF/beangle/beanmeta.idx"

  def main(args: Array[String]): Unit = {
    val (classesDirs, outputPath, beangleXml) = parseArgs(args)

    if (classesDirs.isEmpty) {
      val msg = "No classes directory specified.\n"
      logger.error(msg)
      System.err.println(msg)
      printUsage()
      System.exit(1)
    }

    beangleXml match {
      case Some(list) => generateFromList(list, outputPath, classesDirs)
      case None =>
        val metas = collectFromDirs(classesDirs)
        if (metas.isEmpty) {
          logger.error("No BeanMeta collected. Check classes directory.")
          System.err.println("No BeanMeta collected. Check classes directory.")
          System.exit(1)
        }
        writeMetas(outputPath, metas)
    }
  }

  /** 清单模式：以 --registrars 文件罗列的 MetaRegistrar 类（MappingModule/BindModule 等）为契约，
   * 全部找到并收集 BeanMeta 才算成功；清单为空时正常跳过。具体读取哪些类由调用方（sbt 插件）决定。
   */
  private def generateFromList(listFile: File, outputPath: String, classpath: Seq[String]): Unit = {
    if (!listFile.isFile) {
      logger.error(s"Registrars file does not exist: $listFile")
      System.err.println(s"Registrars file does not exist: $listFile")
      System.exit(1)
    }
    val classNames = readLines(listFile)
    if (classNames.isEmpty) {
      logger.info(s"No registrars declared in $listFile; beanmeta.idx generation skipped")
      return
    }
    val classLoader = new URLClassLoader(classpath.map(p => Path.of(p).toUri.toURL).toArray, getClass.getClassLoader)
    val metas = new mutable.ArrayBuffer[BeanMeta]
    val failures = new mutable.ListBuffer[String]
    try {
      classNames foreach { name =>
        Reflections.tryGetInstance[MetaRegistrar](name, classLoader) match {
          case Some(registrar) =>
            registrar.registering()
            val m = registrar.metas
            metas ++= m
            logger.info(s"Collected ${m.size} BeanMeta from $name")
          case None =>
            val clazzFound = findClass(name, classLoader)
            failures += (if clazzFound then s"$name is not a MetaRegistrar" else s"$name not found on classpath")
        }
      }
    } finally classLoader.close()
    if (failures.nonEmpty) {
      val msg = "Failed to load declared MetaRegistrar implementations:\n" + failures.mkString("\n")
      logger.error(msg)
      System.err.println(msg)
      System.exit(1)
    }
    writeMetas(outputPath, metas.toSeq)
  }

  /** 类或其 Scala object 伴生类（`$`）是否存在于 classpath。 */
  private def findClass(name: String, classLoader: ClassLoader): Boolean = {
    try {
      Class.forName(name, false, classLoader)
      true
    } catch {
      case _: ClassNotFoundException =>
        try {
          Class.forName(name + "$", false, classLoader)
          true
        } catch { case _: ClassNotFoundException => false }
    }
  }

  /** 读取清单文件：每行一个类名，# 开头为注释，忽略空行。 */
  private def readLines(file: File): Seq[String] = {
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
        logger.info(s"Generated $outputPath with ${metas.size} BeanMeta entries")
      }
    } finally {
      if (out ne System.out) out.close()
    }
  }

  /** Collects BeanMeta by scanning classes directories for MetaRegistrar subclasses. */
  def collectFromDirs(classesDirs: Seq[String]): Seq[BeanMeta] = {
    val allMetas = new mutable.ArrayBuffer[BeanMeta]

    classesDirs.foreach { dir =>
      val dirPath = Path.of(dir)
      if (!Files.isDirectory(dirPath)) {
        logger.warn(s"$dir is not a directory")
      } else {
        val metas = collectFromDir(dirPath)
        allMetas ++= metas
      }
    }

    allMetas.toSeq
  }

  /** Scans a single directory for MetaRegistrar subclasses. */
  private def collectFromDir(classesDir: Path): Seq[BeanMeta] = {
    val classFiles = findClassFiles(classesDir)
    if (classFiles.isEmpty) {
      logger.info(s"No .class files found in $classesDir")
      return Seq.empty
    }

    val classLoader = new URLClassLoader(Array(classesDir.toUri.toURL), getClass.getClassLoader)
    try {
      val allMetas = new mutable.ArrayBuffer[BeanMeta]
      var registryCount = 0

      classFiles.foreach { classFile =>
        try {
          val relativePath = classesDir.relativize(classFile)
          val className = relativePath.toString
            .replace(File.separatorChar, '/')
            .stripSuffix(".class")
            .replace('/', '.')

          Reflections.tryGetInstance[MetaRegistrar](className, classLoader) foreach { registrar =>
            registrar.registering()
            val metas = registrar.metas
            allMetas ++= metas
            registryCount += 1
            logger.info(s"Collected ${metas.size} BeanMeta from $className")
          }
        } catch {
          case _: ClassNotFoundException =>
          case _: NoClassDefFoundError =>
          case e: Exception =>
            logger.warn(s"Error loading ${classFile}: ${e.getMessage}")
        }
      }

      logger.info(s"Found $registryCount MetaRegistrar subclasses in $classesDir")
      allMetas.toSeq
    } finally classLoader.close()
  }

  /** Finds all .class files in a directory recursively. */
  private def findClassFiles(dir: Path): Seq[Path] = {
    val classFiles = new mutable.ListBuffer[Path]

    Files.walkFileTree(dir, new SimpleFileVisitor[Path] {
      override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
        if (file.toString.endsWith(".class")) {
          classFiles += file
        }
        FileVisitResult.CONTINUE
      }
    })

    classFiles.toSeq
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
            logger.error("Missing value for --registrars")
            System.err.println("Missing value for --registrars")
            printUsage()
            System.exit(1)
          }
        case "-h" | "--help" =>
          printUsage()
          System.exit(0)
        case arg if arg.startsWith("-") =>
          logger.error(s"Unknown option: $arg")
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
              |file, or by scanning classes directories.
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
              |  MetaGenerator target/classes target/test-classes
              |""".stripMargin)
  }
}
