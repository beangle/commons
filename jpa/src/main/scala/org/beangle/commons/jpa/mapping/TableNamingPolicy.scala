package org.beangle.commons.jpa.mapping

/**
 * Entity table and Collection Table Naming Strategy.
 * 
 * @author chaostone
 */
trait TableNamingPolicy {

  /**
   * Convert class to table name
   * 
   * @param className
   */
  def classToTableName(className:String):String

  /**
   * Convert collection to table name
   * 
   * @param className
   * @param tableName
   * @param collectionName
   */
  def collectionToTableName(className:String,tableName:String,collectionName:String):String

  /**
   * Return schema for package
   * 
   * @param packageName
   */
  def getSchema(packageName:String):String

  /**
   * Mapped in multischema?
   * 
   */
  def isMultiSchema:Boolean

}
