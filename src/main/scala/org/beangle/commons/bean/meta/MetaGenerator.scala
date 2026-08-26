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
    val (classesDirs, outputPath) = parseArgs(args)

    if (classesDirs.isEmpty) {
      logger.error("No classes directory specified.")
      printUsage()
      System.exit(1)
    }

    val metas = collectFromDirs(classesDirs)

    if (metas.isEmpty) {
      logger.error("No BeanMeta collected. Check classes directory.")
      System.exit(1)
    }

    // Generate beanmeta.idx
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
  private def parseArgs(args: Array[String]): (Seq[String], String) = {
    var outputPath = DefaultOutputPath
    val classesDirs = new mutable.ListBuffer[String]

    var i = 0
    while (i < args.length) {
      args(i) match {
        case "-o" | "--output" =>
          i += 1
          if (i < args.length) outputPath = args(i)
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

    (classesDirs.toSeq, outputPath)
  }

  private def printUsage(): Unit = {
    println("""Usage: MetaGenerator [options] <classes-dir> [classes-dir...]
              |
              |Scans classes directories for MetaRegistrar subclasses and generates beanmeta.idx.
              |
              |Options:
              |  -o, --output <path>   Output path (default: META-INF/beangle/beanmeta.idx)
              |  -h, --help            Show this help
              |
              |Examples:
              |  MetaGenerator target/classes
              |  MetaGenerator -o output.idx target/classes
              |  MetaGenerator target/classes target/test-classes
              |""".stripMargin)
  }
}
