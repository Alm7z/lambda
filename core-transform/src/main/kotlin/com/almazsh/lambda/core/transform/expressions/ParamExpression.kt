package com.almazsh.lambda.core.transform.expressions

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

data class ParamExpression(
    val paramIndex: Int // first param = 0 (without "this" local variable)
) : Expression {

    override fun renderAsNode(methodVisitor: MethodVisitor) {
        methodVisitor.visitTypeInsn(Opcodes.NEW, "com/almazsh/lambda/core/api/nodes/ParamNode")
        methodVisitor.visitInsn(Opcodes.DUP)

        methodVisitor.visitIntInsn(Opcodes.BIPUSH, paramIndex)

        methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, "com/almazsh/lambda/core/api/nodes/ParamNode", "<init>", "(I)V", false)
        methodVisitor.visitTypeInsn(Opcodes.CHECKCAST, "com/almazsh/lambda/core/api/nodes/Node")
    }

    override fun renderDirectly(methodVisitor: MethodVisitor) {
        throw Exception("can't render param directly")
    }

    override fun renderDirectlyAndWrapAsNode(methodVisitor: MethodVisitor) {
        throw Exception("can't render param directly")
    }

    override val children = emptyList<Expression>()
    override var mustRenderDirectly: Boolean
        get() = false
        set(value) {
            if (value) {
                throw Exception("can't render param directly")
            }
        }
}
