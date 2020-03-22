package com.almazsh.lambda.core.transform.expressions

import org.objectweb.asm.MethodVisitor

interface Expression {
    fun renderAsNode(methodVisitor: MethodVisitor)
    fun renderDirectly(methodVisitor: MethodVisitor)
    fun renderDirectlyAndWrapAsNode(methodVisitor: MethodVisitor)
    val children: List<Expression>
    var mustRenderDirectly: Boolean
}
