package com.almazsh.lambda.core.transform.expressions

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.MethodInsnNode

data class MethodCallExpression(
    val op: MethodInsnNode,
    var receiver: Expression?,
    var args: List<Expression>
) : Expression {

    override fun renderAsNode(methodVisitor: MethodVisitor) {
        methodVisitor.visitTypeInsn(Opcodes.NEW, "com/almazsh/lambda/core/api/nodes/MethodCallNode")
        methodVisitor.visitInsn(Opcodes.DUP)

        receiver?.let {
            if (it.mustRenderDirectly) {
                it.renderDirectlyAndWrapAsNode(methodVisitor)
            } else {
                it.renderAsNode(methodVisitor)
            }
        } ?: run {
            methodVisitor.visitInsn(Opcodes.ACONST_NULL)
        }

        methodVisitor.visitIntInsn(Opcodes.BIPUSH, args.size)
        methodVisitor.visitTypeInsn(Opcodes.ANEWARRAY, "com/almazsh/lambda/core/api/nodes/Node")
        for ((index, arg) in args.withIndex()) {
            methodVisitor.visitInsn(Opcodes.DUP)
            methodVisitor.visitIntInsn(Opcodes.BIPUSH, index)
            if (arg.mustRenderDirectly) {
                arg.renderDirectlyAndWrapAsNode(methodVisitor)
            } else {
                arg.renderAsNode(methodVisitor)
            }
            methodVisitor.visitInsn(Opcodes.AASTORE)
        }
        methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, "kotlin/collections/ArraysKt", "asList", "([Ljava/lang/Object;)Ljava/util/List;", false)

        methodVisitor.visitLdcInsn(op.owner)
        methodVisitor.visitLdcInsn(op.name)
        methodVisitor.visitLdcInsn(op.desc)

        methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, "com/almazsh/lambda/core/api/nodes/MethodCallNode", "<init>", "(Lcom/almazsh/lambda/core/api/nodes/Node;Ljava/util/List;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", false)
        methodVisitor.visitTypeInsn(Opcodes.CHECKCAST, "com/almazsh/lambda/core/api/nodes/Node")
    }

    override fun renderDirectly(methodVisitor: MethodVisitor) {
        receiver?.renderDirectly(methodVisitor)
        args.forEach { it.renderDirectly(methodVisitor) }
        op.accept(methodVisitor)
    }

    override fun renderDirectlyAndWrapAsNode(methodVisitor: MethodVisitor) {
        val type = Type.getMethodType(op.desc).returnType
        methodVisitor.visitWrappingNodeInsns(type) {
            renderDirectly(methodVisitor)
        }
    }

    override val children = listOfNotNull(receiver) + args
    override var mustRenderDirectly = false
}
