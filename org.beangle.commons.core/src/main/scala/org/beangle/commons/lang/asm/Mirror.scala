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
package org.beangle.commons.lang.asm

import org.objectweb.asm.Opcodes._
import org.beangle.commons.lang.reflect.ClassInfo
import org.beangle.commons.lang.reflect.MethodInfo
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import scala.collection.mutable
object Mirror {

  private var proxies =new mutable.HashMap[Class[_], Mirror]

  /**
   * Get Mirror of given type.
   * <p>
   * First,it search from proxies cache,if not found, then generate new proxy class using asm.
   */
  def get(clazz: Class[_]): Mirror = {
    var proxy = proxies.get(clazz).orNull
    if (null != proxy) return proxy
    proxies.synchronized {
      proxy = proxies.get(clazz).orNull
      if (null != proxy) return proxy
      val classInfo = ClassInfo.get(clazz)
      val className = clazz.getName
      var accessClassName = className + "Mirror"
      if (accessClassName.startsWith("java.")) accessClassName = "beangle." + accessClassName
      var accessClass: Class[_] = null
      val loader = MirrorClassLoader.get(clazz)
      if (null == loader) return Mirrors.none()
      try {
        accessClass = loader.loadClass(accessClassName)
      } catch {
        case ignored: ClassNotFoundException => {
          val accessClassNameInternal = accessClassName.replace('.', '/')
          val classNameInternal = className.replace('.', '/')
          val cw = new ClassWriter(ClassWriter.COMPUTE_MAXS)
          cw.visit(V1_1, ACC_PUBLIC + ACC_SUPER, accessClassNameInternal, null, "org/beangle/commons/lang/asm/Mirror",
            null)
          val initMv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null)
          initMv.visitCode()
          initMv.visitVarInsn(ALOAD, 0)
          initMv.visitMethodInsn(INVOKESPECIAL, "org/beangle/commons/lang/asm/Mirror", "<init>", "()V")
          initMv.visitInsn(RETURN)
          initMv.visitMaxs(0, 0)
          initMv.visitEnd()
          visitRead(cw,classInfo,classNameInternal)
          visitWrite(cw,classInfo,classNameInternal)
          visitInvoke(cw,classInfo,classNameInternal)

          cw.visitEnd()
          val data = cw.toByteArray()
          try {
            (new java.io.FileOutputStream("/tmp/" + accessClassName + ".class"))
              .write(data)
          } catch {
            case e: Exception =>
          }
          accessClass = loader.defineClass(accessClassName, data)
        }
      }
      proxy = accessClass.newInstance().asInstanceOf[Mirror]
      proxy.classInfo = classInfo
      proxies.put(clazz, proxy)
      return proxy
    }
  }

  private def box(clazz:Class[_],mv:MethodVisitor):Type={
    val t= Type.getType(clazz)
    t.getSort match {
      case Type.VOID => mv.visitInsn(ACONST_NULL)
      case Type.BOOLEAN => mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf","(Z)Ljava/lang/Boolean;")
      case Type.BYTE => mv.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;")
      case Type.CHAR => mv.visitMethodInsn(INVOKESTATIC, "java/lang/Character", "valueOf",
        "(C)Ljava/lang/Character;")
      case Type.SHORT => mv.visitMethodInsn(INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;")
      case Type.INT => mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;")
      case Type.FLOAT => mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;")
      case Type.LONG => mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;")
      case Type.DOUBLE => mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;")
      case _ =>
    }
    t
  }

  private def unbox(clazz:Class[_],mv:MethodVisitor):Type={
    val t = Type.getType(clazz)
    t.getSort match {
      case Type.BOOLEAN =>
        mv.visitTypeInsn(CHECKCAST, "java/lang/Boolean")
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z")

      case Type.BYTE =>
        mv.visitTypeInsn(CHECKCAST, "java/lang/Byte")
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Byte", "byteValue", "()B")

      case Type.CHAR =>
        mv.visitTypeInsn(CHECKCAST, "java/lang/Character")
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Character", "charValue", "()C")

      case Type.SHORT =>
        mv.visitTypeInsn(CHECKCAST, "java/lang/Short")
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Short", "shortValue", "()S")

      case Type.INT =>
        mv.visitTypeInsn(CHECKCAST, "java/lang/Integer")
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I")

      case Type.FLOAT =>
        mv.visitTypeInsn(CHECKCAST, "java/lang/Float")
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Float", "floatValue", "()F")

      case Type.LONG =>
        mv.visitTypeInsn(CHECKCAST, "java/lang/Long")
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J")

      case Type.DOUBLE =>
        mv.visitTypeInsn(CHECKCAST, "java/lang/Double")
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Double", "doubleValue", "()D")

      case Type.ARRAY => mv.visitTypeInsn(CHECKCAST, t.getDescriptor)
      case Type.OBJECT => mv.visitTypeInsn(CHECKCAST, t.getInternalName)
    }
    t
  }

  private def visitException(mv:MethodVisitor){
    mv.visitTypeInsn(NEW, "java/lang/IllegalArgumentException")
    mv.visitInsn(DUP)
    mv.visitTypeInsn(NEW, "java/lang/StringBuilder")
    mv.visitInsn(DUP)
    mv.visitLdcInsn("Method not found: ")
    mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V")
    mv.visitVarInsn(ILOAD, 2)
    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(I)Ljava/lang/StringBuilder;")
    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;")
    mv.visitMethodInsn(INVOKESPECIAL, "java/lang/IllegalArgumentException", "<init>", "(Ljava/lang/String;)V")
    mv.visitInsn(ATHROW)
  }

  private def visitRead(cw:ClassWriter,classInfo:ClassInfo,classNameInternal:String):MethodVisitor ={
    val mv = cw.visitMethod(ACC_PUBLIC + ACC_FINAL, "read", "(Ljava/lang/Object;I)Ljava/lang/Object;",null, null)
    mv.visitCode()
    val methods = classInfo.readers.values.toList.sorted
    if (methods.size > 0) {
      mv.visitVarInsn(ALOAD, 1)
      mv.visitTypeInsn(CHECKCAST, classNameInternal)
      mv.visitVarInsn(ASTORE, 4)
      mv.visitVarInsn(ILOAD, 2)
      val labels = new Array[Label](methods.size)
      val indexes= new Array[Int](methods.size)
      var j=0
      while(j<labels.length){
        labels(j)=new Label
        indexes(j)=methods(j).index
        j+=1
      }
      val defaultLabel = new Label()
      mv.visitLookupSwitchInsn(defaultLabel,indexes,labels)
      val buffer = new StringBuilder(128)
      var i = 0
      while (i < labels.length) {
        mv.visitLabel(labels(i))
        if (i == 0) mv.visitFrame(Opcodes.F_APPEND, 1, Array(classNameInternal), 0, null) else mv.visitFrame(Opcodes.F_SAME,0, null, 0, null)
        mv.visitVarInsn(ALOAD, 4)
        val info = methods(i)
        buffer.setLength(0)
        buffer.append("()")
        buffer.append(Type.getDescriptor(info.method.getReturnType))
        mv.visitMethodInsn(INVOKEVIRTUAL, classNameInternal, info.method.getName, buffer.toString)
        box(info.method.getReturnType,mv)
        mv.visitInsn(ARETURN)
        i+=1
      }
      mv.visitLabel(defaultLabel)
      mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null)
    }
    visitException(mv)
    mv.visitMaxs(0, 0)
    mv.visitEnd()
    mv
  }


  private def visitWrite(cw:ClassWriter,classInfo:ClassInfo,classNameInternal:String):MethodVisitor = {
    val mv = cw.visitMethod(ACC_PUBLIC + ACC_FINAL, "write", "(Ljava/lang/Object;ILjava/lang/Object;)Ljava/lang/Object;",
      null, null)
    mv.visitCode()
    val methods = classInfo.writers.values.toList.sorted
    if (methods.size > 0) {
      mv.visitVarInsn(ALOAD, 1)
      mv.visitTypeInsn(CHECKCAST, classNameInternal)
      mv.visitVarInsn(ASTORE, 4)
      mv.visitVarInsn(ILOAD, 2)
      val labels = new Array[Label](methods.size)
      val indexes= new Array[Int](methods.size)
      var j=0
      while(j<labels.length){
        labels(j)=new Label
        indexes(j)=methods(j).index
        j+=1
      }
      val defaultLabel = new Label
      mv.visitLookupSwitchInsn(defaultLabel,indexes,labels)
      val buffer = new StringBuilder(128)
      var i = 0
      while (i < labels.length) {
        mv.visitLabel(labels(i))
        if (i == 0) mv.visitFrame(Opcodes.F_APPEND, 1, Array(classNameInternal), 0, null) else mv.visitFrame(Opcodes.F_SAME,
          0, null, 0, null)
        mv.visitVarInsn(ALOAD, 4)
        buffer.setLength(0)
        buffer.append('(')
        val info = methods(i)
        mv.visitVarInsn(ALOAD, 3)
        val paramType = unbox(info.method.getParameterTypes()(0),mv)
        buffer.append(paramType.getDescriptor)
        buffer.append(')')
        buffer.append(Type.getDescriptor(info.method.getReturnType))
        mv.visitMethodInsn(INVOKEVIRTUAL, classNameInternal, info.method.getName, buffer.toString)
        box(info.method.getReturnType,mv)
        mv.visitInsn(ARETURN)
        i+=1
      }
      mv.visitLabel(defaultLabel)
      mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null)
    }
    visitException(mv)
    mv.visitMaxs(0, 0)
    mv.visitEnd()
    mv
  }

  private def visitInvoke(cw:ClassWriter,classInfo:ClassInfo,classNameInternal:String):MethodVisitor ={
    val mv = cw.visitMethod(ACC_PUBLIC + ACC_FINAL, "invoke", "(Ljava/lang/Object;ILscala/collection/Seq;)Ljava/lang/Object;",
      null, null)
    mv.visitCode()
    val methods = classInfo.getMethods
    if (methods.size > 0) {
      mv.visitVarInsn(ALOAD, 1)
      mv.visitTypeInsn(CHECKCAST, classNameInternal)
      mv.visitVarInsn(ASTORE, 4)
      mv.visitVarInsn(ILOAD, 2)
      val labels = new Array[Label](methods.size)
        (0 until labels.length).foreach(labels(_) = new Label)
      val defaultLabel = new Label()
      mv.visitTableSwitchInsn(0, labels.length - 1, defaultLabel, labels)
      val buffer = new StringBuilder(128)
      var i = 0
      while (i < labels.length) {
        mv.visitLabel(labels(i))
        if (i == 0) mv.visitFrame(Opcodes.F_APPEND, 1, Array(classNameInternal), 0, null) else mv.visitFrame(Opcodes.F_SAME,
          0, null, 0, null)
        mv.visitVarInsn(ALOAD, 4)
        buffer.setLength(0)
        buffer.append('(')
        val info = methods(i)
        val paramTypes = info.method.getParameterTypes
        for (paramIndex <- 0 until paramTypes.length) {
          mv.visitVarInsn(ALOAD, 3)
          mv.visitIntInsn(BIPUSH,paramIndex)
          mv.visitMethodInsn(INVOKEINTERFACE, "scala/collection/Seq", "apply", "(I)Ljava/lang/Object;")
          val paramType = unbox(paramTypes(paramIndex),mv)
          buffer.append(paramType.getDescriptor)
        }
        buffer.append(')')
        buffer.append(Type.getDescriptor(info.method.getReturnType))

        mv.visitMethodInsn(INVOKEVIRTUAL, classNameInternal, info.method.getName, buffer.toString)
        box(info.method.getReturnType,mv)
        mv.visitInsn(ARETURN)
        i+=1
      }
      mv.visitLabel(defaultLabel)
      mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null)
    }
    visitException(mv)
    mv.visitMaxs(0, 0)
    mv.visitEnd()
    mv
  }
}

/**
 * Class invocation proxy,delegate method invocation and property accessment.
 * It employ asm framework,dynamiclly generate a access class.
 * <p>
 * Usage:
 *
 * <pre>
 * Mirror mirror = Mirror.get(YourBean.class);
 * // invoke any method
 * mirror.invoke(bean, &quot;somemethod&quot;, arg1, arg2);
 * </pre>
 *
 * @author chaostone
 * @since 3.2.0
 */
abstract class Mirror {

  var classInfo: ClassInfo = _

  def read(obj: AnyRef, methodIndex: Int): Any

  def write(obj: AnyRef, methodIndex: Int, args: Any): Any

  /**
   * Delegate invocation to object's method with arguments.
   *
   * @see #getIndex(String, Object...)
   */
  def invoke(obj: AnyRef, methodIndex: Int, args: Any*): Any
  /**
   * Return method index.
   * index is 0 based,if not found ,return -1.
   *
   * @see #invoke(Object, int, Object...)
   */
  def getIndex(name: String, args: Any*): Int = classInfo.getIndex(name, args)

  /**
   * invoke the method with the specified name and arguments.
   * <p>
   * It lookup method index by name and arguments,find first method matchs given sigature. The best
   * approach in many time invocations is get the index first and pass through to invoke.
   * <p>
   * In 100 000 000's benchmark test, this method is 35% slower than
   * invoke(obj,getIndex(method),args) form.the former consume 2800ms and the later using just
   * 1800ms. STRANGE!!!
   *
   * @see #invoke(Object, int, Object...)
   */
  def invoke(obj: AnyRef, method: String, args: Any*): Any =  invoke(obj, classInfo.getIndex(method, args), args)
}
