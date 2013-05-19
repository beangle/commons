package org.beangle.jdbc.meta.model

class PrimaryKeyTest {

  def testSqlConstraintString = {
    val pk = new PrimaryKey("pk_sometable", new Column("id", 4))
    assert(pk.sqlConstraintString == "primary key (id)")
  }
}