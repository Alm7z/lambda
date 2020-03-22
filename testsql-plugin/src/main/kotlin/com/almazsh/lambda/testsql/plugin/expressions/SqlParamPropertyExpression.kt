package com.almazsh.lambda.testsql.plugin.expressions

import com.almazsh.lambda.core.transform.expressions.Expression
import com.almazsh.lambda.core.transform.expressions.ParamExpression
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.*

data class SqlParamPropertyExpression(
    val name: String,
    val param: ParamExpression
) : Expression {
    override fun renderAsNode(methodVisitor: MethodVisitor) {
        methodVisitor.visitTypeInsn(NEW, "com/almazsh/lambda/testsql/api/nodes/SqlParamPropertyNode")
        methodVisitor.visitInsn(DUP)
        methodVisitor.visitLdcInsn(name)

        param.renderAsNode(methodVisitor)

        methodVisitor.visitMethodInsn(INVOKESPECIAL, "com/almazsh/lambda/testsql/api/nodes/SqlParamPropertyNode", "<init>", "(Ljava/lang/String;Lcom/almazsh/lambda/core/api/nodes/Node;)V", false)
        methodVisitor.visitTypeInsn(CHECKCAST, "com/almazsh/lambda/core/api/nodes/Node")
    }

    override fun renderDirectly(methodVisitor: MethodVisitor) {
        throw Exception("can't render sql param property directly")
    }

    override fun renderDirectlyAndWrapAsNode(methodVisitor: MethodVisitor) {
        throw Exception("can't render sql param property directly")
    }

    override val children = listOf(param)
    override var mustRenderDirectly: Boolean
        get() = false
        set(value) {
            if (value) {
                throw Exception("can't render sql param property directly")
            }
        }
}
