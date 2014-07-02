package org.beangle.commons.lang.reflect

import org.beangle.commons.bean.Factory
import org.beangle.commons.jndi.{JndiDataSourceFactory, JndiObjectFactory}
import org.beangle.commons.lang.testbean.{Dog, NumIdBean}
import org.junit.runner.RunWith
import org.scalatest.{FunSpec, Matchers}
import org.scalatest.junit.JUnitRunner
import javax.sql.DataSource
import org.beangle.commons.lang.testbean.Entity
import org.beangle.commons.lang.testbean.Book

@RunWith(classOf[JUnitRunner])
class ReflectionsTest extends FunSpec with Matchers {

   describe("Reflections") {
    it("getSuperClassParamType") {
      val dataSourceType=Reflections.getGenericParamType(classOf[JndiDataSourceFactory], classOf[Factory[_]])
      assert(dataSourceType.size==1)
      assert(dataSourceType.values.head==classOf[DataSource])
      
      val idType=Reflections.getGenericParamType(classOf[Book], classOf[Entity[_]])
      assert(idType.size==1)
      assert(idType.values.head==classOf[java.lang.Long])
    }
  }
}