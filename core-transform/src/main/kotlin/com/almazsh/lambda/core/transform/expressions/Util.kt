package com.almazsh.lambda.core.transform.expressions

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.Type

inline fun MethodVisitor.visitWrappingNodeInsns(type: Type, visitInside: (MethodVisitor) -> Unit) {
    visitWrappingNodeInsnsStart()
    visitInside(this)
    visitWrappingNodeInsnsEnd(type)
}

fun MethodVisitor.visitWrappingNodeInsnsStart() {
    visitTypeInsn(NEW, "com/almazsh/lambda/core/api/nodes/ValueNode")
    visitInsn(DUP)
}

fun MethodVisitor.visitWrappingNodeInsnsEnd(type: Type) {
    when (type.sort) {
        Type.OBJECT -> {/*nothing*/
        }
        Type.INT -> visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false)
        else -> throw IllegalArgumentException("unknown Type ${type}")
    }
    visitMethodInsn(INVOKESPECIAL, "com/almazsh/lambda/core/api/nodes/ValueNode", "<init>", "(Ljava/lang/Object;)V", false)
    visitTypeInsn(CHECKCAST, "com/almazsh/lambda/core/api/nodes/Node")
}
