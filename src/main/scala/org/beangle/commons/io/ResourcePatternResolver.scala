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

package org.beangle.commons.io

import org.beangle.commons.lang.{ClassLoaders, Strings}
import org.beangle.commons.net.Networks
import org.beangle.commons.regex.AntPathPattern
import org.beangle.commons.regex.AntPathPattern.isPattern

import java.io.File
import java.net.{JarURLConnection, URL}
import java.util.jar.JarFile

object ResourcePatternResolver {
  def getResources(locationPattern: String): List[URL] = {
    new ResourcePatternResolver().getResources(locationPattern)
  }
}

class ResourcePatternResolver(val loader: ResourceLoader = new ClasspathResourceLoader) extends ResourceResolver {

  /** Find all resources that match the given location pattern via the
   * Ant-style PathMatcher. Supports resources in jar files and zip files
   * and in the file system.
   */
  override def getResources(locationPattern: String): List[URL] = {
    val location =
      if (locationPattern.startsWith(ClasspathAllUrlPrefix) || locationPattern.startsWith(ClasspathUrlPrefix)) Strings.substringAfter(locationPattern, ":")
      else locationPattern
    if (isPattern(location)) {
      val rootDirPath = determineRootDir(locationPattern)
      val subPattern = new AntPathPattern(locationPattern.substring(rootDirPath.length))
      val rootDirResources = getResources(rootDirPath)
      val result = new collection.mutable.LinkedHashSet[URL]
      for (rootUrl <- rootDirResources) {
        if Jars.isJarURL(rootUrl) then result ++= doFindJarResources(rootUrl, subPattern)
        else result ++= doFindFileResources(rootUrl, subPattern)
      }
      result.toList
    } else {
      val path = if (location.startsWith("/")) location.substring(1) else location
      ClassLoaders.getResources(path)
    }
  }

  private def determineRootDir(location: String): String = {
    val prefixEnd = location.indexOf(":") + 1
    var rootDirEnd = location.length
    while (rootDirEnd > prefixEnd && isPattern(location.substring(prefixEnd, rootDirEnd)))
      rootDirEnd = location.lastIndexOf('/', rootDirEnd - 2) + 1
    if (rootDirEnd == 0) rootDirEnd = prefixEnd
    location.substring(0, rootDirEnd)
  }

  /** Find all resources in jar files that match the given location pattern
   * via the Ant-style PathMatcher.
   */
  protected def doFindJarResources(rootDirResource: URL, subPattern: AntPathPattern): collection.Set[URL] = {
    val con = rootDirResource.openConnection
    var jarFile: JarFile = null
    var rootEntryPath: String = null
    var newJarFile = false

    con match
      case jarCon: JarURLConnection =>
        Jars.useCachesIfNecessary(jarCon)
        jarFile = jarCon.getJarFile
        val jarEntry = jarCon.getJarEntry
        rootEntryPath = if (jarEntry != null) jarEntry.getName else ""
      case _ =>
        val urlFile = rootDirResource.getFile
        val separatorIndex = urlFile.indexOf(Jars.URLSeparator)
        if (separatorIndex != -1) {
          rootEntryPath = urlFile.substring(separatorIndex + Jars.URLSeparator.length)
          jarFile = Jars.getJarFile(urlFile.substring(0, separatorIndex))
        } else {
          rootEntryPath = ""
          jarFile = new JarFile(urlFile)
        }
        newJarFile = true

    try {
      if (!"".equals(rootEntryPath) && !rootEntryPath.endsWith("/")) rootEntryPath = rootEntryPath + "/"
      val result = new collection.mutable.LinkedHashSet[URL]
      val entries = jarFile.entries
      while (entries.hasMoreElements) {
        val entry = entries.nextElement
        val entryPath = entry.getName
        if (entryPath.startsWith(rootEntryPath)) {
          val relativePath = entryPath.substring(rootEntryPath.length)
          if (subPattern.matches(relativePath)) result.add(Networks.url(rootDirResource, relativePath))
        }
      }
      result
    } finally
      if (newJarFile) jarFile.close()
  }

  /** Find all resources in the file system that match the given location pattern
   * via the Ant-style PathMatcher.
   */
  protected def doFindFileResources(rootDirURL: URL, pattern: AntPathPattern): collection.Set[URL] =
    try {
      val rootDir = new File(rootDirURL.toURI).getAbsoluteFile
      if (!rootDir.exists || !rootDir.isDirectory || !rootDir.canRead) return Set.empty

      var fullPattern = Strings.replace(rootDir.getAbsolutePath, File.separator, "/")
      if (!pattern.text.startsWith("/")) fullPattern += "/"
      fullPattern = fullPattern + Strings.replace(pattern.text, File.separator, "/")
      val matchingFiles = new collection.mutable.LinkedHashSet[File]
      doRetrieveMatchingFiles(new AntPathPattern(fullPattern), rootDir, matchingFiles)

      val result = new collection.mutable.LinkedHashSet[URL]
      for (file <- matchingFiles) result.add(file.toURI.toURL)
      result
    } catch {
      case _: Throwable => Set.empty
    }

  /** Recursively retrieve files that match the given pattern,
   * adding them to the given result list.
   */
  protected def doRetrieveMatchingFiles(pattern: AntPathPattern, dir: File, result: collection.mutable.Set[File]): Unit = {
    val dirContents = dir.listFiles
    if (dirContents != null)
      for (content <- dirContents) {
        val currPath = Strings.replace(content.getAbsolutePath, File.separator, "/")
        if (content.isDirectory && pattern.matchStart(currPath + "/"))
          if (content.canRead) doRetrieveMatchingFiles(pattern, content, result)
        if (pattern.matches(currPath)) result.add(content)
      }
  }
}
