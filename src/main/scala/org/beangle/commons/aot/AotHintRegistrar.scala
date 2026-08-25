package org.beangle.commons.aot

import org.beangle.commons.lang.Registrar

/** Registers ahead-of-time hints for GraalVM native-image.
 *
 * Implementations declare reflection types, resource patterns, proxy interfaces,
 * and serializable classes by calling methods on [[hints]]. The collected hints
 * are exposed via [[aotHints]] for use by [[AotHintGenerator]].
 *
 * {{{
 * class MyHints extends AotHintRegistrar {
 *   override def registering(): Unit = {
 *     hints.registerType(classOf[User], classOf[Role])
 *     hints.registerPattern("META-INF/custom.idx")
 *     hints.registerProxy(classOf[UserService])
 *     hints.registerSerializable(classOf[UserDto])
 *   }
 * }
 * }}}
 *
 * [[org.beangle.commons.bean.meta.MetaRegistrar]] extends this trait, adding
 * compile-time BeanMeta registration via `register` macro. Its `registering()`
 * bridges metamodel classes into [[hints]] automatically.
 */
abstract class AotHintRegistrar extends Registrar {

  protected val hints = new AotHints

  /** Returns the collected hints for config file generation. */
  final def aotHints: AotHints = hints
}
