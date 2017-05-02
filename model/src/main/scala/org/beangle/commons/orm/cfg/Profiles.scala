package org.beangle.commons.orm.cfg

import java.io.IOException
import java.net.URL

import org.beangle.commons.config.Resources
import org.beangle.commons.lang.ClassLoaders
import org.beangle.commons.lang.Strings.{ replace, substringBetween, isEmpty, isNotEmpty, rightPad, substringBeforeLast, unCamel }
import org.beangle.commons.logging.Logging
import org.beangle.commons.orm.NamingPolicy
import org.beangle.commons.orm.MappingModule
import org.beangle.commons.lang.reflect.Reflections

class Profiles(resources: Resources) extends Logging {

  private val profiles = new collection.mutable.HashMap[String, MappingProfile]

  private val namings = new collection.mutable.HashMap[String, NamingPolicy]

  val modules = new collection.mutable.HashSet[MappingModule]

  namings.put("rails", new RailsNamingPolicy(this))

  for (url <- resources.paths) addConfig(url)
  if (!profiles.isEmpty) logger.info(s"Table name pattern: -> ${this.toString}")

  def addConfig(url: URL): Unit = {
    try {
      logger.debug(s"loading $url")
      val is = url.openStream()
      if (null != is) {
        val xml = scala.xml.XML.load(is)
        (xml \ "naming" \ "profile") foreach { ele => parseProfile(ele, null) }
        (xml \ "mapping") foreach { ele =>
          modules += Reflections.getInstance[MappingModule]((ele \ "@class").text)
        }
        is.close()
      }
      autoWire()
    } catch {
      case e: IOException => logger.error("property load error", e)
    }
  }

  def getSchema(clazz: Class[_]): Option[String] = {
    getProfile(clazz) match {
      case None => None
      case Some(profile) => {
        var schema = profile.schema
        val anno = profile.annotations find { ann =>
          clazz.getAnnotations() exists { annon =>
            if (ann.clazz.isAssignableFrom(annon.getClass())) {
              if (isNotEmpty(ann.value)) {
                try {
                  val method = annon.getClass().getMethod("value")
                  String.valueOf(method.invoke(annon)) == ann.value
                } catch {
                  case e: Throwable => {
                    Console.err.print("Annotation value needed:", ann.value, annon.getClass)
                    false
                  }
                }
              } else true
            } else false
          }
        }
        anno foreach (an => if (isNotEmpty(an.schema)) schema = Some(an.schema))
        schema
      }
    }
  }

  def getPrefix(clazz: Class[_]): String = {
    getProfile(clazz) match {
      case None => ""
      case Some(profile) => {
        var prefix = profile.prefix
        val anno = profile.annotations find { ann =>
          clazz.getAnnotations() exists { annon =>
            if (ann.clazz.isAssignableFrom(annon.getClass())) {
              if (isNotEmpty(ann.value)) {
                try {
                  val method = annon.getClass().getMethod("value")
                  String.valueOf(method.invoke(annon)) == ann.value
                } catch {
                  case e: Exception => {
                    Console.err.print("Annotation value needed:", ann.value, annon.getClass)
                    false
                  }
                }
              } else true
            } else false
          }
        }
        anno foreach (an => if (isNotEmpty(an.prefix)) prefix = an.prefix)
        if (isEmpty(prefix)) "" else prefix
      }
    }
  }

  def getNamingPolicy(clazz: Class[_]): Option[NamingPolicy] = {
    getProfile(clazz).map { p => p.naming }
  }

  def getProfile(clazz: Class[_]): Option[MappingProfile] = {
    var name = clazz.getName()
    var matched: Option[MappingProfile] = None
    while (isNotEmpty(name) && matched == None) {
      if (profiles.contains(name)) matched = Some(profiles(name))
      val len = name.length
      name = substringBeforeLast(name, ".")
      if (name.length() == len) name = ""
    }
    matched
  }

  /**
   * adjust parent relation by package name
   */
  private def autoWire(): Unit = {
    if (profiles.size > 1) {
      profiles.foreach {
        case (key, profile) =>
          var parentName = substringBeforeLast(key, ".")
          while (isNotEmpty(parentName) && null == profile.parent) {
            if (profiles.contains(parentName) && profile.packageName != parentName) {
              logger.debug(s"set ${profile.packageName}'s parent is $parentName")
              profile.parent = profiles(parentName)
            }
            val len = parentName.length
            parentName = substringBeforeLast(parentName, ".")
            if (parentName.length() == len) parentName = ""
          }
      }
    }
  }
  private def parseProfile(melem: scala.xml.Node, parent: MappingProfile): Unit = {
    val profile = new MappingProfile
    if (!(melem \ "@package").isEmpty) {
      profile.packageName = (melem \ "@package").text
      if (null != parent) profile.packageName = parent.packageName + "." + profile.packageName
    }
    (melem \ "class") foreach { anElem =>
      val clazz = ClassLoaders.load((anElem \ "@annotation").text)
      val value = (anElem \ "@value").text
      val annModule = new AnnotationModule(clazz, value)
      profile._annotations += annModule

      if (!(anElem \ "@schema").isEmpty) {
        annModule.schema = parseSchema((anElem \ "@schema").text)
      }
      if (!(anElem \ "@prefix").isEmpty) annModule.prefix = (anElem \ "@prefix").text
    }
    if (!(melem \ "@schema").isEmpty) {
      profile._schema = parseSchema((melem \ "@schema").text)
    }
    if (!(melem \ "@prefix").isEmpty) profile._prefix = (melem \ "@prefix").text
    val naming = if (!(melem \ "@naming").isEmpty) (melem \ "@naming").text else "rails"
    if (namings.contains(naming)) {
      profile.naming = namings(naming)
    } else {
      throw new RuntimeException("Cannot find naming policy :" + naming)
    }
    profiles.put(profile.packageName, profile)
    profile.parent = parent
    (melem \ "profile") foreach { child => parseProfile(child, profile) }
  }

  private def parseSchema(name: String): String = {
    if (isEmpty(name) || (-1 == name.indexOf('{'))) return name
    var newName = replace(name, "$", "")
    val propertyName = substringBetween(newName, "{", "}")
    val pv = System.getProperty(propertyName)
    replace(newName, "{" + propertyName + "}", if (pv == null) "" else pv)
  }

  override def toString: String = {
    if (profiles.isEmpty) return ""
    val maxlength = profiles.map(m => m._1.length).max
    val sb = new StringBuilder
    profiles foreach {
      case (packageName, profile) =>
        sb.append(rightPad(packageName, maxlength, ' ')).append(" : [")
          .append(profile.schema.getOrElse(""))
        sb.append(",").append(profile.prefix)
        //      if (!module.abbreviations.isEmpty()) {
        //        sb.append(" , ").append(module.abbreviations)
        //      }
        sb.append(']').append(';')
    }
    if (sb.length > 0) sb.deleteCharAt(sb.length - 1)
    sb.toString()
  }
}
