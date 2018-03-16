/*
 * Beangle, Agile Development Scaffold and Toolkits.
 *
 * Copyright © 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.commons.lang.reflect

import java.beans.Transient
import java.lang.Character.isUpperCase
import java.lang.reflect.{ Method, Modifier, ParameterizedType, TypeVariable }

import scala.collection.mutable
import scala.language.existentials
import scala.reflect.runtime.{ universe => ru }

import org.beangle.commons.collection.{ Collections, IdentityCache }
import org.beangle.commons.lang.{ ClassLoaders, Strings }
import org.beangle.commons.lang.Strings.{ substringAfter, substringBefore, uncapitalize }
import org.beangle.commons.lang.reflect.Reflections.deduceParamTypes

object BeanInfos {
  /**
   * Ignore scala case class methods
   */
  private val ignores = Set("hashCode", "toString", "productArity", "productPrefix", "productIterator")

  val Default = new BeanInfos()

  def get(clazz: Class[_]): BeanInfo = {
    Default.get(clazz)
  }
  def get(obj: Any): BeanInfo = {
    Default.get(obj.getClass, null)
  }
  def get(clazz: Class[_], typ: ru.Type): BeanInfo = {
    Default.get(clazz, typ)
  }
  def forType[T](clazz: Class[T])(implicit ttag: ru.TypeTag[T] = null, manifest: Manifest[T]): BeanInfo = {
    Default.forType(clazz)(ttag, manifest)
  }
}

class BeanInfos {
  private val cache = new IdentityCache[Class[_], BeanInfo]

  def clear() {
    val i = cache.clear()
  }

  def forType[T](clazz: Class[T])(implicit ttag: ru.TypeTag[T] = null, manifest: Manifest[T]): BeanInfo = {
    assert(null != ttag && manifest.runtimeClass != classOf[Object])
    get(clazz, ttag.tpe)
  }

  def get(obj: Any): BeanInfo = {
    get(obj.getClass, null)
  }

  /**
   * Get BeanManifest from cache or load it by type.
   * It search from cache, when failure build it and put it into cache
   * PS.
   * DON'T using get(class,tye=null) style definition,for scala with invoke get(obj:Any) when invocation is get(someClass).
   * So Don't using default argument and argument overload together.
   */
  def get(clazz: Class[_]): BeanInfo = {
    get(clazz, null)
  }
  /**
   * Get BeanManifest from cache or load it by type.
   * It search from cache, when failure build it and put it into cache.
   */
  def get(clazz: Class[_], tpe: ru.Type): BeanInfo = {
    var exist = cache.get(clazz)
    if (null == exist || (null != tpe && !exist.usingTypeReflection)) {
      exist = load(clazz, tpe)
      cache.put(clazz, exist)
    }
    exist
  }
  /**
   * Load BeanManifest using reflections
   */
  private def load(clazz: Class[_], tpe: ru.Type = null): BeanInfo = {
    if (clazz.getName.startsWith("java.") || clazz.getName.startsWith("scala.")) {
      throw new RuntimeException("Cannot reflect class:" + clazz.getName)
    }
    val getters = new mutable.HashMap[String, Getter]
    val setters = new mutable.HashMap[String, Setter]
    val fields = new mutable.HashSet[String]
    val interfaceSets = new mutable.HashSet[Class[_]]
    var nextClass = clazz
    var paramTypes: collection.Map[String, Class[_]] = Map.empty
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
      navIterface(nextClass, interfaceSets, getters, setters, paramTypes)

      val nextType = nextClass.getGenericSuperclass
      nextClass = nextClass.getSuperclass
      paramTypes = deduceParamTypes(nextClass, nextType, paramTypes)
    }

    val filterGetters = getters.map {
      case (name, getter) =>
        val logicTransient = !Modifier.isAbstract(getter.method.getModifiers) && !(setters.contains(name) || fields.contains(name))
        (name, Getter(getter.method, getter.returnType, getter.isTransient || logicTransient))
    }

    // organize setter and getter
    val allprops = filterGetters.keySet ++ setters.keySet
    val properties = Collections.newMap[String, PropertyDescriptor]
    allprops foreach { p =>
      val getter = filterGetters.get(p)
      val setter = setters.get(p)
      val clazz = if (None == getter) setter.get.parameterTypes(0) else getter.get.returnType

      val typeinfo =
        if (null != tpe) {
          typeof(clazz, tpe, p)
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
            val rawType = t.getRawType.asInstanceOf[Class[_]]
            if (TypeInfo.isCollectionType(rawType) && t.getActualTypeArguments.size == 1) {
              CollectionType(clazz, typeAt(t, 0))
            } else if (TypeInfo.isMapType(rawType) && t.getActualTypeArguments.size == 2) {
              MapType(clazz, typeAt(t, 0), typeAt(t, 1))
            } else {
              ElementType(rawType)
            }
          case _ => throw new RuntimeException("cannot process " + pt)
        }
      }
      ctors += new ConstructorDescriptor(ctor, infoes.toVector)
    }

    //find default constructor parameters
    val defaultConstructorParams: Map[Int, Any] =
      ClassLoaders.get(clazz.getName + "$") match {
        case Some(oclazz) =>
          val osinglton = oclazz.getDeclaredField("MODULE$").get(null)
          val params = Collections.newMap[Int, Any]
          oclazz.getDeclaredMethods foreach { m =>
            val index = Strings.substringAfter(m.getName, "$lessinit$greater$default$")
            if (Strings.isNotEmpty(index)) params.put(Integer.parseInt(index), m.invoke(osinglton))
          }
          params.toMap
        case None => Map.empty
      }

    // change accessible for none public class
    if (!Modifier.isPublic(clazz.getModifiers)) {
      properties foreach {
        case (k, v) =>
          v.getter.foreach { m => m.setAccessible(true) }
          v.setter.foreach { m => m.setAccessible(true) }
      }
    }
    new BeanInfo(properties.toMap, ctors.toList, defaultConstructorParams, null != tpe)
  }

  private def navIterface(clazz: Class[_], interfaceSets: mutable.HashSet[Class[_]],
                          getters: mutable.HashMap[String, Getter], setters: mutable.HashMap[String, Setter],
                          paramTypes: collection.Map[String, Class[_]]): Unit = {
    if (null == clazz || classOf[AnyRef] == clazz) return ;
    val interfaceTypes = clazz.getGenericInterfaces
    val interfaces = clazz.getInterfaces
    (0 until interfaceTypes.length) foreach { i =>
      if (!interfaceSets.contains(interfaces(i))) {
        interfaceSets.add(interfaces(i))
        val interfaceParamTypes = deduceParamTypes(interfaces(i), interfaceTypes(i), paramTypes)
        val interface = interfaces(i)
        val declaredMethods = interface.getDeclaredMethods
        (0 until declaredMethods.length) foreach { i =>
          val method = declaredMethods(i)
          findAccessor(method) match {
            case Some(Tuple2(readable, name)) =>
              if (readable) {
                if (!getters.contains(name))
                  getters.put(name, Getter(method, extract(method.getGenericReturnType, interfaceParamTypes), method.isAnnotationPresent(classOf[Transient])))
              } else {
                if (!setters.contains(name)) {
                  val types = method.getGenericParameterTypes
                  val paramsTypes = new Array[Class[_]](types.length)
                  (0 until types.length) foreach { j => paramsTypes(j) = extract(types(j), interfaceParamTypes) }
                  setters.put(name, Setter(method, paramsTypes))
                }
              }
            case None =>
          }
        }
        navIterface(interface, interfaceSets, getters, setters, paramTypes)
      }
    }
  }
  /**
   * Get TypeInfo using scala type
   */
  private def typeof(clazz: Class[_], typ: ru.Type, name: String): TypeInfo = {
    if (clazz == classOf[Object]) {
      var typeName = typ.member(ru.TermName(name)).typeSignatureIn(typ).erasure.toString
      ElementType(ClassLoaders.load(Strings.replace(typeName, "()", "")), false)
    } else if (clazz == classOf[Option[_]]) {
      val a = typ.member(ru.TermName(name)).typeSignatureIn(typ)
      val innerType = a.resultType.typeArgs.head.toString
      ElementType(ClassLoaders.load(innerType), true)
    } else if (TypeInfo.isCollectionType(clazz)) {
      if (clazz.isArray) {
        CollectionType(clazz, clazz.getComponentType)
      } else {
        val typeSignature = typ.member(ru.TermName(name)).typeSignatureIn(typ).toString
        val elementName = Strings.substringBetween(typeSignature, "[", "]")
        CollectionType(clazz, ClassLoaders.load(elementName))
      }
    } else if (TypeInfo.isMapType(clazz)) {
      val typeSignature = typ.member(ru.TermName(name)).typeSignatureIn(typ).toString
      val kvtype = Strings.substringBetween(typeSignature, "[", "]")
      val mapKeyType = Strings.substringBefore(kvtype, ",").trim
      val mapEleType = Strings.substringAfter(kvtype, ",").trim
      MapType(clazz, ClassLoaders.load(mapKeyType), ClassLoaders.load(mapEleType))
    } else {
      ElementType(clazz, false)
    }
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
    if (name.contains("$") && !name.contains("_$eq") || name.startsWith("_") || BeanInfos.ignores.contains(name)) return None

    val parameterTypes = method.getParameterTypes
    if (0 == parameterTypes.length && method.getReturnType != classOf[Unit]) {
      val propertyName =
        if (name.startsWith("get") && name.length > 3 && isUpperCase(name.charAt(3))) javaBeanPropertyName(name, 3)
        else if (name.startsWith("is") && name.length > 2 && isUpperCase(name.charAt(2))) javaBeanPropertyName(name, 2)
        else name
      Some((true, propertyName))
    } else if (1 == parameterTypes.length) {
      val propertyName =
        if (name.startsWith("set") && name.length > 3 && isUpperCase(name.charAt(3))) javaBeanPropertyName(name, 3)
        else if (name.endsWith("_$eq")) substringBefore(name, "_$eq")
        else null

      if (null != propertyName && !propertyName.contains("$")) Some((false, propertyName)) else None
    } else None
  }

  private def javaBeanPropertyName(n: String, methodIndex: Int): String = {
    val a = n.substring(methodIndex)
    if (isUpperCase(a.charAt(0))) {
      if (a.length > 1 && isUpperCase(a.charAt(1))) a else uncapitalize(a)
    } else {
      a
    }
  }
}
