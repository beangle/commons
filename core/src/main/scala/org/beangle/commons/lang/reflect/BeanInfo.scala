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
import org.beangle.commons.logging.Logging

import java.lang.Character.isUpperCase
import java.lang.reflect.{Constructor, Field, Method, Modifier}
import scala.collection.immutable.ArraySeq
import scala.collection.mutable
import BeanInfo.*
import scala.quoted.*

object BeanInfo extends Logging {

  /**
    * Ignore java object and scala case class methods
    */
  val ignores = Set("hashCode", "toString", "wait", "clone", "equals", "getClass", "notify", "notifyAll") ++
    Set("productArity", "productPrefix", "productElement", "productElementName", "fromProduct", "apply", "unApply", "canEquals")

  case class PropertyInfo(name: String, typeinfo: TypeInfo, getter: Option[Method], setter: Option[Method],isTransient: Boolean) {
    def writable: Boolean = setter.isDefined

    def clazz: Class[_] = typeinfo.clazz

    def readable: Boolean = getter.isDefined

    override def toString: String = {
      if writable && readable then s"var ${name}: ${typeinfo} = _ "
      else if readable then s"def ${name}: ${typeinfo}"
      else s"def ${name}_=(x1: ${typeinfo})"
    }
  }

  case class ParamInfo(name: String, typeinfo: TypeInfo, defaultValue: Option[Any])

  case class MethodInfo(method: Method, returnType: TypeInfo, parameters: ArraySeq[ParamInfo])
    extends Ordered[MethodInfo] {

    override def compare(o: MethodInfo): Int = this.method.getName.compareTo(o.method.getName)

    override def toString(): String = {
      val params = parameters.map(x => x.name + ": " + x.typeinfo).mkString(",")
      s"def ${method.getName}(${params}): ${returnType}"
    }

    def matches(args: Any*): Boolean = {
      if (parameters.length != args.length) return false
      (0 until args.length).find { i =>
        null != args(i) && !parameters(i).typeinfo.clazz.isInstance(args(i))
      }.isEmpty
    }
  }

  case class ConstructorInfo(parameters: ArraySeq[ParamInfo]){
    override def toString(): String = {
      val params = parameters.map{x =>
        x.name + ": " + x.typeinfo + (if x.defaultValue.nonEmpty then " = " + x.defaultValue.get.toString else "")
      }
      s"def this(${params.mkString(",")})"
    }
  }

  object Builder{
    def isTransient(transientAnnotated:Boolean, hasSetter:Boolean):Boolean={
      if transientAnnotated then true else !hasSetter
    }

    def isFineMethod(method: Method,allowBridge:Boolean=false): Boolean = {
      val modifiers = method.getModifiers
      val name=method.getName
      val ignored = BeanInfo.ignores.contains(name)
      val modifierNice = !Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers)
      !ignored && modifierNice && !(name.contains("$") && !name.contains("_$eq") || name.startsWith("_")) && (!method.isBridge || allowBridge)
    }

    def isSignatureMatchable(method: Method, methodInfo: (TypeInfo, ArraySeq[ParamInfo])): Boolean = {
      if classOf[AnyRef] !=method.getReturnType && !method.getReturnType.isAssignableFrom(methodInfo._1.clazz) then false
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
        Some((true, getPropertyName(name,true)))
      } else if (1 == parameterTypes.length) {
        val propertyName =  getPropertyName(name,false)
        if (null != propertyName && !propertyName.contains("$")) Some((false, propertyName)) else None
      } else None
    }

    def getPropertyName(name: String, getter:Boolean): String = {
      if(getter){
        if (name.startsWith("get") && name.length > 3 && isUpperCase(name.charAt(3))) lower(name.substring(3))
        else if (name.startsWith("is") && name.length > 2 && isUpperCase(name.charAt(2))) lower(name.substring(2))
        else name
      }else{
        if (name.startsWith("set") && name.length > 3 && isUpperCase(name.charAt(3))) lower(name.substring(3))
        else if (name.endsWith("_$eq")) substringBefore(name, "_$eq")
        else null
      }
    }

    private def lower(name:String):String={
      if (name.length > 1 && isUpperCase(name.charAt(1))) name else uncapitalize(name)
    }
  }
  import Builder.*
  /** ClassInfo Builder
    *
    * @param clazz
    */
  class Builder(val clazz: Class[_]) {
    private val fieldInfos = new mutable.HashMap[String, TypeInfo]
    //Duplication may occurs due to bridge methods in class hirarchy.
    private val methodInfos = new mutable.HashMap[String, mutable.Buffer[(TypeInfo, ArraySeq[ParamInfo])]]
    private val ctorInfos = new mutable.ArrayBuffer[ArraySeq[ParamInfo]]

    private val transnts = new mutable.HashSet[String]

    def addField(name: String, typeinfo: TypeInfo,transnt:Boolean): Unit = {
      fieldInfos.put(name, typeinfo)
      if(transnt)transnts += name
      //for scala macro quoted api doesn't provider field read method
      addMethod(name, typeinfo, List.empty, false)
    }

    def addMethod(name: String, returnTypeInfo: TypeInfo, paramInfos: collection.Seq[ParamInfo], asField: Boolean = false): Unit = {
      val jvmName = replace(name, "=", "$eq")
      val sameNames = methodInfos.getOrElseUpdate(jvmName, new mutable.ArrayBuffer[(TypeInfo, ArraySeq[ParamInfo])])
      sameNames += Tuple2(returnTypeInfo, ArraySeq.from(paramInfos))
      if (asField) fieldInfos.put(getPropertyName(jvmName,true), returnTypeInfo)
    }

    def addCtor(paramInfos: collection.Seq[ParamInfo]): Unit = {
      ctorInfos.addOne(ArraySeq.from(paramInfos))
    }

    def build(): BeanInfo = {
      val getters = new mutable.HashMap[String, Method]
      val setters = new mutable.HashMap[String, Method]
      val methods = new mutable.HashMap[String, mutable.Buffer[MethodInfo]]
      clazz.getMethods foreach { method =>
        if (isFineMethod(method,true)) {
          findAccessor(method) foreach { (readable, name) =>
            if (readable) then getters.put(name, method) else setters.put(name, method)
          }
          methodInfos(method.getName).find(isSignatureMatchable(method, _))match{
            case Some(mi)=>
              val methodInfo = MethodInfo(method, mi._1, mi._2)
              val sameNames = methods.getOrElseUpdate(method.getName, Collections.newBuffer[MethodInfo])
              sameNames += methodInfo
            case None=>
              logger.error("cannot find method info of " + method.getName + " and candinate is " + methodInfos(method.getName))
          }
        }
      }
      // make buffer to list and filter duplicated bridge methods
      val filterMethods = methods.map { case (x, sameNames) =>
        val partition = sameNames.partition(_.method.isBridge)
        if partition._1.isEmpty then (x, ArraySeq.from(sameNames)) else {
          val nb = partition._2
          partition._1 foreach { mi =>
            if !nb.exists(nbmi => isSignatureMatchable(mi.method, (nbmi.returnType, nbmi.parameters))) then nb += mi
          }
          (x, ArraySeq.from(nb))
        }
      }
      // organize setter and getter
      val properties = new mutable.HashMap[String, BeanInfo.PropertyInfo]
      (getters.keySet ++ setters.keySet) foreach { p =>
        fieldInfos.get(p) foreach { fieldInfo =>
          val getter= getters.get(p)
          val setter= setters.get(p)
          val isTransient = Builder.isTransient(transnts.contains(p),setter.isDefined)
          val pd = BeanInfo.PropertyInfo(p, fieldInfo, getter,setter,isTransient)
          properties.put(p, pd)
        }
      }

      //process constructors,first is primary
      val ctors = ctorInfos map (ConstructorInfo(_))
      if (!Modifier.isPublic(clazz.getModifiers)) for (ml <- filterMethods.values; m <- ml) m.method.setAccessible(true)
      BeanInfo(clazz, ArraySeq.from(ctors), properties.toMap, filterMethods.toMap)
    }
  }


  inline def of[T](clazz:Class[T]): BeanInfo = ${ ofImpl[T]('clazz) }

  private def ofImpl[T](clazz:Expr[Class[T]])(implicit qctx: Quotes, ttype: scala.quoted.Type[T]): Expr[BeanInfo] = {
    import qctx.reflect.{_, given}

    def classOf(tpe: TypeRepr):Expr[Class[?]] =
      Literal(ClassOfConstant(tpe)).asExpr.asInstanceOf[Expr[Class[?]]]

    val typr=TypeRepr.of[T]
    '{
      val b = new BeanInfo.Builder(${classOf(typr)})
      ${Expr.block(addMemberBody('b)(qctx,ttype),'b)}.build()
    }
  }

  private def addMemberBody[T](t:Expr[BeanInfo.Builder])(implicit qctx: Quotes, ttype: scala.quoted.Type[T]): List[Expr[_]] = {
    import qctx.reflect.{_, given}

    case class FieldExpr(name:String,typeinfo:Expr[TypeInfo],transnt:Boolean,defaultValue:Option[Expr[Any]]=None)
    case class MethodExpr(name:String,rt:Expr[TypeInfo],params:Seq[FieldExpr],asField:Boolean)

    def classOf(tpe: TypeRepr):Expr[Class[?]] =
      Literal(ClassOfConstant(tpe)).asExpr.asInstanceOf[Expr[Class[?]]]

    def resolveType(typeRepr:TypeRepr,params:Map[String,TypeRepr]):Expr[TypeInfo]={
      var tpe = typeRepr
      var args:List[Expr[TypeInfo]]=List.empty
      tpe match{
        case d:TypeRef =>  if(tpe.typeSymbol.flags.is(Flags.Param)) tpe = params(tpe.typeSymbol.name)
        case c:AppliedType=>  args = resolveParamTypes(c,params)
        case d:AnnotatedType=> tpe = d.underlying
        case _=>throw new RuntimeException("Unspported type :" +tpe)
      }
      (tpe,args)
      if args.isEmpty then '{TypeInfo.get(${classOf(tpe)})}
      else '{TypeInfo.get(${classOf(tpe)},${Expr.ofList(args)})}
    }

    def resolveClassTypes(a:AppliedType,ctx:Map[String,TypeRepr]=Map.empty):Map[String,TypeRepr]={
      val params=new mutable.HashMap[String,TypeRepr]
      val mts = a.typeSymbol.memberTypes
      var i=0
      a.args foreach { arg =>
        val argType =  if(arg.typeSymbol.flags.is(Flags.Param)) then ctx(arg.typeSymbol.name)else arg
        params.put(mts(i).name,argType)
        i+=1
      }
      params.toMap
    }

    def resolveParamTypes(a:AppliedType,ctx:Map[String,TypeRepr]=Map.empty):List[Expr[TypeInfo]]={
      val mts = a.typeSymbol.memberTypes
      var i=0
      val params = new mutable.ArrayBuffer[Expr[TypeInfo]]
      a.args foreach { arg =>
        arg match{
          case d:TypeRef =>
            val argType = if arg.typeSymbol.flags.is(Flags.Param) then ctx(arg.typeSymbol.name) else d
            params += '{TypeInfo.get(${classOf(argType)})}
          case c:AppliedType=> {
            params += '{TypeInfo.get(${classOf(c)},${Expr.ofList(resolveParamTypes(c,ctx))})}
          }
        }
        i+=1
      }
      params.toList
    }

    def resolveCtorDefaults(symbol: Symbol):Map[Int,Expr[Any]]={
      val comp = symbol.companionClass
      if(comp != Symbol.noSymbol){
        val body = comp.tree.asInstanceOf[ClassDef].body
        val idents: List[(Int,Expr[Any])] =
          for case deff @ DefDef(name, _, _, _) <- body
              if name.startsWith("$lessinit$greater$default$")
          yield (name.substring("$lessinit$greater$default$".length).toInt,Ref(deff.symbol).asExpr)
        idents.toMap
      }else{
        Map.empty
      }
    }

    def resolveDefParams(defdef:DefDef,typeParams:Map[String,TypeRepr],defaults:Map[Int,Expr[Any]]): List[FieldExpr] ={
      val paramList=new mutable.ArrayBuffer[FieldExpr]
      defdef.paramss foreach { a =>
        a match {
          case TermParamClause(ps: List[ValDef])=>
            var i=0
            paramList ++= ps.map{vl =>
              i+=1
              FieldExpr(vl.name,resolveType(vl.tpt.tpe,typeParams),false,defaults.get(i))
            }
          case _=>
        }
      }
      paramList.toList
    }
    val typeRepr = TypeRepr.of[T]
    val fields = new mutable.ArrayBuffer[FieldExpr]
    val methods= new mutable.ArrayBuffer[MethodExpr]()
    val superBases= Set("scala.Any","scala.Matchable","java.lang.Object")
    for(bc <- typeRepr.baseClasses if !superBases.contains(bc.fullName)){
      val base=typeRepr.baseType(bc)
      var params=Map.empty[String,TypeRepr]
      base match{
        case a:AppliedType=> params = resolveClassTypes(a)
        case _=>
      }

      //Some fields declared in primary constructor will by ignored due to missing public access methods.
      base.typeSymbol.declaredFields foreach{ mm=>
        val tpe=mm.tree.asInstanceOf[ValDef].tpt.tpe
        val transnt = mm.annotations exists(x => x.show.toLowerCase.contains("transient"))
        fields += FieldExpr(mm.name,resolveType(tpe,params),transnt)
      }

      base.typeSymbol.declaredMethods foreach{  mm=>
        val defdef =mm.tree.asInstanceOf[DefDef]
        val rtType = resolveType(defdef.returnTpt.tpe,params)
        val paramList= resolveDefParams(defdef,params,Map.empty)
        if(!defdef.name.contains("$")){
          methods += MethodExpr(defdef.name,rtType,paramList.toList,defdef.termParamss.isEmpty)
        }
      }
    }

    val typeSymbol = typeRepr.typeSymbol
    val ctorDeclarations = typeSymbol.declarations.filter(_.isClassConstructor).toBuffer
    ctorDeclarations -= typeSymbol.primaryConstructor
    ctorDeclarations.prepend(typeSymbol.primaryConstructor)
    val ctorDefaults = resolveCtorDefaults(typeSymbol)
    val ctors = ctorDeclarations.map{ s =>
      val defdef = s.tree.asInstanceOf[DefDef]
      resolveDefParams(defdef,Map.empty,ctorDefaults)
    }

    val members = new mutable.ArrayBuffer[Expr[_]]()
    members ++= ctors.map{ m=>
      val paramInfos = m.map{ p=>
        if(p.defaultValue.isEmpty) '{ParamInfo(${Expr(p.name)},${p.typeinfo},None)}
        else '{ParamInfo(${Expr(p.name)},${p.typeinfo},Some(${p.defaultValue.get}))}
      }
      val paramList = Expr.ofList(paramInfos)
      '{${t}.addCtor(${paramList})}
    }
    members ++= fields.toList.map { x =>
      '{${t}.addField(${Expr(x.name)},${x.typeinfo},${Expr(x.transnt)})}
    }
    members ++= methods.map { x =>
      val paramInfos = x.params.map{ p=>
        if(p.defaultValue.isEmpty) '{ParamInfo(${Expr(p.name)},${p.typeinfo},None)}
        else '{ParamInfo(${Expr(p.name)},${p.typeinfo},Some(${p.defaultValue.get}))}
      }
      val paramList = Expr.ofList(paramInfos)
      '{${t}.addMethod(${Expr(x.name)},${x.rt},${paramList},${Expr(x.asField)})}
    }
    members.toList
  }

}

case class BeanInfo(clazz: Class[_], ctors: ArraySeq[ConstructorInfo], properties: Map[String, PropertyInfo], methods: Map[String, ArraySeq[MethodInfo]]) {
  /**
    * Return public metheds according to given name
    */
  def getMethods(name: String): ArraySeq[MethodInfo] = {
    methods.get(name).getOrElse(ArraySeq.empty)
  }

  /**
    * Return all public methods.
    */
  def methodList: List[MethodInfo] = {
    val rs = new mutable.ListBuffer[MethodInfo]
    for ((key, value) <- methods; info <- value) rs += info
    rs.sorted.toList
  }

  override def toString: String = {
    val sb = new mutable.ArrayBuffer[String]

    if (ctors.isEmpty) {
      sb += s"class ${clazz.getName} {"
    } else {
      sb += s"class ${clazz.getName}${replace(ctors.head.toString,"def this","")} {"
      ctors.tail foreach{ ctor=>
        sb += s"  ${ctor}"
      }
    }

    val displayed = new mutable.HashSet[Method]
    properties foreach { (name, pi) =>
      displayed ++= pi.getter
      displayed ++= pi.setter
      sb += s"  ${pi}"
    }
    for ((name, ml) <- methods; mi <- ml) {
      if (!displayed.contains(mi.method)) {
        displayed += mi.method
        sb += s"  ${mi}"
      }
    }
    sb += "}"
    sb.mkString("\n")
  }

  def getPropertyTypeInfo(property: String): Option[TypeInfo] =
    properties.get(property) match {
      case Some(p) => Some(p.typeinfo)
      case None => None
    }

  def getPropertyType(property: String): Option[Class[_]] =
    properties.get(property) match {
      case Some(p) => Some(p.clazz)
      case None => None
    }

  def getGetter(property: String): Option[Method] =
    properties.get(property) match {
      case Some(p) => p.getter
      case None => None
    }

  def getSetter(property: String): Option[Method] =
    properties.get(property) match {
      case Some(p) => p.setter
      case None => None
    }

  def readables:Iterable[PropertyInfo] =  properties.values.filter(_.readable)

  def writables:Iterable[PropertyInfo] = properties.values.filter(_.writable)
}
