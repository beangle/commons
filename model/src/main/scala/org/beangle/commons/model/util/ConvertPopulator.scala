/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2016, Beangle Software.
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
package org.beangle.commons.model.util

import java.lang.reflect.Method

import org.beangle.commons.model.Entity
import org.beangle.commons.model.meta._
import org.beangle.commons.lang.Objects
import org.beangle.commons.lang.Strings
import org.beangle.commons.logging.Logging
import org.beangle.commons.conversion.Conversion
import org.beangle.commons.conversion.impl.DefaultConversion
import org.beangle.commons.lang.reflect.Reflections
import org.beangle.commons.bean.Properties

object ConvertPopulator extends Logging {
  val TrimStr = true
}
/**
 * ConvertPopulator
 *
 * @author chaostone
 */
import ConvertPopulator._
import org.beangle.commons.lang.reflect.BeanInfos

class ConvertPopulator(conversion: Conversion = DefaultConversion.Instance) extends Populator with Logging {
  val properties = new Properties(BeanInfos.Default, conversion)
  /**
   * Initialize target's attribuate path,Return the last property value and type.
   */
  override def init(target: Entity[_], t: EntityType, attr: String): (Any, Property) = {
    var propObj: Any = target
    var property: Any = null
    var objtype: StructType = t
    var propertyType: Property = null

    var index = 0
    val attrs = Strings.split(attr, ".")
    while (index < attrs.length) {
      val nested = attrs(index)
      try {
        property = properties.get[Object](propObj, nested)
        objtype.getProperty(nested) match {
          case Some(t) => {
            property match {
              case null | None =>
                property = t.clazz.newInstance()
                properties.set(propObj.asInstanceOf[AnyRef], nested, property)
              case Some(p) =>
                property = p
              case _ =>
            }
            if (index < attrs.length) {
              t match {
                case n: SingularProperty => {
                  n.propertyType match {
                    case s: StructType => objtype = s
                    case _ =>
                      logger.error(s"Cannot find property type [$nested] of ${propObj.getClass}")
                      throw new RuntimeException("Cannot find property type " + nested + " of " + propObj.getClass().getName())
                  }
                }
                case _ =>
                  logger.error(s"Cannot populate collection property type [$nested] of ${propObj.getClass}")
                  throw new RuntimeException("Cannot find property type " + nested + " of " + propObj.getClass().getName())
              }
            }
            propertyType = t
          }
          case None => {
            logger.error(s"Cannot find property type [$nested] of ${propObj.getClass}")
            throw new RuntimeException("Cannot find property type " + nested + " of " + propObj.getClass().getName())
          }
        }
        index += 1
        propObj = property
      } catch {
        case e: Exception => throw new RuntimeException(e)
      }
    }
    return (property, propertyType)
  }

  /**
   * 安静的拷贝属性，如果属性非法或其他错误则记录日志
   */
  override def populate(target: Entity[_], entityType: EntityType, attr: String, value: Any): Boolean = {
    populate(target, entityType, Map(attr -> value)) == 1
  }

  /**
   * 将params中的属性([attr(string)->value(object)]，放入到实体类中。
   * <p>
   * 如果引用到了别的实体，那么<br>
   * 如果params中的id为null，则将该实体的置为null.<br>
   * 否则新生成一个实体，将其id设为params中指定的值。 空字符串按照null处理
   */
  override def populate(entity: Entity[_], entityType: EntityType, params: collection.Map[String, Any]): Int = {
    var success = 0
    params foreach {
      case (attr, v) =>
        var value = v
        if (value.isInstanceOf[String]) {
          if (Strings.isEmpty(value.asInstanceOf[String])) value = null
          else if (TrimStr) value = (value.asInstanceOf[String]).trim()
        }
        if (-1 == attr.indexOf('.')) {
          copyValue(entity, attr, value)
        } else {
          val parentAttr = Strings.substring(attr, 0, attr.lastIndexOf('.'))
          try {
            val ot = init(entity, entityType, parentAttr)
            if (null == ot) {
              logger.error(s"error attr:[$attr] value:[$value]")
            } else {
              ot._2 match {
                case sp: SingularProperty =>
                  sp.propertyType match {
                    case ft: EntityType =>
                      val foreignKey = ft.id.name
                      if (attr.endsWith("." + foreignKey)) {
                        if (null == value) {
                          copyValue(entity, parentAttr, null)
                        } else {
                          val oldValue = properties.get[Object](entity, attr)
                          val newValue = convert(ft, foreignKey, value)
                          if (!Objects.equals(oldValue, newValue)) {
                            // 如果外键已经有值
                            if (null != oldValue) {
                              copyValue(entity, parentAttr, null)
                              init(entity, entityType, parentAttr)
                            }
                            properties.set(entity, attr, newValue)
                          }
                        }
                      } else {
                        copyValue(entity, attr, value)
                      }
                    case _ =>
                      copyValue(entity, attr, value)
                  }
                case _ =>
              }
            }
          } catch {
            case e: Exception =>
              logger.error(s"error attr:[$attr] value:[$value]", e)
              success -= 1
          }
        }
        success += 1
    }
    success
  }

  private def convert(t: EntityType, attr: String, value: Any): Any = {
    if (value.isInstanceOf[AnyRef] && null == value) null
    else {
      t.getProperty(attr) match {
        case Some(ty) => conversion.convert(value, ty.clazz)
        case None     => throw new RuntimeException("cannot find attribuate type of " + attr + " in " + t.entityName)
      }
    }
  }

  private def copyValue(target: AnyRef, attr: String, value: Any): Any = {
    properties.copy(target, attr, value)
  }
}
