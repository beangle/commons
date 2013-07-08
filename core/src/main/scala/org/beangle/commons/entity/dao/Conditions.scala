/*
 * Beangle, Agile Java/Scala Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2013, Beangle Software.
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
package org.beangle.commons.entity.dao


import org.beangle.commons.bean.PropertyUtils
import org.beangle.commons.entity.Component
import org.beangle.commons.entity.Entity
import org.beangle.commons.entity.util.ValidKey
import org.beangle.commons.lang.Strings
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * 条件提取辅助类
 * 
 * @author chaostone
 */
object Conditions {
  private val logger = LoggerFactory.getLogger(this.getClass);

  def toQueryString( conditions:List[Condition]):String = {
    if (null == conditions || conditions.isEmpty) return ""
    val buf = new StringBuilder("")
    val seperator= " and "
    for (con <- conditions) {
      buf.append('(').append(con.content).append(')').append(seperator)
    }
    if(buf.length>0) buf.delete(buf.length - seperator.length,buf.length)
    buf.toString
  }

  /**
   * 提取对象中的条件<br>
   * 提取的属性仅限"平面"属性(允许包括component)<br>
   * 过滤掉属性:null,或者空Collection
   * 
   * @param alias
   * @param entity
   */
  def extractConditions(alias:String,entity:Entity[_]): List[Condition]= {
    if (null == entity) return Nil 
    val conditions = new collection.mutable.ListBuffer[Condition]
    val prefix= if (null!=alias && alias.length > 0 && !alias.endsWith(".")) alias +"." else ""
    var curr = "";
    try {
      val props = PropertyUtils.getWritableProperties(entity.getClass)
      for (attr <- props) {
        curr=attr
        val value = PropertyUtils.getProperty(entity, attr);
        if (null != value && !value.isInstanceOf[Seq[_]] && !value.isInstanceOf[java.util.Collection[_]])
          addAttrCondition(conditions, prefix + attr, value)
      }
    } catch {
        case e:Exception => 
      logger.debug("error occur in extractConditions for  bean {} with attr named {}", entity, curr);
    }
    conditions.toList
  }

  /**
   * 获得条件的绑定参数映射
   * 
   * @param conditions
   */
  def getParamMap(conditions: List[Condition]) :Map[String, Any]={
    val params = new collection.mutable.HashMap[String, Any]
    for (con <- conditions) {
      params++=getParamMap(con)
    }
    params.toMap
  }

  /**
   * 获得条件的绑定参数映射
   * 
   * @param condition
   */
  def getParamMap(condition:Condition):Map[String, Any]= {
    val params = new collection.mutable.HashMap[String, Any]
    if (!Strings.contains(condition.content, "?")) {
      val paramNames = condition.paramNames
      if (paramNames.size > condition.params.size) throw new RuntimeException(
          "condition params not set [" + condition.content + "] with value:" + condition.params)
      var i=0
      while(i<paramNames.size){
        params.put(paramNames(i), condition.params(i))
        i+=1
      }
    }
    params.toMap
  }

  /**
   * 为extractConditions使用的私有方法<br>
   * 
   * @param conditions
   * @param name
   * @param value
   * @param mode
   */
  def addAttrCondition(conditions: collection.mutable.ListBuffer[Condition], name:String,value:Any) {
    if (value.isInstanceOf[String]) {
      if (Strings.isBlank(value.asInstanceOf[String])) return
      val content = new StringBuilder(name)
      content.append(" like :").append(name.replace('.', '_'))
      conditions+=new Condition(content.toString(), "%" + value + "%")
    } else if (value.isInstanceOf[Component]) {
      conditions++=extractComponent(name, value.asInstanceOf[Component])
      return
    } else if (value.isInstanceOf[Entity[_]]) {
      try {
        val key = "id";
        val property = PropertyUtils.getProperty(value, key);
        if (ValidKey(property)) {
          val content = new StringBuilder(name);
          content.append('.').append(key).append(" = :").append(name.replace('.', '_')).append('_')
              .append(key);
          conditions += Condition(content.toString(), property)
        }
      } catch {
        case e:Exception => logger.warn("getProperty " + value + "error", e);
      }
    } else {
      conditions+=Condition(name + " = :" + name.replace('.', '_'), value)
    }
  }

  def extractComponent(prefix:String ,component:Component):List[Condition] = {
    if (null == component) return Nil
    val  conditions = new collection.mutable.ListBuffer[Condition]
    var curr=""
    try {
      val props = PropertyUtils.getWritableProperties(component.getClass)
      for (attr <- props) {
        curr= attr
        val value = PropertyUtils.getProperty(component, attr)
        if (null != value && !value.isInstanceOf[Seq[_]] && !value.isInstanceOf[java.util.Collection[_]])
          addAttrCondition(conditions, prefix + "." + attr, value)
      }
    } catch  {
      case e:Exception =>
      logger.warn("error occur in extractComponent of component:" + component + "with attr named :" + curr)
    }
    conditions.toList
  }

}
