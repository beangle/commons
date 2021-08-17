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
import scala.collection.immutable.ArraySeq
import scala.collection.mutable
import BeanInfo.*
import scala.quoted.*

object BeanInfoDigger{
  def digInto(argsExpr:Expr[Seq[Class[_]]],cache:Expr[BeanInfoCache])(using Quotes):Expr[List[BeanInfo]]={
    import quotes.reflect.*
    argsExpr match{
      case Varargs(cls)=>
        val biList = cls.map { cl =>
          cl.asTerm match{
            case TypeApply(term,trees) => new BeanInfoDigger[quotes.type](trees.head.tpe).dig()
          }
        }
        '{
          val bis = ${Expr.ofList(biList)}
          bis.foreach{ bi=> ${cache}.update(bi)}
          bis
        }
      case _=>
        report.error(s"Args must be explicit", argsExpr)
        '{???}
    }
  }

  def digInto[T:Type](ec:Expr[Class[T]],cache:Expr[BeanInfoCache])(implicit quotes: Quotes):Expr[BeanInfo]={
    import quotes.reflect.*
    val digger = new BeanInfoDigger[quotes.type](quotes.reflect.TypeRepr.of[T])
    '{
      ${cache}.update(${digger.dig()})
    }
  }
}

class BeanInfoDigger[Q <: Quotes](trr: Any)(using val q: Q) {
  import q.reflect.*
  val typeRepr = trr.asInstanceOf[TypeRepr]
  val symbol = typeRepr.typeSymbol

  def dig(): Expr[BeanInfo] = {
    '{
    val b = new BeanInfo.Builder(${classOf(typeRepr)})
    ${Expr.block(addMemberBody('b),'b)}.build()
    }
  }

  def classOf(tpe: TypeRepr):Expr[Class[?]] =
    Literal(ClassOfConstant(tpe)).asExpr.asInstanceOf[Expr[Class[?]]]

  case class FieldExpr(name:String,typeinfo:Expr[TypeInfo],transnt:Boolean,defaultValue:Option[Expr[Any]]=None)
  case class MethodExpr(name:String,rt:Expr[TypeInfo],params:Seq[FieldExpr],asField:Boolean)

  private def addMemberBody(t:Expr[BeanInfo.Builder]): List[Expr[_]] = {
    val fields = new mutable.ArrayBuffer[FieldExpr]
    val methods= new mutable.ArrayBuffer[MethodExpr]()
    val superBases= Set("scala.Any","scala.Matchable","java.lang.Object","scala.Equals","scala.Product","java.io.Serializable")
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
    var i=0
    val ctors = ctorDeclarations.map{ s =>
      val defdef = s.tree.asInstanceOf[DefDef]
      i += 1
      resolveDefParams(defdef,Map.empty,if i==1 then ctorDefaults else Map.empty)
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
  def resolveType(typeRepr:TypeRepr,params:Map[String,TypeRepr]):Expr[TypeInfo]={
    var tpe = typeRepr
    var args:List[Expr[TypeInfo]]=List.empty
    tpe match{
      case d:TypeRef =>  if(tpe.typeSymbol.flags.is(Flags.Param) && params.contains(tpe.typeSymbol.name)) tpe = params(tpe.typeSymbol.name)
      case c:AppliedType=>  args = resolveParamTypes(c,params)
      case d:AnnotatedType=> tpe = d.underlying
      case c:ConstantType=>
      case _=>throw new RuntimeException("Unspported type :" +tpe)
    }
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
          val argType = if arg.typeSymbol.flags.is(Flags.Param) && ctx.contains(arg.typeSymbol.name) then ctx(arg.typeSymbol.name) else d
          params += '{TypeInfo.get(${classOf(argType)})}
        case c:AppliedType=>
          params += '{TypeInfo.get(${classOf(c)},${Expr.ofList(resolveParamTypes(c,ctx))})}
        case tb:TypeBounds => '{TypeInfo.get(${classOf(tb)})}
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
}
