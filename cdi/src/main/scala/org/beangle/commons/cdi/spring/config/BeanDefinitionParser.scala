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
package org.beangle.commons.cdi.spring.config

import org.springframework.beans.factory.xml.BeanDefinitionParserDelegate._
import org.beangle.commons.logging.Logging
import org.beangle.commons.lang.Strings
import org.beangle.commons.collection.Collections
import org.springframework.beans.BeanMetadataAttribute
import org.springframework.beans.BeanMetadataAttributeAccessor
import org.springframework.beans.PropertyValue
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.beans.factory.config.BeanDefinitionHolder
import org.springframework.beans.factory.config.ConstructorArgumentValues
import org.springframework.beans.factory.config.RuntimeBeanNameReference
import org.springframework.beans.factory.config.RuntimeBeanReference
import org.springframework.beans.factory.config.TypedStringValue
import org.springframework.beans.factory.parsing.BeanEntry
import org.springframework.beans.factory.parsing.ConstructorArgumentEntry
import org.springframework.beans.factory.parsing.ParseState
import org.springframework.beans.factory.parsing.PropertyEntry
import org.springframework.beans.factory.parsing.QualifierEntry
import org.springframework.beans.factory.support._
import org.springframework.util.CollectionUtils
import org.springframework.util.StringUtils
import org.springframework.util.xml.DomUtils
import org.w3c.dom.Element
import org.w3c.dom.NamedNodeMap
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import scala.collection.mutable
import org.springframework.beans.factory.config.ConstructorArgumentValues.ValueHolder
import org.beangle.commons.cdi.bind.Binder.InjectPlaceHolder
/**
 * BeanDefinitionParser class.
 *
 * @author chaostone
 */
class BeanDefinitionParser extends Logging {

  private val parseState = new ParseState

  /**
   * Stores all used bean names so we can enforce uniqueness on a per file
   * basis.
   */
  private val usedNames = new mutable.HashSet[String]

  /**
   * extractSource.
   */
  protected def extractSource(ele: Element): Object = null

  /**
   * Report an error with the given message for the given source element.
   */
  protected def error(message: String, source: Node) {
    logger.error(message)
  }

  /**
   * Report an error with the given message for the given source element.
   */
  protected def error(message: String, source: Element) {
    logger.error(message)
  }

  /**
   * Report an error with the given message for the given source element.
   */
  protected def error(message: String, source: Element, cause: Throwable) {
    logger.error(message)
  }

  /**
   * Parses the supplied <code>&ltbean&gt</code> element. May return <code>null</code> if there
   * were errors during parse.
   */
  def parseBeanDefinitionElement(ele: Element): ReconfigBeanDefinitionHolder = parseBeanDefinitionElement(ele, null)

  /**
   * Parses the supplied <code>&ltbean&gt</code> element. May return <code>null</code> if there
   * were errors during parse.
   */
  private def parseBeanDefinitionElement(ele: Element, containingBean: BeanDefinition): ReconfigBeanDefinitionHolder = {
    val id = ele.getAttribute(ID_ATTRIBUTE)
    val nameAttr = ele.getAttribute(NAME_ATTRIBUTE)

    val aliases = new mutable.ListBuffer[String]
    if (Strings.isNotEmpty(nameAttr)) {
      val nameArr = StringUtils.tokenizeToStringArray(nameAttr, MULTI_VALUE_ATTRIBUTE_DELIMITERS)
      for (name <- nameArr) aliases += name
    }

    var beanName = id
    if (!StringUtils.hasText(beanName) && !aliases.isEmpty) {
      beanName = aliases.remove(0)
      logger.debug(s"No XML 'id' specified - using '$beanName' as bean name and $aliases as aliases")
    }

    if (containingBean == null) checkNameUniqueness(beanName, aliases, ele)

    val beanDefinition = parseBeanDefinitionElement(ele, beanName, containingBean)
    if (beanDefinition != null) {
      val bdh = new ReconfigBeanDefinitionHolder(beanDefinition, beanName, aliases.toArray)
      val ovr = ele.getAttribute("override")
      if (null != ovr && ovr == "remove") bdh.configType = ReconfigType.Remove
      val primary = ele.getAttribute("primary")
      if (null != primary && primary.equals("true")) bdh.configType = ReconfigType.Primary
      return bdh
    }
    return null
  }

  /**
   * Validate that the specified bean name and aliases have not been used
   * already.
   */
  protected def checkNameUniqueness(beanName: String, aliases: Seq[String], beanElement: Element) {
    var foundName: String = null

    if (StringUtils.hasText(beanName) && this.usedNames.contains(beanName)) foundName = beanName

    if (foundName == null) foundName = Collections.findFirstMatch(this.usedNames, aliases).orNull

    if (foundName != null) error("Bean name '" + foundName + "' is already used in this file", beanElement)

    this.usedNames += beanName
    this.usedNames ++= aliases
  }

  /**
   * Parse the bean definition itself, without regard to name or aliases. May
   * return <code>null</code> if problems occured during the parse of the bean
   * definition.
   */
  private def parseBeanDefinitionElement(ele: Element, beanName: String, containingBean: BeanDefinition): AbstractBeanDefinition = {

    this.parseState.push(new BeanEntry(beanName))

    var className: String = if (ele.hasAttribute(CLASS_ATTRIBUTE)) ele.getAttribute(CLASS_ATTRIBUTE).trim() else null

    try {
      var parent: String = if (ele.hasAttribute(PARENT_ATTRIBUTE)) ele.getAttribute(PARENT_ATTRIBUTE) else null
      val bd = createBeanDefinition(className, parent)

      parseBeanDefinitionAttributes(ele, beanName, containingBean, bd)
      bd.setDescription(DomUtils.getChildElementValueByTagName(ele, DESCRIPTION_ELEMENT))

      parseMetaElements(ele, bd)
      parseLookupOverrideSubElements(ele, bd.getMethodOverrides())
      parseReplacedMethodSubElements(ele, bd.getMethodOverrides())

      parseConstructorArgElements(ele, bd)
      parsePropertyElements(ele, bd)
      parseQualifierElements(ele, bd)

      // bd.setResource(this.readerContext.getResource())
      bd.setSource(extractSource(ele))

      return bd
    } catch {
      case ex: ClassNotFoundException => error("Bean class [" + className + "] not found", ele, ex)
      case err: NoClassDefFoundError  => error("Class that bean class [" + className + "] depends on not found", ele, err)
      case exr: Throwable             => error("Unexpected failure during bean definition parsing", ele, exr)
    } finally {
      this.parseState.pop()
    }

    return null
  }

  /**
   * Apply the attributes of the given bean element to the given bean *
   * definition.
   */
  private def parseBeanDefinitionAttributes(ele: Element, beanName: String,
                                            containingBean: BeanDefinition, bd: AbstractBeanDefinition): AbstractBeanDefinition = {

    if (ele.hasAttribute(SCOPE_ATTRIBUTE)) {
      // Spring 2.x "scope" attribute
      bd.setScope(ele.getAttribute(SCOPE_ATTRIBUTE))
    } else if (containingBean != null) {
      // Take default from containing bean in case of an inner bean
      // definition.
      bd.setScope(containingBean.getScope())
    } else {
      bd.setScope(BeanDefinition.SCOPE_SINGLETON)
    }

    if (ele.hasAttribute(ABSTRACT_ATTRIBUTE))
      bd.setAbstract(TRUE_VALUE.equals(ele.getAttribute(ABSTRACT_ATTRIBUTE)))

    bd.setLazyInit(TRUE_VALUE == ele.getAttribute(LAZY_INIT_ATTRIBUTE))

    bd.setAutowireMode(getAutowireMode(ele.getAttribute(AUTOWIRE_ATTRIBUTE)))

    if (ele.hasAttribute(DEPENDS_ON_ATTRIBUTE)) {
      bd.setDependsOn(StringUtils.tokenizeToStringArray(ele.getAttribute(DEPENDS_ON_ATTRIBUTE), MULTI_VALUE_ATTRIBUTE_DELIMITERS))
    }

    if (ele.hasAttribute(PRIMARY_ATTRIBUTE))
      bd.setPrimary(TRUE_VALUE.equals(ele.getAttribute(PRIMARY_ATTRIBUTE)))

    if (ele.hasAttribute(INIT_METHOD_ATTRIBUTE)) {
      val initMethodName = ele.getAttribute(INIT_METHOD_ATTRIBUTE)
      if (!"".equals(initMethodName)) bd.setInitMethodName(initMethodName)
    }

    if (ele.hasAttribute(DESTROY_METHOD_ATTRIBUTE)) {
      val destroyMethodName = ele.getAttribute(DESTROY_METHOD_ATTRIBUTE)
      if (!"".equals(destroyMethodName)) bd.setDestroyMethodName(destroyMethodName)
    }

    if (ele.hasAttribute(FACTORY_METHOD_ATTRIBUTE))
      bd.setFactoryMethodName(ele.getAttribute(FACTORY_METHOD_ATTRIBUTE))
    if (ele.hasAttribute(FACTORY_BEAN_ATTRIBUTE))
      bd.setFactoryBeanName(ele.getAttribute(FACTORY_BEAN_ATTRIBUTE))

    return bd
  }

  /**
   * Create a bean definition for the given class name and parent name.
   *
   */
  protected def createBeanDefinition(className: String, parentName: String): AbstractBeanDefinition = BeanDefinitionReaderUtils.createBeanDefinition(parentName, className, null)

  /**
   * parseMetaElements.
   */
  private def parseMetaElements(ele: Element, attributeAccessor: BeanMetadataAttributeAccessor) {
    val nl = ele.getChildNodes()
    for (i <- 0 until nl.getLength) {
      val node = nl.item(i)
      if (node.isInstanceOf[Element] && nodeNameEquals(node, META_ELEMENT)) {
        val metaElement = node.asInstanceOf[Element]
        val attribute = new BeanMetadataAttribute(metaElement.getAttribute(KEY_ATTRIBUTE), metaElement.getAttribute(VALUE_ATTRIBUTE))
        attribute.setSource(extractSource(metaElement))
        attributeAccessor.addMetadataAttribute(attribute)
      }
    }
  }

  /**
   * getAutowireMode.
   */
  private def getAutowireMode(att: String): Int = {
    if (AUTOWIRE_BY_NAME_VALUE == att) {
      AbstractBeanDefinition.AUTOWIRE_BY_NAME
    } else if (AUTOWIRE_BY_TYPE_VALUE == att) {
      AbstractBeanDefinition.AUTOWIRE_BY_TYPE
    } else if (AUTOWIRE_CONSTRUCTOR_VALUE == att) {
      AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR
    } else AbstractBeanDefinition.AUTOWIRE_NO
  }

  /**
   * Parse constructor-arg sub-elements of the given bean element.
   */
  private def parseConstructorArgElements(beanEle: Element, bd: BeanDefinition) {
    val nl = beanEle.getChildNodes()
    for (i <- 0 until nl.getLength) {
      val node = nl.item(i)
      if (node.isInstanceOf[Element] && nodeNameEquals(node, CONSTRUCTOR_ARG_ELEMENT))
        parseConstructorArgElement(node.asInstanceOf[Element], bd)
    }
  }

  /**
   * Parse property sub-elements of the given bean element.
   *
   */
  private def parsePropertyElements(beanEle: Element, bd: BeanDefinition) {
    val nl = beanEle.getChildNodes()
    for (i <- 0 until nl.getLength) {
      val node = nl.item(i)
      if (node.isInstanceOf[Element] && nodeNameEquals(node, PROPERTY_ELEMENT))
        parsePropertyElement(node.asInstanceOf[Element], bd)
    }
  }

  /**
   * Parse qualifier sub-elements of the given bean element.
   *
   */
  private def parseQualifierElements(beanEle: Element, bd: AbstractBeanDefinition) {
    val nl = beanEle.getChildNodes()
    for (i <- 0 until nl.getLength) {
      val node = nl.item(i)
      if (node.isInstanceOf[Element] && nodeNameEquals(node, QUALIFIER_ELEMENT))
        parseQualifierElement(node.asInstanceOf[Element], bd)
    }
  }

  /**
   * Parse lookup-override sub-elements of the given bean element.
   *
   */
  private def parseLookupOverrideSubElements(beanEle: Element, overrides: MethodOverrides) {
    val nl = beanEle.getChildNodes()
    for (i <- 0 until nl.getLength) {
      val node = nl.item(i)
      if (node.isInstanceOf[Element] && nodeNameEquals(node, LOOKUP_METHOD_ELEMENT)) {
        val ele = node.asInstanceOf[Element]
        val ovr = new LookupOverride(ele.getAttribute(NAME_ATTRIBUTE), ele.getAttribute(BEAN_ELEMENT))
        ovr.setSource(extractSource(ele))
        overrides.addOverride(ovr)
      }
    }
  }

  /**
   * Parse replaced-method sub-elements of the given bean element.
   *
   */
  private def parseReplacedMethodSubElements(beanEle: Element, overrides: MethodOverrides) {
    val nl = beanEle.getChildNodes
    for (i <- 0 until nl.getLength) {
      val node = nl.item(i)
      if (node.isInstanceOf[Element] && nodeNameEquals(node, REPLACED_METHOD_ELEMENT)) {
        val replacedMethodEle = node.asInstanceOf[Element]
        val name = replacedMethodEle.getAttribute(NAME_ATTRIBUTE)
        val callback = replacedMethodEle.getAttribute(REPLACER_ATTRIBUTE)
        val replaceOverride = new ReplaceOverride(name, callback)
        // Look for arg-type match elements.
        val argTypeElesIter = DomUtils.getChildElementsByTagName(replacedMethodEle, ARG_TYPE_ELEMENT).iterator()
        while (argTypeElesIter.hasNext) {
          replaceOverride.addTypeIdentifier(argTypeElesIter.next().getAttribute(ARG_TYPE_MATCH_ATTRIBUTE))
        }
        replaceOverride.setSource(extractSource(replacedMethodEle))
        overrides.addOverride(replaceOverride)
      }
    }
  }

  /**
   * Parse a constructor-arg element.
   *
   */
  private def parseConstructorArgElement(ele: Element, bd: BeanDefinition) {
    val indexAttr = ele.getAttribute(INDEX_ATTRIBUTE)
    val typeAttr = ele.getAttribute(TYPE_ATTRIBUTE)
    val nameAttr = ele.getAttribute(NAME_ATTRIBUTE)
    if (Strings.isNotEmpty(indexAttr)) {
      try {
        val index = Integer.parseInt(indexAttr)
        if (index < 0) {
          error("'index' cannot be lower than 0", ele)
        } else {
          try {
            this.parseState.push(new ConstructorArgumentEntry(index))
            val value = parsePropertyValue(ele, bd, null)
            val valueHolder = new ConstructorArgumentValues.ValueHolder(value)
            if (Strings.isNotEmpty(typeAttr)) valueHolder.setType(typeAttr)
            if (Strings.isNotEmpty(nameAttr)) valueHolder.setName(nameAttr)
            valueHolder.setSource(extractSource(ele))
            if (bd.getConstructorArgumentValues().hasIndexedArgumentValue(index)) {
              error("Ambiguous constructor-arg entries for index " + index, ele)
            } else {
              bd.getConstructorArgumentValues().addIndexedArgumentValue(index, valueHolder)
            }
          } finally {
            this.parseState.pop()
          }
        }
      } catch {
        case ex: NumberFormatException => error("Attribute 'index' of tag 'constructor-arg' must be an integer", ele)
      }
    } else {
      try {
        this.parseState.push(new ConstructorArgumentEntry())
        val value = parsePropertyValue(ele, bd, null)
        val valueHolder = new ConstructorArgumentValues.ValueHolder(value)
        if (Strings.isNotEmpty(typeAttr)) valueHolder.setType(typeAttr)
        if (Strings.isNotEmpty(nameAttr)) valueHolder.setName(nameAttr)
        valueHolder.setSource(extractSource(ele))
        bd.getConstructorArgumentValues().addGenericArgumentValue(valueHolder)
      } finally {
        this.parseState.pop()
      }
    }
  }

  /**
   * Parse a property element.
   *
   */
  private def parsePropertyElement(ele: Element, bd: BeanDefinition) {
    val propertyName = ele.getAttribute(NAME_ATTRIBUTE)
    if (Strings.isEmpty(propertyName)) {
      error("Tag 'property' must have a 'name' attribute", ele)
      return
    }
    this.parseState.push(new PropertyEntry(propertyName))
    try {
      if (bd.getPropertyValues().contains(propertyName)) {
        error("Multiple 'property' definitions for property '" + propertyName + "'", ele)
        return
      }
      val value = parsePropertyValue(ele, bd, propertyName)
      val pv = new PropertyValue(propertyName, value)
      parseMetaElements(ele, pv)
      pv.setSource(extractSource(ele))
      bd.getPropertyValues().addPropertyValue(pv)
    } finally {
      this.parseState.pop()
    }
  }

  /**
   * Parse a qualifier element.
   *
   */
  private def parseQualifierElement(ele: Element, bd: AbstractBeanDefinition) {
    val typeName = ele.getAttribute(TYPE_ATTRIBUTE)
    if (Strings.isEmpty(typeName)) {
      error("Tag 'qualifier' must have a 'type' attribute", ele)
      return
    }
    this.parseState.push(new QualifierEntry(typeName))
    try {
      val qualifier = new AutowireCandidateQualifier(typeName)
      qualifier.setSource(extractSource(ele))
      val value = ele.getAttribute(VALUE_ATTRIBUTE)
      if (Strings.isNotEmpty(value)) {
        qualifier.setAttribute(AutowireCandidateQualifier.VALUE_KEY, value)
      }
      val nl = ele.getChildNodes()
      for (i <- 0 until nl.getLength) {
        val node = nl.item(i)
        if (node.isInstanceOf[Element] && nodeNameEquals(node, QUALIFIER_ATTRIBUTE_ELEMENT)) {
          val attributeEle = node.asInstanceOf[Element]
          val attributeName = attributeEle.getAttribute(KEY_ATTRIBUTE)
          val attributeValue = attributeEle.getAttribute(VALUE_ATTRIBUTE)
          if (Strings.isNotEmpty(attributeName) && Strings.isNotEmpty(attributeValue)) {
            val attribute = new BeanMetadataAttribute(attributeName, attributeValue)
            attribute.setSource(extractSource(attributeEle))
            qualifier.addMetadataAttribute(attribute)
          } else {
            error("Qualifier 'attribute' tag must have a 'name' and 'value'", attributeEle)
            return
          }
        }
      }
      bd.addQualifier(qualifier)
    } finally {
      this.parseState.pop()
    }
  }

  /**
   * Get the value of a property element. May be a list etc. Also used for
   * constructor arguments, "propertyName" being null in this case.
   */
  private def parsePropertyValue(ele: Element, bd: BeanDefinition, propertyName: String): Object = {
    val elementName = if (propertyName != null) "<property> element for property '" + propertyName + "'"
    else "<constructor-arg> element"

    // Should only have one child element: ref, value, list, etc.
    val nl = ele.getChildNodes()
    var subElement: Element = null
    for (i <- 0 until nl.getLength) {
      val node = nl.item(i)
      if (node.isInstanceOf[Element] && !nodeNameEquals(node, DESCRIPTION_ELEMENT)
        && !nodeNameEquals(node, META_ELEMENT)) {
        // Child element is what we're looking for.
        if (subElement != null) error(elementName + " must not contain more than one sub-element", ele)
        else subElement = node.asInstanceOf[Element]
      }
    }

    val hasRefAttribute = ele.hasAttribute(REF_ATTRIBUTE)
    val hasValueAttribute = ele.hasAttribute(VALUE_ATTRIBUTE)
    if ((hasRefAttribute && hasValueAttribute)
      || ((hasRefAttribute || hasValueAttribute) && subElement != null)) {
      error(elementName
        + " is only allowed to contain either 'ref' attribute OR 'value' attribute OR sub-element", ele)
    }

    if (hasRefAttribute) {
      val refName = ele.getAttribute(REF_ATTRIBUTE)
      if (!StringUtils.hasText(refName)) error(elementName + " contains empty 'ref' attribute", ele)

      val ref = new RuntimeBeanReference(refName)
      ref.setSource(extractSource(ele))
      return ref
    } else if (hasValueAttribute) {
      val v = ele.getAttribute(VALUE_ATTRIBUTE)
      val src = extractSource(ele)
      if (null == propertyName && v == "?") {
        return InjectPlaceHolder
      } else {
        val valueHolder = new TypedStringValue(v)
        valueHolder.setSource(src)
        return valueHolder
      }
    } else if (subElement != null) {
      return parsePropertySubElement(subElement, bd)
    } else {
      // Neither child element nor "ref" or "value" attribute found.
      error(elementName + " must specify a ref or value", ele)
      return null
    }
  }

  /**
   * <p>
   * parsePropertySubElement.
   * </p>
   *
   */
  private def parsePropertySubElement(ele: Element, bd: BeanDefinition): Object = parsePropertySubElement(ele, bd, null)

  /**
   * Parse a value, ref or collection sub-element of a property or
   * constructor-arg element.
   */
  private def parsePropertySubElement(ele: Element, bd: BeanDefinition, defaultValueType: String): Object = {
    if (!isDefaultNamespace(getNamespaceURI(ele))) {
      error("Cannot support nested element .", ele)
      return null
    } else if (nodeNameEquals(ele, BEAN_ELEMENT)) {
      var nestedBd: BeanDefinitionHolder = parseBeanDefinitionElement(ele, bd)
      if (nestedBd != null) nestedBd = decorateBeanDefinitionIfRequired(ele, nestedBd, bd)
      return nestedBd
    } else if (nodeNameEquals(ele, REF_ELEMENT)) {
      // A generic reference to any name of any bean.
      var refName = ele.getAttribute(BEAN_REF_ATTRIBUTE)
      var toParent = false
      if (Strings.isEmpty(refName)) {
        // A reference to the id of another bean in the same XML file.
        refName = ele.getAttribute(LOCAL_REF_ATTRIBUTE)
        if (Strings.isEmpty(refName)) {
          // A reference to the id of another bean in a parent context.
          refName = ele.getAttribute(PARENT_REF_ATTRIBUTE)
          toParent = true
          if (Strings.isEmpty(refName)) {
            error("'bean', 'local' or 'parent' is required for <ref> element", ele)
            return null
          }
        }
      }
      if (!StringUtils.hasText(refName)) {
        error("<ref> element contains empty target attribute", ele)
        return null
      }
      val ref = new RuntimeBeanReference(refName, toParent)
      ref.setSource(extractSource(ele))
      return ref
    } else if (nodeNameEquals(ele, IDREF_ELEMENT)) {
      return parseIdRefElement(ele)
    } else if (nodeNameEquals(ele, VALUE_ELEMENT)) {
      return parseValueElement(ele, defaultValueType)
    } else if (nodeNameEquals(ele, NULL_ELEMENT)) {
      // It's a distinguished null value. Let's wrap it in a
      // TypedStringValue
      // object in order to preserve the source location.
      val nullHolder = new TypedStringValue(null)
      nullHolder.setSource(extractSource(ele))
      return nullHolder
    } else if (nodeNameEquals(ele, ARRAY_ELEMENT)) {
      return parseArrayElement(ele, bd)
    } else if (nodeNameEquals(ele, LIST_ELEMENT)) {
      return parseListElement(ele, bd)
    } else if (nodeNameEquals(ele, SET_ELEMENT)) {
      return parseSetElement(ele, bd)
    } else if (nodeNameEquals(ele, MAP_ELEMENT)) {
      return parseMapElement(ele, bd)
    } else if (nodeNameEquals(ele, PROPS_ELEMENT)) {
      return parsePropsElement(ele)
    } else {
      error("Unknown property sub-element: [" + ele.getNodeName() + "]", ele)
      return null
    }
  }

  /**
   * Return a typed String value Object for the given 'idref' element.
   */
  private def parseIdRefElement(ele: Element): Object = {
    // A generic reference to any name of any bean.
    var refName = ele.getAttribute(BEAN_REF_ATTRIBUTE)
    if (Strings.isEmpty(refName)) {
      // A reference to the id of another bean in the same XML file.
      refName = ele.getAttribute(LOCAL_REF_ATTRIBUTE)
      if (Strings.isEmpty(refName)) {
        error("Either 'bean' or 'local' is required for <idref> element", ele)
        return null
      }
    }
    if (!StringUtils.hasText(refName)) {
      error("<idref> element contains empty target attribute", ele)
      return null
    }
    val ref = new RuntimeBeanNameReference(refName)
    ref.setSource(extractSource(ele))
    return ref
  }

  /**
   * Return a typed String value Object for the given value element.
   */
  private def parseValueElement(ele: Element, defaultTypeName: String): Object = {
    // It's a literal value.
    val value = DomUtils.getTextValue(ele)
    val specifiedTypeName = ele.getAttribute(TYPE_ATTRIBUTE)
    var typeName = specifiedTypeName
    if (!StringUtils.hasText(typeName)) typeName = defaultTypeName
    try {
      val typedValue = buildTypedStringValue(value, typeName)
      typedValue.setSource(extractSource(ele))
      typedValue.setSpecifiedTypeName(specifiedTypeName)
      typedValue
    } catch {
      case ex: ClassNotFoundException =>
        error("Type class [" + typeName + "] not found for <value> element", ele, ex)
    }
    value
  }

  /**
   * Build a typed String value Object for the given raw value.
   */
  protected def buildTypedStringValue(value: String, targetTypeName: String): TypedStringValue = {
    if (!StringUtils.hasText(targetTypeName)) new TypedStringValue(value)
    else new TypedStringValue(value, targetTypeName)
  }

  /**
   * Parse an array element.
   */
  private def parseArrayElement(arrayEle: Element, bd: BeanDefinition): Object = {
    val elementType = arrayEle.getAttribute(VALUE_TYPE_ATTRIBUTE)
    val nl = arrayEle.getChildNodes()
    val target = new ManagedArray(elementType, nl.getLength())
    target.setSource(extractSource(arrayEle))
    target.setElementTypeName(elementType)
    target.setMergeEnabled(parseMergeAttribute(arrayEle))
    parseCollectionElements(nl, target, bd, elementType)
    return target
  }

  /**
   * Parse a list element.
   */
  private def parseListElement(collectionEle: Element, bd: BeanDefinition): java.util.List[Object] = {
    val defaultElementType = collectionEle.getAttribute(VALUE_TYPE_ATTRIBUTE)
    val nl = collectionEle.getChildNodes()
    val target = new ManagedList[Object](nl.getLength())
    target.setSource(extractSource(collectionEle))
    target.setElementTypeName(defaultElementType)
    target.setMergeEnabled(parseMergeAttribute(collectionEle))
    parseCollectionElements(nl, target, bd, defaultElementType)
    return target
  }

  /**
   * Parse a set element.
   */
  private def parseSetElement(collectionEle: Element, bd: BeanDefinition): java.util.Set[Object] = {
    val defaultElementType = collectionEle.getAttribute(VALUE_TYPE_ATTRIBUTE)
    val nl = collectionEle.getChildNodes()
    val target = new ManagedSet[Object](nl.getLength())
    target.setSource(extractSource(collectionEle))
    target.setElementTypeName(defaultElementType)
    target.setMergeEnabled(parseMergeAttribute(collectionEle))
    parseCollectionElements(nl, target, bd, defaultElementType)
    return target
  }

  /**
   * parseCollectionElements.
   */
  protected def parseCollectionElements(elementNodes: NodeList, target: java.util.Collection[Object], bd: BeanDefinition,
                                        defaultElementType: String) {
    for (i <- 0 until elementNodes.getLength) {
      val node = elementNodes.item(i)
      if (node.isInstanceOf[Element] && !nodeNameEquals(node, DESCRIPTION_ELEMENT))
        target.add(parsePropertySubElement(node.asInstanceOf[Element], bd, defaultElementType))
    }
  }

  /**
   * Parse a map element.
   */
  private def parseMapElement(mapEle: Element, bd: BeanDefinition): java.util.Map[Object, Object] = {
    val defaultKeyType = mapEle.getAttribute(KEY_TYPE_ATTRIBUTE)
    val defaultValueType = mapEle.getAttribute(VALUE_TYPE_ATTRIBUTE)

    val entryElesIter = DomUtils.getChildElementsByTagName(mapEle, ENTRY_ELEMENT).iterator()
    val map = new ManagedMap[Object, Object]()
    map.setSource(extractSource(mapEle))
    map.setKeyTypeName(defaultKeyType)
    map.setValueTypeName(defaultValueType)
    map.setMergeEnabled(parseMergeAttribute(mapEle))

    while (entryElesIter.hasNext()) {
      val entryEle = entryElesIter.next()
      // Should only have one value child element: ref, value, list, etc.
      // Optionally, there might be a key child element.
      val entrySubNodes = entryEle.getChildNodes()
      var keyEle: Element = null
      var valueEle: Element = null
      for (j <- 0 until entrySubNodes.getLength) {
        val node = entrySubNodes.item(j)
        if (node.isInstanceOf[Element]) {
          val candidateEle = node.asInstanceOf[Element]
          if (nodeNameEquals(candidateEle, KEY_ELEMENT)) {
            if (keyEle != null) error("<entry> element is only allowed to contain one <key> sub-element",
              entryEle)
            else keyEle = candidateEle
          } else {
            // Child element is what we're looking for.
            if (valueEle != null) error("<entry> element must not contain more than one value sub-element",
              entryEle)
            else valueEle = candidateEle
          }
        }
      }

      // Extract key from attribute or sub-element.
      var key: Object = null
      val hasKeyAttribute = entryEle.hasAttribute(KEY_ATTRIBUTE)
      val hasKeyRefAttribute = entryEle.hasAttribute(KEY_REF_ATTRIBUTE)
      if ((hasKeyAttribute && hasKeyRefAttribute) || ((hasKeyAttribute || hasKeyRefAttribute))
        && keyEle != null) {
        error("<entry> element is only allowed to contain either "
          + "a 'key' attribute OR a 'key-ref' attribute OR a <key> sub-element", entryEle)
      }
      if (hasKeyAttribute) {
        key = buildTypedStringValueForMap(entryEle.getAttribute(KEY_ATTRIBUTE), defaultKeyType, entryEle)
      } else if (hasKeyRefAttribute) {
        val refName = entryEle.getAttribute(KEY_REF_ATTRIBUTE)
        if (!StringUtils.hasText(refName))
          error("<entry> element contains empty 'key-ref' attribute", entryEle)

        val ref = new RuntimeBeanReference(refName)
        ref.setSource(extractSource(entryEle))
        key = ref
      } else if (keyEle != null) {
        key = parseKeyElement(keyEle, bd, defaultKeyType)
      } else {
        error("<entry> element must specify a key", entryEle)
      }

      // Extract value from attribute or sub-element.
      var value: Object = null
      val hasValueAttribute = entryEle.hasAttribute(VALUE_ATTRIBUTE)
      val hasValueRefAttribute = entryEle.hasAttribute(VALUE_REF_ATTRIBUTE)
      if ((hasValueAttribute && hasValueRefAttribute) || ((hasValueAttribute || hasValueRefAttribute))
        && valueEle != null) {
        error("<entry> element is only allowed to contain either "
          + "'value' attribute OR 'value-ref' attribute OR <value> sub-element", entryEle)
      }
      if (hasValueAttribute) {
        value = buildTypedStringValueForMap(entryEle.getAttribute(VALUE_ATTRIBUTE), defaultValueType,
          entryEle)
      } else if (hasValueRefAttribute) {
        val refName = entryEle.getAttribute(VALUE_REF_ATTRIBUTE)
        if (!StringUtils.hasText(refName)) {
          error("<entry> element contains empty 'value-ref' attribute", entryEle)
        }
        val ref = new RuntimeBeanReference(refName)
        ref.setSource(extractSource(entryEle))
        value = ref
      } else if (valueEle != null) {
        value = parsePropertySubElement(valueEle, bd, defaultValueType)
      } else {
        error("<entry> element must specify a value", entryEle)
      }
      map.put(key, value)
    }

    return map
  }

  /**
   * Build a typed String value Object for the given raw value.
   */
  protected final def buildTypedStringValueForMap(value: String, defaultTypeName: String, entryEle: Element): Object = {
    try {
      val typedValue = buildTypedStringValue(value, defaultTypeName)
      typedValue.setSource(extractSource(entryEle))
      return typedValue
    } catch {
      case ex: ClassNotFoundException =>
        error("Type class [" + defaultTypeName + "] not found for Map key/value type", entryEle, ex)
        return value
    }
  }

  /**
   * Parse a key sub-element of a map element.
   */
  protected def parseKeyElement(keyEle: Element, bd: BeanDefinition, defaultKeyTypeName: String): Object = {
    val nl = keyEle.getChildNodes()
    var subElement: Element = null
    for (i <- 0 until nl.getLength) {
      val node = nl.item(i)
      if (node.isInstanceOf[Element]) {
        // Child element is what we're looking for.
        if (subElement != null) error("<key> element must not contain more than one value sub-element",
          keyEle)
        else subElement = node.asInstanceOf[Element]
      }
    }
    return parsePropertySubElement(subElement, bd, defaultKeyTypeName)
  }

  /**
   * Parse a props element.
   */
  private def parsePropsElement(propsEle: Element): java.util.Properties = {
    val props = new ManagedProperties()
    props.setSource(extractSource(propsEle))
    props.setMergeEnabled(parseMergeAttribute(propsEle))

    val propElesIter = DomUtils.getChildElementsByTagName(propsEle, PROP_ELEMENT).iterator()
    while (propElesIter.hasNext()) {
      val propEle = propElesIter.next()
      val key = propEle.getAttribute(KEY_ATTRIBUTE)
      // Trim the text value to avoid unwanted whitespace
      // caused by typical XML formatting.
      val value = DomUtils.getTextValue(propEle).trim()
      val keyHolder = new TypedStringValue(key)
      keyHolder.setSource(extractSource(propEle))
      val valueHolder = new TypedStringValue(value)
      valueHolder.setSource(extractSource(propEle))
      props.put(keyHolder, valueHolder)
    }
    return props
  }

  /**
   * Parse the merge attribute of a collection element, if any.
   *
   */
  private def parseMergeAttribute(collectionElement: Element) = collectionElement.getAttribute(MERGE_ATTRIBUTE) == TRUE_VALUE

  private def decorateBeanDefinitionIfRequired(ele: Element, holder: BeanDefinitionHolder): BeanDefinitionHolder = decorateBeanDefinitionIfRequired(ele, holder, null)

  private def decorateBeanDefinitionIfRequired(ele: Element, definitionHolder: BeanDefinitionHolder, containingBd: BeanDefinition): BeanDefinitionHolder = {
    var finalDefinition = definitionHolder

    // Decorate based on custom attributes first.
    val attributes = ele.getAttributes()
    for (i <- 0 until attributes.getLength) {
      val node = attributes.item(i)
      finalDefinition = decorateIfRequired(node, finalDefinition, containingBd)
    }

    // Decorate based on custom nested elements.
    val children = ele.getChildNodes()
    for (i <- 0 until children.getLength) {
      val node = children.item(i)
      if (node.getNodeType() == Node.ELEMENT_NODE)
        finalDefinition = decorateIfRequired(node, finalDefinition, containingBd)
    }
    return finalDefinition
  }

  private def decorateIfRequired(node: Node, originalDef: BeanDefinitionHolder, containingBd: BeanDefinition) = originalDef

  private def isDefaultNamespace(namespaceUri: String): Boolean = Strings.isEmpty(namespaceUri) || BEANS_NAMESPACE_URI == namespaceUri

  private def getNamespaceURI(node: Node): String = node.getNamespaceURI()

  private def nodeNameEquals(node: Node, desiredName: String): Boolean = desiredName.equals(node.getNodeName()) || desiredName.equals(getLocalName(node))

  private def getLocalName(node: Node): String = node.getLocalName
}
