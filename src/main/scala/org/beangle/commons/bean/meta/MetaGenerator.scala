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

import org.beangle.commons.bean.meta.MetaModel.ClassMeta
import org.beangle.commons.json.{JsonArray, JsonObject}

import java.io.{File, FileOutputStream}
import java.net.URLClassLoader
import java.nio.charset.StandardCharsets
import java.nio.file.{FileVisitResult, Files, Path, SimpleFileVisitor}
import java.nio.file.attribute.BasicFileAttributes
import scala.collection.mutable

/** Tool for scanning MetaRegistry subclasses from classes directory and generating metamodel.idx.
  *
  * Usage:
  * {{{
  * // Scan classes directory and generate metamodel.idx
  * java -cp <classpath> org.beangle.commons.bean.meta.MetaGenerator /path/to/classes
  *
  * // Generate to custom output path
  * java -cp <classpath> org.beangle.commons.bean.meta.MetaGenerator -o /path/to/output.idx /path/to/classes
  *
  * // Also generate GraalVM native-image config files
  * java -cp <classpath> org.beangle.commons.bean.meta.MetaGenerator --graalvm /path/to/classes
  * }}}
  *
  * The tool scans the specified classes directory, finds all MetaRegistry subclasses,
  * collects their ClassMeta entries, and generates metamodel.idx file.
  */
object MetaGenerator {

  private val DefaultOutputPath = "META-INF/beangle/metamodel.idx"

  def main(args: Array[String]): Unit = {
    val (classesDirs, outputPath, graalvmMode) = parseArgs(args)

    if (classesDirs.isEmpty) {
      println("Error: No classes directory specified.")
      printUsage()
      System.exit(1)
    }

    val metas = collectFromDirs(classesDirs)

    if (metas.isEmpty) {
      println("No ClassMeta collected. Check classes directory.")
      System.exit(1)
    }

    // Generate metamodel.idx
    val out = if (outputPath == "-") System.out
    else {
      val path = Path.of(outputPath)
      Files.createDirectories(path.getParent)
      new FileOutputStream(path.toFile)
    }

    try {
      MetaIndex.write(out, metas)
      if (outputPath != "-") {
        println(s"Generated $outputPath with ${metas.size} ClassMeta entries")
      }
    } finally {
      if (out ne System.out) out.close()
    }

    // Generate GraalVM config files if requested
    if (graalvmMode && outputPath != "-") {
      val parentDir = Path.of(outputPath).getParent
      generateGraalvmConfigs(parentDir, outputPath, metas)
    }
  }

  /** Generates GraalVM native-image config files. */
  private def generateGraalvmConfigs(outputDir: Path, metamodelPath: String, metas: Seq[ClassMeta]): Unit = {
    // 1. Generate reflect-config.json
    val reflectPath = outputDir.resolve("reflect-config.json")
    generateReflectConfig(reflectPath, metas)
    println(s"Generated $reflectPath")

    // 2. Generate resource-config.json
    val resourcePath = outputDir.resolve("resource-config.json")
    generateResourceConfig(resourcePath, metamodelPath)
    println(s"Generated $resourcePath")
  }

  /** Generates reflect-config.json for GraalVM native-image. */
  private def generateReflectConfig(outputPath: Path, metas: Seq[ClassMeta]): Unit = {
    val entries = metas.map { cm =>
      JsonObject(
        "name" -> cm.clazz.getName,
        "allDeclaredFields" -> true,
        "allDeclaredConstructors" -> true,
        "allDeclaredMethods" -> true)
    }

    val json = JsonArray(entries).toJson
    Files.write(outputPath, json.getBytes(StandardCharsets.UTF_8))
  }

  /** Generates resource-config.json for GraalVM native-image. */
  private def generateResourceConfig(outputPath: Path, metamodelPath: String): Unit = {
    // Normalize path to use forward slashes for GraalVM
    val normalizedPath = metamodelPath.replace('\\', '/')

    val resources = JsonObject(
      "resources" -> JsonObject(
        "includes" -> JsonArray(
          JsonObject("pattern" -> "META-INF/beangle/metamodel\\.idx"),
          JsonObject("pattern" -> normalizedPath)
        ),
        "excludes" -> JsonArray()
      ),
      "bundles" -> JsonArray()
    )

    val json = resources.toJson
    Files.write(outputPath, json.getBytes(StandardCharsets.UTF_8))
  }

  /** Collects ClassMeta by scanning classes directories for MetaRegistry subclasses. */
  def collectFromDirs(classesDirs: Seq[String]): Seq[ClassMeta] = {
    val allMetas = new mutable.ArrayBuffer[ClassMeta]

    classesDirs.foreach { dir =>
      val dirPath = Path.of(dir)
      if (!Files.isDirectory(dirPath)) {
        println(s"Warning: $dir is not a directory")
      } else {
        val metas = collectFromDir(dirPath)
        allMetas ++= metas
      }
    }

    allMetas.toSeq
  }

  /** Scans a single directory for MetaRegistry subclasses. */
  private def collectFromDir(classesDir: Path): Seq[ClassMeta] = {
    val classFiles = findClassFiles(classesDir)
    if (classFiles.isEmpty) {
      println(s"No .class files found in $classesDir")
      return Seq.empty
    }

    // Create a URLClassLoader for the classes directory
    val classLoader = new URLClassLoader(Array(classesDir.toUri.toURL), getClass.getClassLoader)

    val allMetas = new mutable.ArrayBuffer[ClassMeta]
    var registryCount = 0

    classFiles.foreach { classFile =>
      try {
        val relativePath = classesDir.relativize(classFile)
        val className = relativePath.toString
          .replace(File.separatorChar, '/')
          .stripSuffix(".class")
          .replace('/', '.')

        val clazz = classLoader.loadClass(className)
        if (classOf[MetaRegistry].isAssignableFrom(clazz) && !clazz.isInterface) {
          val registry = clazz.getDeclaredConstructor().newInstance().asInstanceOf[MetaRegistry]
          val metas = registry.collect()
          allMetas ++= metas
          registryCount += 1
          println(s"Collected ${metas.size} ClassMeta from $className")
        }
      } catch {
        case _: ClassNotFoundException =>
        case _: NoClassDefFoundError =>
        case e: Exception =>
          println(s"Warning: Error loading ${classFile}: ${e.getMessage}")
      }
    }

    println(s"Found $registryCount MetaRegistry subclasses in $classesDir")
    allMetas.toSeq
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
  private def parseArgs(args: Array[String]): (Seq[String], String, Boolean) = {
    var outputPath = DefaultOutputPath
    var graalvmMode = false
    val classesDirs = new mutable.ListBuffer[String]

    var i = 0
    while (i < args.length) {
      args(i) match {
        case "-o" | "--output" =>
          i += 1
          if (i < args.length) outputPath = args(i)
        case "--graalvm" | "--reflect" =>
          graalvmMode = true
        case "-h" | "--help" =>
          printUsage()
          System.exit(0)
        case arg if arg.startsWith("-") =>
          println(s"Unknown option: $arg")
          printUsage()
          System.exit(1)
        case dir =>
          classesDirs += dir
      }
      i += 1
    }

    (classesDirs.toSeq, outputPath, graalvmMode)
  }

  private def printUsage(): Unit = {
    println("""Usage: MetaGenerator [options] <classes-dir> [classes-dir...]
              |
              |Scans classes directories for MetaRegistry subclasses and generates metamodel.idx.
              |
              |Options:
              |  -o, --output <path>   Output path (default: META-INF/beangle/metamodel.idx)
              |  --graalvm             Also generate GraalVM native-image config files:
              |                        - reflect-config.json (reflection metadata)
              |                        - resource-config.json (resource inclusion)
              |  -h, --help            Show this help
              |
              |Examples:
              |  MetaGenerator target/classes
              |  MetaGenerator -o output.idx target/classes
              |  MetaGenerator --graalvm target/classes
              |  MetaGenerator target/classes target/test-classes
              |""".stripMargin)
  }
}
