package com.almazsh.lambda.testsql.plugin.expressions

import com.almazsh.lambda.core.transform.expressions.Expression
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.*

data class SqlFunctionExpression(
    val name: String,
    var args: List<Expression>
) : Expression {
    override fun renderAsNode(methodVisitor: MethodVisitor) {
        methodVisitor.visitTypeInsn(NEW, "com/almazsh/lambda/testsql/api/nodes/SqlFunctionNode")
        methodVisitor.visitInsn(DUP)
        methodVisitor.visitLdcInsn(name)

        methodVisitor.visitIntInsn(BIPUSH, args.size)
        methodVisitor.visitTypeInsn(ANEWARRAY, "com/almazsh/lambda/core/api/nodes/Node")
        for ((index, arg) in args.withIndex()) {
            methodVisitor.visitInsn(DUP)
            methodVisitor.visitIntInsn(BIPUSH, index)
            if (arg.mustRenderDirectly) {
                arg.renderDirectlyAndWrapAsNode(methodVisitor)
            } else {
                arg.renderAsNode(methodVisitor)
            }
            methodVisitor.visitInsn(AASTORE)
        }
        methodVisitor.visitMethodInsn(INVOKESTATIC, "kotlin/collections/ArraysKt", "asList", "([Ljava/lang/Object;)Ljava/util/List;", false)

        methodVisitor.visitMethodInsn(INVOKESPECIAL, "com/almazsh/lambda/testsql/api/nodes/SqlFunctionNode", "<init>", "(Ljava/lang/String;Ljava/util/List;)V", false)
        methodVisitor.visitTypeInsn(CHECKCAST, "com/almazsh/lambda/core/api/nodes/Node")
    }

    override fun renderDirectly(methodVisitor: MethodVisitor) {
        throw Exception("can't render sql function directly")
    }

    override fun renderDirectlyAndWrapAsNode(methodVisitor: MethodVisitor) {
        throw Exception("can't render sql function directly")
    }

    override val children = args
    override var mustRenderDirectly: Boolean
        get() = false
        set(value) {
            if (value) {
                throw Exception("can't render sql function directly")
            }
        }
}
