package org.beangle.commons.text.i18n

import java.util.Locale

object Messages {
  def apply(locale: Locale): Messages = {
    new Messages(locale, new DefaultTextBundleRegistry(), new DefaultTextFormater())
  }
}

class Messages(locale: Locale, val registry: TextBundleRegistry, val format: TextFormater) {
  def get(clazz: Class[_], key: String): String = {
    if (key == "class") {
      val bundle = registry.load(locale, clazz.getPackage.getName + ".package")
      bundle.get(clazz.getSimpleName).orNull
    } else {
      new HierarchicalTextResource(clazz, locale, registry, format)(key).orNull
    }
  }
}

