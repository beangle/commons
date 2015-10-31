/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2015, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.commons.lang.reflect

import java.beans.Transient
import java.lang.Character.isUpperCase
import java.lang.reflect.{ Constructor, Method, Modifier, ParameterizedType, TypeVariable }
import scala.collection.mutable
import scala.language.existentials
import org.beangle.commons.collection.{ Collections, IdentityCache }
import org.beangle.commons.lang.{ ClassLoaders, Strings }
import org.beangle.commons.lang.Strings.{ substringAfter, substringBefore, uncapitalize }

case class Getter(val method: Method, val returnType: Class[_], val isTransient: Boolean)

case class Setter(val method: Method, val parameterTypes: Array[Class[_]])

sealed trait TypeInfo {
  def clazz: Class[_]
  def isElementType: Boolean = false
  def isCollectionType: Boolean = false
  def isMapType: Boolean = false
  def isSetType: Boolean = false
}

object TypeInfo {

  def of(clazz: Class[_]): TypeInfo = {
    of(clazz, clazz.getGenericSuperclass)
  }

  def of(clazz: Class[_], typ: java.lang.reflect.Type): TypeInfo = {
    import Reflections._
    if (CollectionType.isCollectionType(clazz) || classOf[java.util.Map[_, _]].isAssignableFrom(clazz)) {
      if (clazz.isArray) {
        CollectionType(clazz, clazz.getComponentType)
      } else {
        typ match {
          case pt: ParameterizedType =>
            if (pt.getActualTypeArguments.size == 1) CollectionType(clazz, typeAt(pt, 0))
            else MapType(clazz, typeAt(pt, 0), typeAt(pt, 1))
          case c: Class[_] => {
            if (classOf[collection.Map[_, _]].isAssignableFrom(clazz)) {
              val typeParams = getGenericParamType(c, classOf[collection.Map[_, _]])
              MapType(clazz, typeParams("A"), typeParams("B"))
            } else if (classOf[collection.Iterable[_]].isAssignableFrom(clazz)) {
              CollectionType(clazz, getGenericParamType(c, classOf[collection.Iterable[_]]).head._2)
            } else if (classOf[java.util.Collection[_]].isAssignableFrom(clazz)) {
              CollectionType(clazz, getGenericParamType(c, classOf[java.util.Collection[_]]).head._2)
            } else {
              val typeParams = getGenericParamType(c, classOf[java.util.Map[_, _]])
              MapType(clazz, typeParams("K"), typeParams("V"))
            }
          }
        }
      }
    } else {
      ElementType(clazz)
    }
  }

  def typeAt(typ: java.lang.reflect.Type, idx: Int): Class[_] = {
    typ match {
      case c: Class[_] => c
      case pt: ParameterizedType =>
        pt.getActualTypeArguments()(idx) match {
          case c: Class[_] => c
          case _           => classOf[AnyRef]
        }
      case _ => classOf[AnyRef]
    }
  }
}
case class ElementType(val clazz: Class[_]) extends TypeInfo {
  override def isElementType: Boolean = true
}

object CollectionType {
  def isCollectionType(clazz: Class[_]): Boolean = {
    classOf[collection.Iterable[_]].isAssignableFrom(clazz) || classOf[java.util.Collection[_]].isAssignableFrom(clazz) || clazz.isArray()
  }
}

case class CollectionType(val clazz: Class[_], val componentType: Class[_]) extends TypeInfo {
  override def isSetType: Boolean = {
    classOf[collection.Set[_]].isAssignableFrom(clazz) || classOf[java.util.Set[_]].isAssignableFrom(clazz)
  }
  override def isCollectionType: Boolean = true
}

case class MapType(val clazz: Class[_], val keyType: Class[_], valueType: Class[_]) extends TypeInfo {
  override def isMapType: Boolean = true
}

class PropertyDescriptor(val name: String, val typeinfo: TypeInfo, val getter: Option[Method], val setter: Option[Method], val isTransient: Boolean) {
  def writable: Boolean = {
    None != setter
  }

  def clazz: Class[_] = {
    typeinfo.clazz
  }

  def readable: Boolean = {
    None != getter
  }
}

class ConstructorDescriptor(val constructor: Constructor[_], val args: Vector[TypeInfo])

class BeanManifest(val properties: Map[String, PropertyDescriptor], val constructors: List[ConstructorDescriptor]) {

  def getGetter(property: String): Option[Method] = {
    properties.get(property) match {
      case Some(p) => p.getter
      case None    => None
    }
  }

  def getPropertyType(property: String): Option[Class[_]] = {
    properties.get(property) match {
      case Some(p) => Some(p.clazz)
      case None    => None
    }
  }

  def getSetter(property: String): Option[Method] = {
    properties.get(property) match {
      case Some(p) => p.setter
      case None    => None
    }
  }

  def readables: Map[String, PropertyDescriptor] = {
    properties.filter(p => p._2.readable)
  }
  def writables: Map[String, PropertyDescriptor] = {
    properties.filter(p => p._2.writable)
  }

  def getWritableProperties(): Set[String] = {
    properties.filter(e => e._2.writable).keySet
  }
}

object BeanManifest {

  private val cache = new IdentityCache[Class[_], BeanManifest]
  /**
   * Support scala case class
   */
  private val ignores = Set("hashCode", "toString", "productArity", "productPrefix", "productIterator")

  import scala.reflect.runtime.{ universe => ru }
  def forType[T](clazz: Class[T])(implicit ttag: ru.TypeTag[T] = null, manifest: Manifest[T]): BeanManifest = {
    assert(null != ttag && manifest.runtimeClass != classOf[Object])
    get(clazz, ttag.tpe)
  }

  def get(obj: Any): BeanManifest = {
    get(obj.getClass, null)
  }

  /**
   * Get BeanManifest from cache or load it by type.
   * It search from cache, when failure build it and put it into cache
   * PS.
   * DON'T using get(class,tye=null) style definition,for scala with invoke get(obj:Any) when invocation is get(someClass).
   * So Don't using default argument and argument overload together.
   */
  def get(clazz: Class[_]): BeanManifest = {
    get(clazz, null)
  }
  /**
   * Get BeanManifest from cache or load it by type.
   * It search from cache, when failure build it and put it into cache.
   */
  def get(clazz: Class[_], tpe: ru.Type): BeanManifest = {
    var exist = cache.get(clazz)
    if (null == exist) {
      exist = load(clazz, tpe)
      cache.put(clazz, exist)
    }
    exist
  }
  /**
   * Load BeanManifest using reflections
   */
  def load(clazz: Class[_], tpe: ru.Type = null): BeanManifest = {
    val getters = new mutable.HashMap[String, Getter]
    val setters = new mutable.HashMap[String, Setter]
    var nextClass = clazz
    var paramTypes: collection.Map[String, Class[_]] = Map.empty
    val fields = new mutable.HashSet[String]
    while (null != nextClass && classOf[AnyRef] != nextClass) {
      val declaredMethods = nextClass.getDeclaredMethods
      nextClass.getDeclaredFields() foreach { f => fields += f.getName }
      (0 until declaredMethods.length) foreach { i =>
        val method = declaredMethods(i)
        findAccessor(method) match {
          case Some(Tuple2(readable, name)) =>
            if (readable) {
              getters.put(name, Getter(method, extract(method.getGenericReturnType, paramTypes), method.isAnnotationPresent(classOf[Transient])))
            } else {
              val types = method.getGenericParameterTypes
              val paramsTypes = new Array[Class[_]](types.length)
              (0 until types.length) foreach { j => paramsTypes(j) = extract(types(j), paramTypes) }
              setters.put(name, Setter(method, paramsTypes))
            }
          case None =>
        }
      }
      val nextType = nextClass.getGenericSuperclass
      nextClass = nextClass.getSuperclass
      paramTypes = nextType match {
        case ptSuper: ParameterizedType =>
          val tmp = new mutable.HashMap[String, Class[_]]
          val ps = ptSuper.getActualTypeArguments
          val tvs = nextClass.getTypeParameters
          (0 until ps.length) foreach { k =>
            tmp.put(tvs(k).getName,
              ps(k) match {
                case c: Class[_]           => c
                case tv: TypeVariable[_]   => paramTypes(tv.getName)
                case pt: ParameterizedType => pt.getRawType.asInstanceOf[Class[_]]
              })
          }
          tmp
        case _ => Map.empty
      }
    }

    val filterGetters = getters.map {
      case (name, getter) =>
        (name, Getter(getter.method, getter.returnType, getter.isTransient || !(setters.contains(name) || fields.contains(name))))
    }

    if (Modifier.isAbstract(clazz.getModifiers) || clazz.isInterface) {
      clazz.getMethods foreach { method =>
        if (Modifier.isAbstract(method.getModifiers)) {
          findAccessor(method) foreach {
            case (readable, name) =>
              if (readable) {
                filterGetters.put(name, Getter(method, method.getReturnType, method.isAnnotationPresent(classOf[Transient])))
              } else {
                setters.put(name, Setter(method, method.getParameterTypes))
              }
          }
        }
      }
    }

    // organize setter and getter
    val allprops = filterGetters.keySet ++ setters.keySet
    val properties = Collections.newMap[String, PropertyDescriptor]
    allprops foreach { p =>
      val getter = filterGetters.get(p)
      val setter = setters.get(p)
      var clazz = if (None == getter) setter.get.parameterTypes(0) else getter.get.returnType

      val typeinfo =
        if (clazz == classOf[Object] && null != tpe) {
          var typeName = tpe.member(ru.TermName(p)).typeSignatureIn(tpe).erasure.toString
          typeName = Strings.replace(typeName, "()", "")
          clazz = ClassLoaders.load(typeName)
          ElementType(clazz)
        } else {
          TypeInfo.of(clazz, if (None == getter) setter.get.method.getGenericParameterTypes()(0) else getter.get.method.getGenericReturnType)
        }

      val isTrasient = if (None == getter) false else getter.get.isTransient
      val pd = new PropertyDescriptor(p, typeinfo, getter.map(x => x.method), setter.map(x => x.method), isTrasient)
      properties.put(p, pd)
    }

    // find constructor with arguments
    val ctors = Collections.newBuffer[ConstructorDescriptor]
    import TypeInfo._
    clazz.getConstructors() foreach { ctor =>
      val infoes: Array[TypeInfo] = ctor.getGenericParameterTypes map { pt =>
        pt match {
          case c: Class[_] => ElementType(c)
          case t: ParameterizedType =>
            if (t.getActualTypeArguments().size == 1) CollectionType(clazz, typeAt(t, 0))
            else MapType(clazz, typeAt(t, 0), typeAt(t, 1))
          case _ => throw new RuntimeException("cannot process " + pt)
        }
      }
      ctors += new ConstructorDescriptor(ctor, infoes.toVector)
    }
    new BeanManifest(properties.toMap, ctors.toList)
  }

  private def extract(t: java.lang.reflect.Type, types: collection.Map[String, Class[_]]): Class[_] = {
    t match {
      case pt: ParameterizedType => pt.getRawType.asInstanceOf[Class[_]]
      case tv: TypeVariable[_]   => types.get(tv.getName).getOrElse(classOf[AnyRef])
      case c: Class[_]           => c
      case _                     => classOf[AnyRef]
    }
  }

  /**
   * Return this method is property read method (true,name) or write method(false,name) or None.
   */
  private def findAccessor(method: Method): Option[Tuple2[Boolean, String]] = {
    val modifiers = method.getModifiers
    if (Modifier.isStatic(modifiers) || !Modifier.isPublic(modifiers) || method.isBridge) return None

    val name = method.getName
    if (name.contains("$") && !name.contains("_$eq") || ignores.contains(name)) return None

    val parameterTypes = method.getParameterTypes
    if (0 == parameterTypes.length && method.getReturnType != classOf[Unit]) {
      val propertyName =
        if (name.startsWith("get") && name.length > 3 && isUpperCase(name.charAt(3))) uncapitalize(substringAfter(name, "get"))
        else if (name.startsWith("is") && name.length > 2 && isUpperCase(name.charAt(2))) uncapitalize(substringAfter(name, "is"))
        else name
      Some((true, propertyName))
    } else if (1 == parameterTypes.length) {
      val propertyName =
        if (name.startsWith("set") && name.length > 3 && isUpperCase(name.charAt(3))) uncapitalize(substringAfter(name, "set"))
        else if (name.endsWith("_$eq")) substringBefore(name, "_$eq")
        else null

      if (null != propertyName && !propertyName.contains("$")) Some((false, propertyName)) else None
    } else None
  }
}
