package org.beangle.commons.io

import java.io.File
import java.lang.reflect.Method
import java.net.{ JarURLConnection, URL }
import java.util.jar.JarFile

import org.beangle.commons.lang.{ ClassLoaders, Strings }
import org.beangle.commons.logging.Logging
import org.beangle.commons.text.regex.AntPathPattern
import org.beangle.commons.text.regex.AntPathPattern.isPattern

trait ResourceResolver {

  val ClasspathAllUrlPrefix = "classpath*:"
  val ClasspathUrlPrefix = "classpath:"
  def getResources(locationPattern: String): List[URL]
}

class ResourcePatternResolver(val loader: ResourceLoader = new ClasspathResourceLoader) extends ResourceResolver with Logging {

  private val equinoxResolveMethod: Method = {
    try {
      // Detect Equinox OSGi (e.g. on WebSphere 6.1)
      ClassLoaders.loadClass("org.eclipse.core.runtime.FileLocator").getMethod("resolve", classOf[URL])
    } catch {
      case _: Throwable => null
    }
  }
  /**
   * Find all resources that match the given location pattern via the
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
      for (rootDir <- rootDirResources) {
        val rootUrl = resolve(rootDir)
        if (Jars.isJarURL(rootUrl)) {
          result ++= doFindJarResources(rootUrl, subPattern)
        } else {
          result ++= doFindFileResources(rootUrl, subPattern)
        }
      }
      result.toList
    } else {
      val path = if (location.startsWith("/")) location.substring(1) else location
      ClassLoaders.getResources(path)
    }
  }

  protected def determineRootDir(location: String): String = {
    val prefixEnd = location.indexOf(":") + 1
    var rootDirEnd = location.length
    while (rootDirEnd > prefixEnd && isPattern(location.substring(prefixEnd, rootDirEnd))) {
      rootDirEnd = location.lastIndexOf('/', rootDirEnd - 2) + 1
    }
    if (rootDirEnd == 0) rootDirEnd = prefixEnd
    location.substring(0, rootDirEnd)
  }

  /**
   * Resolve the specified resource for path matching.
   * <p>The default implementation detects an Equinox OSGi "bundleresource:"
   * / "bundleentry:" URL and resolves it into a standard jar file URL that
   * can be traversed using Spring's standard jar file traversal algorithm.
   */
  protected def resolve(url: URL): URL = {
    if (equinoxResolveMethod != null && url.getProtocol.startsWith("bundle")) {
      equinoxResolveMethod.invoke(null, url).asInstanceOf[URL]
    } else url
  }

  /**
   * Find all resources in jar files that match the given location pattern
   * via the Ant-style PathMatcher.
   */
  protected def doFindJarResources(rootDirResource: URL, subPattern: AntPathPattern): collection.Set[URL] = {
    val con = rootDirResource.openConnection
    var jarFile: JarFile = null
    var rootEntryPath: String = null
    var newJarFile = false

    if (con.isInstanceOf[JarURLConnection]) {
      val jarCon = con.asInstanceOf[JarURLConnection]
      Jars.useCachesIfNecessary(jarCon)
      jarFile = jarCon.getJarFile
      val jarEntry = jarCon.getJarEntry
      rootEntryPath = if (jarEntry != null) jarEntry.getName else ""
    } else {
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
    }

    try {
      if (!"".equals(rootEntryPath) && !rootEntryPath.endsWith("/")) rootEntryPath = rootEntryPath + "/"
      val result = new collection.mutable.LinkedHashSet[URL]
      val entries = jarFile.entries
      while (entries.hasMoreElements) {
        val entry = entries.nextElement
        val entryPath = entry.getName
        if (entryPath.startsWith(rootEntryPath)) {
          val relativePath = entryPath.substring(rootEntryPath.length)
          if (subPattern.matches(relativePath)) result.add(new URL(rootDirResource, relativePath))
        }
      }
      return result
    } finally {
      if (newJarFile) jarFile.close()
    }
  }

  /**
   * Find all resources in the file system that match the given location pattern
   * via the Ant-style PathMatcher.
   */
  protected def doFindFileResources(rootDirURL: URL, pattern: AntPathPattern): collection.Set[URL] = {
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
  }

  /**
   * Recursively retrieve files that match the given pattern,
   * adding them to the given result list.
   */
  protected def doRetrieveMatchingFiles(pattern: AntPathPattern, dir: File, result: collection.mutable.Set[File]): Unit = {
    val dirContents = dir.listFiles
    if (dirContents == null) return
    for (content <- dirContents) {
      val currPath = Strings.replace(content.getAbsolutePath, File.separator, "/")
      if (content.isDirectory && pattern.matchStart(currPath + "/")) {
        if (content.canRead) doRetrieveMatchingFiles(pattern, content, result)
      }
      if (pattern.matches(currPath)) result.add(content)
    }
  }
}
