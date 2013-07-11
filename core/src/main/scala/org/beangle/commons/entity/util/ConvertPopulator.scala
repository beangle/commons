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
package org.beangle.commons.entity.util

import java.lang.reflect.Method

import org.beangle.commons.entity.Entity
import org.beangle.commons.entity.meta.EntityType
import org.beangle.commons.entity.meta.Type
import org.beangle.commons.lang.Objects
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.conversion.Conversion
import org.beangle.commons.lang.conversion.impl.DefaultConversion
import org.beangle.commons.lang.reflect.Reflections
import org.beangle.commons.bean.PropertyUtils.{ getProperty, setProperty, copyProperty }
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object ConvertPopulator {
  /** Constant <code>logger</code> */
  val logger = LoggerFactory.getLogger(this.getClass);
  val TrimStr = true
}
/**
 * <p>
 * ConvertPopulatorBean class.
 * </p>
 *
 * @author chaostone
 * @version $Id: $
 */
import ConvertPopulator._

class ConvertPopulator(val conversion: Conversion = DefaultConversion.Instance) extends Populator {

  /**
   * Initialize target's attribuate path,Return the last property value and type.
   */
  def init(target: Entity[_], t: Type, attr: String): (Any, Type) = {
    var propObj: Any = target
    var property: Any = null
    var objtype = t

    var index = 0;
    val attrs = Strings.split(attr, ".");
    while (index < attrs.length) {
      val nested = attrs(index)
      try {
        property = getProperty(propObj, nested);
        val propertyType = objtype.getPropertyType(nested);
        // 初始化
        if (null == propertyType) {
          logger.error("Cannot find property type [{}] of {}", nested, propObj.getClass());
          throw new RuntimeException("Cannot find property type " + nested + " of "
            + propObj.getClass().getName());
        }
        if (null == property) {
          property = propertyType.newInstance();
          try {
            setProperty(propObj.asInstanceOf[AnyRef], nested, property);
          } catch {
            case e: Exception =>
              // Try fix jdk error for couldn't find correct setter when object's Set required type is
              // diffent with Get's return type declared in interface.
              val setter = Reflections.getSetter(propObj.getClass(), nested);
              if (null != setter) setter.invoke(propObj, property.asInstanceOf[AnyRef]);
              else throw e;
          }
        }
        index += 1
        propObj = property
        objtype = propertyType
      } catch {
        case e: Exception => throw new RuntimeException(e);
      }
    }
    return (property, objtype);
  }

  /**
   * 安静的拷贝属性，如果属性非法或其他错误则记录日志
   */
  def populate(target: Entity[_], entityType: EntityType, attr: String, value: Any): Boolean = {
    try {
      if (attr.indexOf('.') > -1) {
        val ot = init(target, entityType, Strings.substringBeforeLast(attr, "."));
        val lastAttr = Strings.substringAfterLast(attr, ".");
        setProperty(ot._1.asInstanceOf[AnyRef], lastAttr, convert(ot._2, lastAttr, value));
      } else {
        setProperty(target, attr, convert(entityType, attr, value));
      }
      return true;
    } catch {
      case e: Exception =>
        logger.warn("copy property failure:[class:" + entityType.entityName + " attr:" + attr + " value:"
          + value + "]:", e);
        return false;
    }
  }

  /**
   * 将params中的属性([attr(string)->value(object)]，放入到实体类中。
   * <p>
   * 如果引用到了别的实体，那么<br>
   * 如果params中的id为null，则将该实体的置为null.<br>
   * 否则新生成一个实体，将其id设为params中指定的值。 空字符串按照null处理
   */
  def populate(entity: Entity[_], entityType: EntityType, params: Map[String, Any]) {
    for ((attr, v) <- params) {
      var value = v
      if (value.isInstanceOf[String]) {
        if (Strings.isEmpty(value.asInstanceOf[String])) value = null
        else if (TrimStr) value = (value.asInstanceOf[String]).trim()
      }
      // 主键
      // if (type.isEntityType() && attr.equals(((EntityType) type).getIdName())) {
      // setProperty(entity, attr, convert(type, attr, value));
      // continue;
      // }
      // 普通属性
      if (-1 == attr.indexOf('.')) {
        copyValue(entity, attr, value);
      } else {
        val parentAttr = Strings.substring(attr, 0, attr.lastIndexOf('.'))
        try {
          val ot = init(entity, entityType, parentAttr);
          if (null == ot) {
            logger.error("error attr:[" + attr + "] value:[" + value + "]")
          } else {
            // 属性也是实体类对象
            if (ot._2.isEntityType) {
              val foreignKey = ot._2.asInstanceOf[EntityType].idName
              if (attr.endsWith("." + foreignKey)) {
                if (null == value) {
                  copyValue(entity, parentAttr, null);
                } else {
                  val oldValue = getProperty(entity, attr);
                  val newValue = convert(ot._2, foreignKey, value);
                  if (!Objects.equals(oldValue, newValue)) {
                    // 如果外键已经有值
                    if (null != oldValue) {
                      copyValue(entity, parentAttr, null);
                      init(entity, entityType, parentAttr);
                    }
                    setProperty(entity, attr, newValue);
                  }
                }
              } else {
                copyValue(entity, attr, value);
              }
            } else {
              copyValue(entity, attr, value);
            }
          }
        } catch {
          case e: Exception => logger.error("error attr:[" + attr + "] value:[" + value + "]", e);
        }
      }
    }
  }

  private def convert(t: Type, attr: String, value: Any): Any = {
    if (value.isInstanceOf[AnyRef] && null == value) null else conversion.convert(value, t.getPropertyType(attr).returnedClass)
  }

  private def copyValue(target: AnyRef, attr: String, value: Any): Any = {
    // try {
    copyProperty(target, attr, value, conversion);
    // } catch (Exception e) {
    // logger.error("copy property failure:[class:" + target.getClass().getName() + " attr:" + attr
    // + " value:" + value + "]:", e);
    // }
  }
}
