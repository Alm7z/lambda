package com.almazsh.lambda.core.transform.expressions

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

object ZeroExpression : Expression {

    override fun renderAsNode(methodVisitor: MethodVisitor) {
        renderDirectlyAndWrapAsNode(methodVisitor)
    }

    override fun renderDirectly(methodVisitor: MethodVisitor) {
        methodVisitor.visitInsn(Opcodes.ICONST_0)
    }

    override fun renderDirectlyAndWrapAsNode(methodVisitor: MethodVisitor) {
        methodVisitor.visitWrappingNodeInsns(Type.INT_TYPE) {
            renderDirectly(methodVisitor)
        }
    }

    override val children = emptyList<Expression>()
    override var mustRenderDirectly = false
}
