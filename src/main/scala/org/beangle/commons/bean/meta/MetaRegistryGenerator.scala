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
import org.beangle.commons.io.Resources
import org.beangle.commons.lang.ClassLoaders
import org.beangle.commons.lang.Strings

import java.io.{FileOutputStream, OutputStream}
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import scala.collection.mutable

/** Tool for scanning MetaRegistry subclasses and generating beaninfo.idx.
  *
  * Usage:
  * {{{
  * // Generate beaninfo.idx to default location (META-INF/beangle/beaninfo.idx)
  * java -cp <classpath> org.beangle.commons.bean.meta.MetaRegistryGenerator
  *
  * // Generate to custom output path
  * java -cp <classpath> org.beangle.commons.bean.meta.MetaRegistryGenerator /path/to/output.idx
  *
  * // Scan specific registry classes
  * java -cp <classpath> org.beangle.commons.bean.meta.MetaRegistryGenerator com.example.AppRegistry
  * }}}
  *
  * Registry discovery:
  * 1. Command-line arguments (if provided)
  * 2. META-INF/beangle/metaregistry.properties (registry.classes=comma-separated)
  * 3. Scans META-INF/beangle/metaregistry.idx (pre-built registry index)
  */
object MetaRegistryGenerator {

  private val DefaultOutputPath = "META-INF/beangle/beaninfo.idx"
  private val RegistryConfigPath = "META-INF/beangle/metaregistry.properties"

  def main(args: Array[String]): Unit = {
    val (registryClasses, outputPath) = parseArgs(args)
    val metas = collectAll(registryClasses)

    if (metas.isEmpty) {
      println("No ClassMeta collected. Check registry configuration.")
      System.exit(1)
    }

    val out = if (outputPath == "-") System.out
    else {
      val path = Paths.get(outputPath)
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
  }

  /** Collects ClassMeta from all specified or discovered registries. */
  def collectAll(registryClasses: Seq[String] = Seq.empty): Seq[ClassMeta] = {
    val classes = if (registryClasses.nonEmpty) registryClasses
    else discoverRegistries()

    if (classes.isEmpty) {
      println("No MetaRegistry subclasses found.")
      return Seq.empty
    }

    val allMetas = new mutable.ArrayBuffer[ClassMeta]
    classes.foreach { className =>
      try {
        val clazz = ClassLoaders.load(className)
        if (classOf[MetaRegistry].isAssignableFrom(clazz)) {
          val registry = clazz.getDeclaredConstructor().newInstance().asInstanceOf[MetaRegistry]
          val metas = registry.collect()
          allMetas ++= metas
          println(s"Collected ${metas.size} ClassMeta from $className")
        } else {
          println(s"Warning: $className is not a MetaRegistry subclass")
        }
      } catch {
        case e: Exception =>
          println(s"Error loading registry $className: ${e.getMessage}")
      }
    }
    allMetas.toSeq
  }

  /** Discovers registry classes from configuration file. */
  private def discoverRegistries(): Seq[String] = {
    val classes = new mutable.ListBuffer[String]

    // Load from properties file
    ClassLoaders.getResourceAsStream(RegistryConfigPath).foreach { is =>
      val props = new java.util.Properties()
      props.load(is)
      val classNames = props.getProperty("registry.classes", "")
      if (classNames.nonEmpty) {
        classes ++= Strings.split(classNames, ",").map(_.trim).filter(_.nonEmpty)
      }
    }

    classes.toSeq
  }

  /** Parses command-line arguments. */
  private def parseArgs(args: Array[String]): (Seq[String], String) = {
    var outputPath = DefaultOutputPath
    val registryClasses = new mutable.ListBuffer[String]

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
          println(s"Unknown option: $arg")
          printUsage()
          System.exit(1)
        case className =>
          registryClasses += className
      }
      i += 1
    }

    (registryClasses.toSeq, outputPath)
  }

  private def printUsage(): Unit = {
    println("""Usage: MetaRegistryGenerator [options] [registry-class...]
              |
              |Options:
              |  -o, --output <path>   Output path (default: META-INF/beangle/beaninfo.idx)
              |  -h, --help            Show this help
              |
              |If no registry classes are specified, reads from META-INF/beangle/metaregistry.properties
              |""".stripMargin)
  }
}
