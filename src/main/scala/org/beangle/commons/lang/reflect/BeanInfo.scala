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

package org.beangle.commons.lang.reflect

import org.beangle.commons.collection.Collections
import org.beangle.commons.lang.Strings.*
import org.beangle.commons.lang.reflect.BeanInfo.*

import java.lang.Character.isUpperCase
import java.lang.reflect.{Method, Modifier}
import scala.collection.immutable.ArraySeq
import scala.collection.mutable

object BeanInfo {

  /** Ignore java object and scala case class methods
   */
  private val ignores = Set("hashCode", "toString", "wait", "clone", "equals", "getClass", "notify", "notifyAll") ++
    Set("apply", "unApply", "canEqual")
  private val caseIgnores = Set("productArity", "productIterator", "productPrefix", "productElement", "productElementName", "productElementNames", "copy")

  case class PropertyInfo(name: String, typeinfo: TypeInfo, getter: Option[Method], setter: Option[Method], isTransient: Boolean) {
    def writable: Boolean = setter.isDefined

    def clazz: Class[_] = typeinfo.clazz

    def readable: Boolean = getter.isDefined

    override def toString: String = {
      if writable && readable then s"var $name: $typeinfo = _ "
      else if readable then s"def $name: $typeinfo"
      else s"def ${name}_=(x1: $typeinfo)"
    }
  }

  case class ParamInfo(name: String, typeinfo: TypeInfo, defaultValue: Option[Any])

  case class MethodInfo(method: Method, returnType: TypeInfo, parameters: ArraySeq[ParamInfo])
    extends Ordered[MethodInfo] {

    override def compare(o: MethodInfo): Int = this.method.getName.compareTo(o.method.getName)

    /** check this method if is preferred over given method
     *
     * @param o
     * @return
     */
    def isOver(o: MethodInfo): Boolean = {
      if o != this && o.method.getName == this.method.getName && o.parameters.size == this.parameters.size then
        //primary type over Object,but Object.isAssignableFrom(Int) is false
        if o.method.getReturnType == classOf[AnyRef] || o.method.getReturnType.isAssignableFrom(this.method.getReturnType) then
          val ps = o.method.getParameterTypes
          val paramTypeMatch = parameters.indices.forall { i => ps(i) == classOf[AnyRef] || ps(i).isAssignableFrom(parameters(i).typeinfo.clazz) }
          if paramTypeMatch then
            o.method.isBridge || o.method.getDeclaringClass.isAssignableFrom(this.method.getDeclaringClass)
          else false
        else false
      else false
    }

    override def toString: String = {
      val params = parameters.map(x => x.name + ": " + x.typeinfo).mkString(",")
      s"def ${method.getName}(${params}): ${returnType}"
    }

    def matches(args: Any*): Boolean = {
      if (parameters.length != args.length) return false
      !(0 until args.length).exists { i =>
        null != args(i) && !parameters(i).typeinfo.clazz.isInstance(args(i))
      }
    }
  }

  case class ConstructorInfo(parameters: ArraySeq[ParamInfo]) {
    override def toString: String = {
      val params = parameters.map { x =>
        x.name + ": " + x.typeinfo + (if x.defaultValue.nonEmpty then " = " + x.defaultValue.get.toString else "")
      }
      s"def this(${params.mkString(",")})"
    }
  }

  object Builder {
    def isTransient(transientAnnotated: Boolean, hasSetter: Boolean, usedInPrimaryCtor: Boolean): Boolean = {
      if transientAnnotated then true else !usedInPrimaryCtor && !hasSetter
    }

    def isCaseMethod(isCase: Boolean, name: String): Boolean = {
      isCase && caseIgnores.contains(name)
    }

    def isFineMethod(isCase: Boolean, method: Method, allowBridge: Boolean = false): Boolean = {
      val modifiers = method.getModifiers
      val name = method.getName
      val ignored = BeanInfo.ignores.contains(name) || isCaseMethod(isCase, name)
      val modifierNice = !Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers)
      !ignored && modifierNice && isFineMethodName(name) && (!method.isBridge || allowBridge)
    }

    def isFineMethodName(name: String): Boolean = {
      if name.startsWith("_") then false
      else if name.endsWith("_$eq") then !name.substring(0, name.length - 4).contains("$")
      else !name.contains("$")
    }

    def isSignatureMatchable(method: Method, methodInfo: (TypeInfo, ArraySeq[ParamInfo])): Boolean = {
      if classOf[AnyRef] != method.getReturnType && !method.getReturnType.isAssignableFrom(methodInfo._1.clazz) then false
      else {
        val ps = method.getParameterTypes
        val types = methodInfo._2
        types.size == ps.length &&
          (0 until ps.length).forall { i => ps(i) == classOf[AnyRef] || ps(i).isAssignableFrom(types(i).typeinfo.clazz) }
      }
    }

    /** Return this method is property read method (true,name) or write method(false,name) or None.
     */
    def findAccessor(method: Method): Option[Tuple2[Boolean, String]] = {
      val name = method.getName
      val parameterTypes = method.getParameterTypes
      if (0 == parameterTypes.length && method.getReturnType != classOf[Unit]) {
        Some((true, getPropertyName(name, true)))
      } else if (1 == parameterTypes.length) {
        val propertyName = getPropertyName(name, false)
        if (null != propertyName && !propertyName.contains("$")) Some((false, propertyName)) else None
      } else None
    }

    def getPropertyName(name: String, getter: Boolean): String = {
      if (getter) {
        if (name.startsWith("get") && name.length > 3 && isUpperCase(name.charAt(3))) lower(name.substring(3))
        else if (name.startsWith("is") && name.length > 2 && isUpperCase(name.charAt(2))) lower(name.substring(2))
        else name
      } else {
        if (name.startsWith("set") && name.length > 3 && isUpperCase(name.charAt(3))) lower(name.substring(3))
        else if (name.endsWith("_$eq")) substringBefore(name, "_$eq")
        else if (name.endsWith("_=")) substringBefore(name, "_=")
        else null
      }
    }

    /** filter bridge method and superclass method with same name and compatible signature
     *
     * @param methods
     * @return
     */
    def filterSameNames(methods: Iterable[MethodInfo]): collection.Seq[MethodInfo] = {
      if (methods.size == 1) {
        methods.toSeq
      } else {
        val result = Collections.newBuffer[MethodInfo]
        val paramSizeMap = methods.groupBy(_.parameters.size)
        paramSizeMap.values foreach { ml =>
          val reminded = Collections.newBuffer(ml)
          ml foreach { mi =>
            reminded.find(x => x.isOver(mi)) foreach (_ => reminded -= mi)
          }
          result ++= reminded
        }
        result
      }
    }

    private def lower(name: String): String = {
      if (name.length > 1 && isUpperCase(name.charAt(1))) name else uncapitalize(name)
    }

    class ParamHolder(name: String, typeinfo: Any, defaultValue: Option[Any]) {
      def this(name: String, typeInfo: Any) = {
        this(name, typeInfo, None)
      }

      def toParamInfo: ParamInfo = {
        ParamInfo(name, TypeInfo.convert(typeinfo), defaultValue)
      }
    }

  }

  import Builder.*

  /** ClassInfo Builder
   *
   * @param clazz
   */
  class Builder(val clazz: Class[_]) {
    private val fieldInfos = new mutable.HashMap[String, TypeInfo]
    //head will be primary constructor
    private val ctorInfos = new mutable.ArrayBuffer[ArraySeq[ParamInfo]]

    private val transients = new mutable.HashSet[String]

    def addTransients(names: Array[String]): Unit = transients ++= names

    def addField(name: String, ti: Any): Unit = {
      val typeinfo = TypeInfo.convert(ti)
      fieldInfos.put(name, typeinfo)
    }

    def addCtor(paramInfos: Array[ParamHolder]): Unit = {
      ctorInfos.addOne(ArraySeq.from(paramInfos.map(_.toParamInfo)))
    }

    private def registerMethodInfo(methods: mutable.HashMap[String, mutable.Buffer[MethodInfo]], method: Method,
                                   returnType: TypeInfo, parameters: ArraySeq[ParamInfo]): Unit = {
      val methodInfo = MethodInfo(method, returnType, parameters)
      val sameNames = methods.getOrElseUpdate(method.getName, Collections.newBuffer[MethodInfo])
      sameNames += methodInfo
    }

    def build(): BeanInfo = {
      val getters = new mutable.HashMap[String, Method]
      val setters = new mutable.HashMap[String, Method]
      val accessMethods = new mutable.HashMap[String, mutable.Buffer[MethodInfo]] //getter and setter
      val methods = new mutable.ArrayBuffer[Method]
      val isCase = TypeInfo.isCaseClass(clazz)
      clazz.getMethods foreach { method =>
        if (isFineMethod(isCase, method, true)) {
          var added = false
          findAccessor(method) foreach { (readable, name) =>
            fieldInfos.get(name) foreach { field =>
              added = true
              if readable then
                getters.put(name, method)
                registerMethodInfo(accessMethods, method, field, ArraySeq.empty[ParamInfo])
              else
                setters.put(name, method)
                registerMethodInfo(accessMethods, method, TypeInfo.UnitType, ArraySeq(ParamInfo(name, field, None)))
            }
          }
          if !added then methods += method
        }
      }
      val nonPublic = !Modifier.isPublic(clazz.getModifiers)
      // make buffer to list and filter duplicated bridge methods
      accessMethods.foreach { case (x, sameNames) =>
        val nb = filterSameNames(sameNames)
        if getters.contains(x) && nb.size == 1 then
          getters.put(x, nb.head.method)
          if nonPublic then nb.head.method.setAccessible(true)
      }

      val pCtorParamNames = if ctorInfos.isEmpty then Set.empty else ctorInfos.head.map(_.name).toSet
      // organize setter and getter
      val properties = new mutable.HashMap[String, BeanInfo.PropertyInfo]
      (getters.keySet ++ setters.keySet) foreach { p =>
        fieldInfos.get(p) foreach { fieldInfo =>
          val getter = getters.get(p)
          val setter = setters.get(p)
          val isTransient = Builder.isTransient(transients.contains(p), setter.isDefined, pCtorParamNames.contains(p))
          val pd = BeanInfo.PropertyInfo(p, fieldInfo, getter, setter, isTransient)
          //validProperty(clazz,pd)
          properties.put(p, pd)
        }
      }

      //process constructors,first is primary
      val ctors = ctorInfos map ConstructorInfo.apply
      val groupMethods = methods.groupBy(_.getName).map(x => (x._1, ArraySeq.from(x._2)))
      BeanInfo(clazz, ArraySeq.from(ctors), properties.toMap, groupMethods)
    }
  }

  private def validProperty(holderClass: Class[_], pi: BeanInfo.PropertyInfo): Unit = {
    val propertyClazz = pi.typeinfo.clazz
    pi.getter foreach { g => require(g.getReturnType == propertyClazz, s"${holderClass.getName}.${pi.name}'s type is ${propertyClazz.getName},but get method return ${g.getReturnType.getName}") }
    pi.setter foreach { g => require(g.getParameterTypes()(0) == propertyClazz, s"${holderClass.getName}.${pi.name}'s type is ${propertyClazz.getName},but set method need ${g.getParameterTypes()(0).getName}") }
  }
}

case class BeanInfo(clazz: Class[_], ctors: ArraySeq[ConstructorInfo], properties: Map[String, PropertyInfo], methods: collection.Map[String, ArraySeq[Method]]) {

  override def toString: String = {
    val sb = new mutable.ArrayBuffer[String]
    val isCase = TypeInfo.isCaseClass(clazz)
    val fieldInCtor = if ctors.isEmpty then Set.empty else ctors.head.parameters.map(_.name).toSet
    if (ctors.isEmpty) {
      sb += s"class ${clazz.getName} {"
    } else {
      sb += s"${if isCase then "case " else ""}class ${clazz.getName}${replace(ctors.head.toString, "def this", "")} {"
      ctors.tail foreach { ctor =>
        sb += s"  ${ctor}"
      }
    }

    val displayed = new mutable.HashSet[Method]
    properties foreach { (name, pi) =>
      displayed ++= pi.getter
      displayed ++= pi.setter
      if (pi.setter.nonEmpty || !fieldInCtor.contains(name)) {
        sb += s"  ${pi}"
      }
    }
    sb += "}"
    sb.mkString("\n")
  }

  def getPropertyTypeInfo(property: String): Option[TypeInfo] = {
    properties.get(property) match {
      case Some(p) => Some(p.typeinfo)
      case None => None
    }
  }

  def getPropertyType(property: String): Option[Class[_]] = {
    properties.get(property).map(_.clazz)
  }

  def getGetter(property: String): Option[Method] = {
    properties.get(property) match {
      case Some(p) => p.getter
      case None => None
    }
  }

  def getSetter(property: String): Option[Method] = {
    properties.get(property) match {
      case Some(p) => p.setter
      case None => None
    }
  }

  def readables: Map[String, PropertyInfo] = properties.filter(x => x._2.readable)

  def writables: Map[String, PropertyInfo] = properties.filter(x => x._2.writable)
}
