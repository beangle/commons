package org.beangle.commons.lang.reflect

import org.beangle.commons.bean.Factory
import org.beangle.commons.jndi.JndiDataSourceFactory
import org.beangle.commons.lang.annotation.description
import org.beangle.commons.lang.testbean.{Book, TestChild2Bean}
import org.beangle.commons.lang.testbean.Entity
import org.junit.runner.RunWith
import org.scalatest.{FunSpec, Matchers}
import org.scalatest.junit.JUnitRunner
import org.scalatest.junit.JUnitRunner
import javax.sql.DataSource

@RunWith(classOf[JUnitRunner])
class ReflectionsTest extends FunSpec with Matchers {

  describe("Reflections") {
    it("getSuperClassParamType") {
      val dataSourceType = Reflections.getGenericParamType(classOf[JndiDataSourceFactory], classOf[Factory[_]])
      assert(dataSourceType.size == 1)
      assert(dataSourceType.values.head == classOf[DataSource])

      val idType = Reflections.getGenericParamType(classOf[Book], classOf[Entity[_]])
      assert(idType.size == 1)
      assert(idType.values.head == classOf[java.lang.Long])
    }
    it("getAnnotation") {
      val clazz = classOf[TestChild2Bean]
      val method1 = clazz.getMethod("method1", classOf[Long])
      val method2 = clazz.getMethod("method2", classOf[Long])
      assert(null != Reflections.getAnnotation(method1, classOf[description]))
      assert(null == Reflections.getAnnotation(method2, classOf[description]))
    }
  }
}