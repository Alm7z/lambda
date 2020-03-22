package com.almazsh.lambda.core.transform.expressions

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Type
import org.objectweb.asm.tree.AbstractInsnNode

data class ValueExpression(
    val type: Type,
    val code: List<AbstractInsnNode>
) : Expression {

    override fun renderAsNode(methodVisitor: MethodVisitor) {
        renderDirectlyAndWrapAsNode(methodVisitor)
    }

    override fun renderDirectly(methodVisitor: MethodVisitor) {
        for (insnNode in code) {
            insnNode.accept(methodVisitor)
        }
    }

    override fun renderDirectlyAndWrapAsNode(methodVisitor: MethodVisitor) {
        methodVisitor.visitWrappingNodeInsns(type) {
            renderDirectly(methodVisitor)
        }
    }

    override val children = emptyList<Expression>()
    override var mustRenderDirectly = false
}
