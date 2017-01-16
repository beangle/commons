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

import java.{ util => ju }

import org.beangle.commons.cdi.spring.beans.{ ScalaBeanInfoFactory, ScalaEditorRegistrar, FactoryBeanProxy }
import org.beangle.commons.cdi.spring.context.HierarchicalEventMulticaster
import org.beangle.commons.bean.{ Disposable, Initializing, Factory }
import org.beangle.commons.collection.Collections
import org.beangle.commons.event.EventListener
import org.beangle.commons.cdi.{ BeanNamesEventMulticaster, ContainerListener, PropertySource, Scope }
import org.beangle.commons.cdi.bind.{ BindRegistry, Binder }
import org.beangle.commons.cdi.bind.{ Module, nowire, profile }
import org.beangle.commons.cdi.bind.Binder.{ Definition, InjectPlaceHolder, Injection, PropertyPlaceHolder, ReferenceValue }
import org.beangle.commons.config.Resources
import org.beangle.commons.io.IOs
import org.beangle.commons.lang.{ ClassLoaders, Strings, SystemInfo }
import org.beangle.commons.lang.annotation.description
import org.beangle.commons.lang.reflect.{ CollectionType, ConstructorDescriptor, ElementType, MapType, Reflections, TypeInfo }
import org.beangle.commons.lang.time.Stopwatch
import org.beangle.commons.logging.Logging
import org.springframework.beans.MutablePropertyValues
import org.springframework.beans.factory.FactoryBean
import org.springframework.beans.factory.config.{ BeanDefinition, ConfigurableListableBeanFactory, ConstructorArgumentValues, RuntimeBeanReference, TypedStringValue }
import org.springframework.beans.factory.support.{ AbstractBeanDefinition, BeanDefinitionRegistry, BeanDefinitionRegistryPostProcessor, GenericBeanDefinition, ManagedList, ManagedMap, ManagedProperties, ManagedSet }
import org.springframework.core.io.{ Resource, UrlResource }
import scala.collection.JavaConverters

/**
 * 完成bean的自动注册和再配置
 *
 * @author chaostone
 */
abstract class BindModuleProcessor extends BeanDefinitionRegistryPostProcessor with Logging {

  var name: String = "default"

  var moduleLocations: Array[Resource] = Array.empty

  var reconfigLocations: String = _

  var modules: Set[Module] = Set.empty

  private var properties: Map[String, String] = Map.empty

  private var reconfigBeans: List[ReconfigBeanDefinitionHolder] = List.empty

  //nowire beanname -> properties
  private val nowires = Collections.newMap[String, collection.Set[String]]
  /**
   * Automate register and wire bean<br/>
   * Reconfig beans
   */
  override def postProcessBeanDefinitionRegistry(definitionRegistry: BeanDefinitionRegistry) {
    // find bean definition by code
    val registry = new SpringBindRegistry(definitionRegistry)
    readConfig()
    val newDefinitions = registerModules(registry)
    //reconfig all bean by spring-config.xml
    reconfig(definitionRegistry, registry)
    //register beangle factory
    registerBeangleFactory(definitionRegistry, registry)
    //register last one
    registerLast(registry)

    // support initializing/disposable
    lifecycle(registry, definitionRegistry)
    // wire by constructor/properties
    autowire(newDefinitions, registry)

    // add container description
    val meType = this.getClass
    registry.getBeanNames(meType) foreach { containerName =>
      val containerDef = definitionRegistry.getBeanDefinition(containerName)
      if (null == containerDef.getDescription) {
        containerDef match {
          case abDef: AbstractBeanDefinition => abDef.setDescription(getClassDescription(meType))
          case _                             =>
        }
      }
    }
    properties = Map.empty
    reconfigBeans = List.empty
    nowires.clear()
    ScalaBeanInfoFactory.BeanInfos.clear()
  }

  def postProcessBeanFactory(factory: ConfigurableListableBeanFactory) {
    factory.registerCustomEditor(classOf[Resources], classOf[ResourcesEditor])
    factory.addPropertyEditorRegistrar(new ScalaEditorRegistrar)
  }

  /**
   * Read spring-config.xml
   */
  private def readConfig(): Unit = {
    if (null != reconfigLocations) {
      val re = new ResourcesEditor()
      re.setAsText(reconfigLocations)
      val reconfigResources = re.getValue().asInstanceOf[Resources]
      val reconfigBeansBuilder = new collection.mutable.ListBuffer[ReconfigBeanDefinitionHolder]
      val reader = new BeanDefinitionReader()
      for (url <- reconfigResources.paths) {
        val watch = new Stopwatch(true)
        val holders = reader.load(new UrlResource(url))
        for (holder <- holders) {
          val beanName = holder.getBeanName
          if (beanName == "properties") {
            val value = holder.getBeanDefinition().getPropertyValues().getPropertyValue("value").getValue
            this.properties ++= Strings.split(value.toString.trim, '\n').map { line =>
              val eqIndex = line.indexOf("=")
              (line.substring(0, eqIndex).trim() -> line.substring(eqIndex + 1).trim())
            }.toMap
          } else {
            reconfigBeansBuilder += holder
          }
        }
        logger.info(s"Read $url in $watch")
      }
      reconfigBeans = reconfigBeansBuilder.toList
    }

    properties ++= SystemInfo.properties
    val profile = properties.get(Module.profileProperty).orNull
    val profiles = if (null == profile) Set.empty[String] else Strings.split(profile, ",").map(s => s.trim).toSet

    val moduleSet = new collection.mutable.HashSet[Module]
    val effectiveLocations = Collections.newBuffer[Resource]
    moduleLocations foreach { r =>
      val is = r.getInputStream

      (scala.xml.XML.load(is) \ "container") foreach { con =>
        var containerName = (con \ "@name").text
        if (Strings.isEmpty(containerName)) containerName = "default"
        if (containerName == this.name) {
          effectiveLocations += r
          (con \ "module") foreach { moduleElem =>
            val module = loadModule((moduleElem \ "@class").text)
            val anno = module.getClass.getAnnotation(classOf[profile])
            if (null == anno || null != anno && new ProfileMatcher(anno.value).matches(profiles)) {
              module match {
                case ps: PropertySource => this.properties ++= ps.properties
                case _                  =>
              }
              moduleSet += module
            }
          }
        }
      }
      IOs.close(is)
    }
    this.moduleLocations = effectiveLocations.toArray
    this.modules = moduleSet.toSet
  }

  private def loadModule(name: String): Module = {
    var moduleClass = ClassLoaders.load(name)
    if (!classOf[Module].isAssignableFrom(moduleClass)) {
      ClassLoaders.get(name + "$") match {
        case Some(clazz) => moduleClass = clazz
        case None        => throw new RuntimeException(name + " is not a module")
      }
    }
    if (moduleClass.getConstructors.length > 0) moduleClass.newInstance().asInstanceOf[Module]
    else moduleClass.getDeclaredField("MODULE$").get(null).asInstanceOf[Module]
  }

  private def reconfig(registry: BeanDefinitionRegistry, bindRegistry: BindRegistry) {
    val watch = new Stopwatch(true)
    val beanNames = new collection.mutable.HashSet[String]
    for (holder <- reconfigBeans) {
      val beanName = holder.getBeanName
      // choose primary key
      if (holder.configType.equals(ReconfigType.Primary)) {
        val clazz = ClassLoaders.load(holder.getBeanDefinition().getBeanClassName)
        if (clazz.isInterface) {
          val names = bindRegistry.getBeanNames(clazz)
          if (names.contains(beanName)) {
            for (name <- names) bindRegistry.setPrimary(name, name == beanName, registry.getBeanDefinition(name))
          }
        }
        // lets do property update and merge.
        holder.configType = ReconfigType.Update
        holder.getBeanDefinition().setBeanClassName(null)
      }

      if (holder.configType == ReconfigType.Update) {
        if (registry.containsBeanDefinition(beanName)) {
          val successName = mergeDefinition(registry.getBeanDefinition(beanName), holder)
          if (null != successName) beanNames += successName
        } else {
          logger.warn(s"No bean $beanName to reconfig")
        }
      }
    }
    if (!beanNames.isEmpty) logger.info(s"Reconfig $beanNames in $watch")
  }

  /**
   *  Try find bean implements factory interface,and convert to spring FactoryBean[_]
   */
  private def registerBeangleFactory(definitionRegistry: BeanDefinitionRegistry, registry: BindRegistry) {
    for (name <- definitionRegistry.getBeanDefinitionNames if !(name.startsWith("&"))) {
      val defn = definitionRegistry.getBeanDefinition(name).asInstanceOf[AbstractBeanDefinition]
      if (!defn.isAbstract()) {
        val clazz = SpringBindRegistry.getBeanClass(definitionRegistry, name)
        // convert factory to spring factorybean
        if (classOf[Factory[_]].isAssignableFrom(clazz) && !classOf[FactoryBean[_]].isAssignableFrom(clazz)) {
          val proxy = new GenericBeanDefinition()
          proxy.setBeanClass(classOf[FactoryBeanProxy[_]])
          proxy.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_NO)
          proxy.setScope(defn.getScope)
          proxy.setPrimary(defn.isPrimary)
          registry.register(name + "#proxy", clazz, defn)
          val objectTypes = Reflections.getGenericParamType(clazz, classOf[Factory[_]])
          if (objectTypes.isEmpty) throw new RuntimeException(s"Cannot find factory object type of class ${clazz.getName}")
          val objectType = objectTypes.values.head
          proxy.getPropertyValues().add("target", new RuntimeBeanReference(name + "#proxy"))
          proxy.getPropertyValues().add("objectType", objectType)
          val description = getClassDescription(clazz)
          proxy.setDescription(if (null != description) description + "的Spring代理" else null)
          registry.register(name, objectType, proxy)
          registry.register("&" + name, classOf[FactoryBeanProxy[_]])
        }
      }
    }
  }
  /**
   * lifecycle.
   */
  private def lifecycle(registry: BindRegistry, definitionRegistry: BeanDefinitionRegistry) {
    registry.beanNames foreach { name =>
      val clazz = registry.getBeanType(name)
      val springName = if (name.startsWith("&")) name.substring(1) else name
      if (definitionRegistry.containsBeanDefinition(springName)) {
        val defn = definitionRegistry.getBeanDefinition(springName).asInstanceOf[AbstractBeanDefinition]
        // convert Initializing to init-method
        if (classOf[Initializing].isAssignableFrom(clazz) && null == defn.getInitMethodName()
          && !defn.getPropertyValues().contains("init-method")) {
          defn.setInitMethodName("init")
        }
        // convert Disposable to destry-method
        if (classOf[Disposable].isAssignableFrom(clazz) && null == defn.getDestroyMethodName()
          && !defn.getPropertyValues().contains("destroy-method")) {
          defn.setDestroyMethodName("destroy")
        }
      }
    }
  }

  /**
   * register last buildin beans.
   */
  private def registerLast(registry: BindRegistry) {
    val listenerBeans = registry.getBeanNames(classOf[EventListener[_]])
    val eventMulticaster = new Definition("EventMulticaster.default" + System.currentTimeMillis(), classOf[HierarchicalEventMulticaster], Scope.Singleton.toString)
    eventMulticaster.description = getClassDescription(classOf[BeanNamesEventMulticaster])
    eventMulticaster.property("container", this)
    registry.getBeanNames(classOf[HierarchicalEventMulticaster]).foreach { parentName =>
      eventMulticaster.property("parent", new RuntimeBeanReference(parentName))
      eventMulticaster.primary = true
    }
    registerBean(eventMulticaster, registry)
  }

  /**
   * 合并bean定义
   */
  private def mergeDefinition(target: BeanDefinition, source: ReconfigBeanDefinitionHolder): String = {
    if (null == target.getBeanClassName()) {
      logger.warn(s"ingore bean definition ${source.getBeanName} for without class")
      return null
    }
    val sourceDefn = source.getBeanDefinition
    // 当类型变化后,删除原有配置
    if (null != sourceDefn.getBeanClassName && sourceDefn.getBeanClassName != target.getBeanClassName) {
      target.setBeanClassName(sourceDefn.getBeanClassName)
      target.asInstanceOf[GenericBeanDefinition].setDescription(getClassDescription(ClassLoaders.load(sourceDefn.getBeanClassName)))
      for (pv <- target.getPropertyValues().getPropertyValues) {
        target.getPropertyValues().removePropertyValue(pv)
      }
    }
    val pvs = sourceDefn.getPropertyValues
    for (pv <- JavaConverters.collectionAsScalaIterable(pvs.getPropertyValueList)) {
      val name = pv.getName()
      target.getPropertyValues().addPropertyValue(name, pv.getValue)
      logger.debug(s"config ${source.getBeanName}.$name = ${pv.getValue}")
    }
    if (!sourceDefn.getConstructorArgumentValues.isEmpty) {
      target.asInstanceOf[GenericBeanDefinition].setConstructorArgumentValues(sourceDefn.getConstructorArgumentValues)
    }
    logger.debug(s"Reconfig bean ${source.getBeanName} ")
    source.getBeanName
  }

  /**
   * registerModules.
   */
  private def registerModules(registry: BindRegistry): Map[String, BeanDefinition] = {
    val watch = new Stopwatch(true)
    val definitions = new collection.mutable.HashMap[String, Definition]
    val singletons = new collection.mutable.HashMap[String, AnyRef]
    val bean2profiles = new collection.mutable.HashMap[String, profile]

    modules foreach { module =>
      logger.info(s"Binding ${module.getClass.getName}")
      val binder = new Binder(module.getClass.getName)
      module.configure(binder)
      val profile = module.getClass().getAnnotation(classOf[profile])

      binder.singletons foreach { e =>
        val beanName = e._1
        if (singletons.contains(beanName)) {
          if (null != profile && bean2profiles.get(beanName).isEmpty) {
            singletons.put(beanName, e._2)
            bean2profiles.put(beanName, profile)
          } else {
            logger.warn(s"Ingore exists bean definition $beanName in ${module.getClass.getName}")
          }
        } else {
          singletons.put(beanName, e._2)
          bean2profiles.put(beanName, profile)
        }
      }

      for (definition <- binder.definitions) {
        val beanName = definition.beanName
        nowires.put(beanName, definition.nowires)
        if (definitions.contains(beanName)) {
          if (null != profile && bean2profiles.get(beanName).isEmpty) {
            definitions.put(beanName, definition)
            bean2profiles.put(beanName, profile)
          } else {
            logger.warn(s"Ingore exists bean definition $beanName in ${module.getClass.getName}")
          }
        } else {
          definitions.put(beanName, definition)
          if (null != profile) bean2profiles.put(beanName, profile)
        }
      }
    }

    var newBeanCount = 0
    singletons foreach {
      case (beanName, singleton) =>
        if (registry.contains(beanName)) logger.warn(s"Ingore exists bean definition $beanName")
        else {
          registry.register(beanName, singleton)
          newBeanCount += 1
        }
    }
    val beanDefinitions = new collection.mutable.HashMap[String, BeanDefinition]
    definitions foreach {
      case (beanName, definition) =>
        if (registry.contains(beanName)) logger.warn(s"Ingore exists bean definition $beanName")
        else {
          beanDefinitions.put(beanName, registerBean(definition, registry))
          newBeanCount += 1
        }
    }
    logger.info(s"Auto register $newBeanCount beans in $watch")
    beanDefinitions.toMap
  }

  private def convert(definition: Definition): BeanDefinition = {
    val defn = new GenericBeanDefinition()
    defn.setBeanClass(definition.clazz)
    defn.setScope(definition.scope)
    if (null != definition.initMethod) defn.setInitMethodName(definition.initMethod)
    val mpv = new MutablePropertyValues()
    for ((key, v) <- definition.properties) mpv.add(key, convertValue(v))

    defn.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_NO)
    defn.setLazyInit(definition.lazyInit)
    defn.setAbstract(definition.isAbstract)
    defn.setParentName(definition.parent)
    defn.setPrimary(definition.primary)
    defn.setDescription(definition.description)
    if (null != definition.constructorArgs) {
      val cav = defn.getConstructorArgumentValues
      definition.constructorArgs.map(arg => cav.addGenericArgumentValue(convertValue(arg)))
    }
    defn.setPropertyValues(mpv)
    defn
  }

  private def convertValue(v: Any): AnyRef = {
    v match {
      case value: List[_] =>
        val list = new ManagedList[Any]
        value foreach { item =>
          item match {
            case rv: ReferenceValue => list.add(new RuntimeBeanReference(rv.ref))
            case _                  => list.add(item)
          }
        }
        list
      case value: Set[_] =>
        val set = new ManagedSet[Any]
        value foreach { item =>
          set.add(item match {
            case rv: ReferenceValue => new RuntimeBeanReference(rv.ref)
            case _                  => item
          })
        }
        set
      case value: ju.Properties =>
        val props = new ManagedProperties()
        val propertyNames = value.propertyNames()
        while (propertyNames.hasMoreElements()) {
          val key = propertyNames.nextElement().toString
          props.put(new TypedStringValue(key), new TypedStringValue(value.getProperty(key)))
        }
        props
      case value: Map[_, _] =>
        val maps = new ManagedMap[Any, Any]
        value foreach {
          case (itemk, itemv) =>
            itemv match {
              case rv: ReferenceValue => maps.put(itemk, new RuntimeBeanReference(rv.ref))
              case _                  => maps.put(itemk, convertValue(itemv))
            }
        }
        maps
      case value: Definition     => new RuntimeBeanReference(value.beanName)
      case value: ReferenceValue => new RuntimeBeanReference(value.ref)
      case PropertyPlaceHolder(name, defaultValue) =>
        properties.get(name) match {
          case Some(v) => v
          case None    => if (null == defaultValue) "${" + name + "}" else defaultValue
        }
      case value: AnyRef => value
    }
  }

  /**
   * registerBean.
   */
  private def registerBean(defn: Definition, registry: BindRegistry): BeanDefinition = {
    val bd = convert(defn)
    //register spring factory bean
    if (classOf[FactoryBean[_]].isAssignableFrom(defn.clazz)) {
      var target = defn.targetClass
      if (null == target && !defn.isAbstract) target = defn.clazz.newInstance().asInstanceOf[FactoryBean[_]].getObjectType
      registry.register(defn.beanName, target, bd)
      // register concrete factory bean
      if (!defn.isAbstract) registry.register("&" + defn.beanName, defn.clazz)
    } else {
      registry.register(defn.beanName, defn.clazz, bd)
    }
    logger.debug(s"Register definition ${defn.beanName} for ${defn.clazz}")
    bd
  }

  /**
   * autowire bean by constructor and properties.
   *
   * <ul>policy
   * <li>find unique dependency
   * <li>find primary type of dependency
   * </ul>
   */
  private def autowire(newBeanDefinitions: Map[String, BeanDefinition], registry: BindRegistry) {
    val watch = new Stopwatch(true)
    for ((name, bd) <- newBeanDefinitions) autowireBean(name, bd, registry)
    logger.info(s"Autowire ${newBeanDefinitions.size} beans using $watch")
  }

  /**
   * convert typeinfo into ReferenceValue
   */
  private def convertInjectValue(typeinfo: TypeInfo, registry: BindRegistry, excludeBeanName: String): AnyRef = {
    val result =
      typeinfo match {
        case ElementType(clazz,optional) => Injection(clazz)
        case CollectionType(clazz, componentType) =>
          if (componentType == classOf[AnyRef]) List.empty
          else {
            val result = registry.getBeanNames(componentType) filterNot (n => n == excludeBeanName) map (bn => new RuntimeBeanReference(bn))
            if (typeinfo.asInstanceOf[CollectionType].isSetType) result.toSet else result.toList
          }
        case MapType(clazz, keyType, valueType) =>
          if (keyType == classOf[String]) {
            if (valueType == classOf[AnyRef]) Map.empty
            else registry.getBeanNames(valueType).filterNot(n => n == excludeBeanName).map(bn => (bn, new RuntimeBeanReference(bn))).toMap
          } else {
            Map.empty
          }
      }
    convertValue(result)
  }
  /**
   * autowire single bean.
   */
  private def autowireBean(beanName: String, mbd: BeanDefinition, registry: BindRegistry) {
    val clazz = SpringBindRegistry.getBeanClass(mbd)
    val manifest = ScalaBeanInfoFactory.BeanInfos.get(clazz)
    //1. inject constructor
    // find only constructor or constructor with same parameters count
    var ctor: ConstructorDescriptor = {
      val ctors = manifest.constructors
      if (mbd.getConstructorArgumentValues.isEmpty) {
        if (ctors.length == 1) ctors.head
        else null
      } else {
        val argLength = mbd.getConstructorArgumentValues.getArgumentCount()
        ctors.find(ctor => ctor.constructor.getParameterTypes.size == argLength).getOrElse(null)
      }
    }

    if (null != ctor && mbd.isInstanceOf[GenericBeanDefinition]) {
      // doesn't have arguments
      if (mbd.getConstructorArgumentValues.isEmpty) {
        val cav = mbd.getConstructorArgumentValues
        (0 until ctor.args.length) foreach { i =>
          val typeinfo = ctor.args(i)
          manifest.defaultConstructorParams.get(i + 1) match {
            case Some(v) => cav.addGenericArgumentValue(v)
            case None    => cav.addGenericArgumentValue(convertInjectValue(typeinfo, registry, beanName))
          }
        }
      } else {
        // check have inject place holder
        val cav = new ConstructorArgumentValues
        var i = 0
        val paramTypes = ctor.args
        val itor = mbd.getConstructorArgumentValues.getGenericArgumentValues.iterator
        while (itor.hasNext) {
          val v = itor.next.getValue
          cav.addGenericArgumentValue(
            v match {
              case InjectPlaceHolder => if (i < paramTypes.length) convertInjectValue(paramTypes(i), registry, beanName) else null
              case _                 => v
            })
          i += 1
        }
        mbd.asInstanceOf[GenericBeanDefinition].setConstructorArgumentValues(cav)
      }
    }
    // inject constructor by parameter type
    if (!mbd.getConstructorArgumentValues().isEmpty()) {
      val cav = new ConstructorArgumentValues
      val itor = mbd.getConstructorArgumentValues.getGenericArgumentValues.iterator
      while (itor.hasNext) {
        val v = itor.next.getValue
        v match {
          case Injection(argClass) =>
            val beanNames = registry.getBeanNames(argClass)
            if (beanNames.size == 1) {
              cav.addGenericArgumentValue(new RuntimeBeanReference(beanNames(0)))
            } else if (beanNames.size > 1) {
              beanNames.find { name => registry.isPrimary(name) } match {
                case Some(name) => cav.addGenericArgumentValue(new RuntimeBeanReference(name))
                case None       => throw new RuntimeException(s"Cannot wire bean ${mbd.getBeanClassName}, find candinates $beanNames of ${argClass.getName}")
              }
            } else {
              throw new RuntimeException(s"Cannot wire bean $mbd.name,cannot find dependency bean of type ${argClass.getName}")
            }
          case _ => cav.addGenericArgumentValue(v)
        }
      }
      mbd.asInstanceOf[GenericBeanDefinition].setConstructorArgumentValues(cav)
    }

    //2. inject properties
    val properties = unsatisfiedNonSimpleProperties(mbd, beanName)
    for ((propertyName, propertyType) <- properties) {
      propertyType match {
        case ElementType(clazz,optional) =>
          val beanNames = registry.getBeanNames(propertyType.clazz)
          var binded = false
          if (beanNames.size == 1) {
            mbd.getPropertyValues().add(propertyName, new RuntimeBeanReference(beanNames(0)))
            binded = true
          } else if (beanNames.size > 1) {
            // first autowire by name
            for (name <- beanNames if binded == false) {
              if (name.equals(propertyName)) {
                mbd.getPropertyValues().add(propertyName, new RuntimeBeanReference(name))
                binded = true
              }
            }
            // second autowire by primary
            if (!binded) {
              for (name <- beanNames if binded == false) {
                if (registry.isPrimary(name)) {
                  mbd.getPropertyValues().add(propertyName, new RuntimeBeanReference(name))
                  binded = true
                }
              }
            }
            // third autowire by default
            if (!binded) {
              for (name <- beanNames if binded == false) {
                if (name.endsWith(".default")) {
                  mbd.getPropertyValues.add(propertyName, new RuntimeBeanReference(name))
                  binded = true
                }
              }
            }
          }
          if (!binded) {
            if (beanNames.isEmpty) logger.debug(s"$beanName's $propertyName cannot found candidate beans.")
            else logger.warn(s"$beanName's $propertyName expected single bean but found ${beanNames.size} : $beanNames")
          }
        case _ =>
          val v = convertInjectValue(propertyType, registry, beanName)
          v match {
            case jc: java.util.Collection[_] => if (!jc.isEmpty) mbd.getPropertyValues.add(propertyName, jc)
            case jm: java.util.Map[_, _]     => if (!jm.isEmpty) mbd.getPropertyValues.add(propertyName, jm)
          }
      }
    }
  }

  /**
   * Find unsatisfied properties<br>
   * Unsatisfied property is empty value and not primary type and not starts with java.
   */
  private def unsatisfiedNonSimpleProperties(mbd: BeanDefinition, beanName: String): collection.Map[String, TypeInfo] = {
    val properties = new collection.mutable.HashMap[String, TypeInfo]
    val bd = mbd.asInstanceOf[GenericBeanDefinition]
    val clazz = SpringBindRegistry.getBeanClass(bd)
    if (!mbd.isAbstract) {
      val pvs = mbd.getPropertyValues
      val nowireProperties = nowires.get(beanName).getOrElse(Set.empty[String])
      for ((name, m) <- ScalaBeanInfoFactory.BeanInfos.get(clazz).properties) {
        if (m.writable && !nowireProperties.contains(name)) {
          val method = m.setter.get
          val typeinfo = m.typeinfo
          if (null == method.getAnnotation(classOf[nowire]) && !pvs.contains(name)) {
            if (typeinfo.isElementType) {
              if (!typeinfo.clazz.getName.startsWith("java.") && !typeinfo.clazz.getName.startsWith("scala.")) {
                //Skip Factory.result method for it's a provider,DONOT need wire
                if (!(name == "result" && classOf[Factory[_]].isAssignableFrom(clazz))) properties.put(name, typeinfo)
              }
            } else {
              if (!classOf[ContainerListener].isAssignableFrom(clazz)) properties.put(name, typeinfo)
            }
          }
        }
      }
    }
    properties
  }

  private def autowireable(clazz: Class[_]): Boolean = {
    !clazz.getName.startsWith("java.") && !clazz.getName.startsWith("scala.")
  }

  private def getClassDescription(clazz: Class[_]): String = {
    val containerDescription = clazz.getAnnotation(classOf[description])
    if (null == containerDescription) null else containerDescription.value()
  }
}
