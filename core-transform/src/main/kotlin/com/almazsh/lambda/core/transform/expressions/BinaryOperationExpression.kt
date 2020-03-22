package com.almazsh.lambda.core.transform.expressions

import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.Type
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.JumpInsnNode

data class BinaryOperationExpression(
    val op: AbstractInsnNode,
    var first: Expression,
    var second: Expression
) : Expression {

    override fun renderAsNode(methodVisitor: MethodVisitor) {
        methodVisitor.visitTypeInsn(NEW, "com/almazsh/lambda/core/api/nodes/BinaryNode")
        methodVisitor.visitInsn(DUP)

        for (exp in listOf(first, second)) {
            if (exp.mustRenderDirectly) {
                exp.renderDirectlyAndWrapAsNode(methodVisitor)
            } else {
                exp.renderAsNode(methodVisitor)
            }
        }

        val opEnum = when (op) {
            is InsnNode -> {
                when (op.opcode) {
                    IADD -> "SUM"
                    ISUB -> "SUBTRACTION"
                    IMUL -> "MULTIPLY"
                    IDIV -> "DIVIDE"
                    IREM -> "REMAINDER"
                    IAND -> "AND"
                    IOR -> "OR"
                    IXOR -> "XOR"
                    else -> throw IllegalArgumentException("unknown InsnNode ${op.opcode}")
                }
            }
            is JumpInsnNode -> {
                when (op.opcode) {
                    IF_ICMPEQ -> "NOT_EQ"
                    IF_ICMPNE -> "EQ"
                    IF_ICMPLT -> "GREATER_EQ"
                    IF_ICMPGE -> "LESS"
                    IF_ICMPGT -> "LESS_EQ"
                    IF_ICMPLE -> "GREATER"
                    else -> throw IllegalArgumentException("unknown JumpInsnNode ${op.opcode}")
                }
            }
            else -> throw IllegalArgumentException("unknown opcode ${op.opcode}")
        }

        methodVisitor.visitFieldInsn(GETSTATIC, "com/almazsh/lambda/core/api/nodes/BinaryNodeType", opEnum, "Lcom/almazsh/lambda/core/api/nodes/BinaryNodeType;")
        methodVisitor.visitMethodInsn(INVOKESPECIAL, "com/almazsh/lambda/core/api/nodes/BinaryNode", "<init>", "(Lcom/almazsh/lambda/core/api/nodes/Node;Lcom/almazsh/lambda/core/api/nodes/Node;Lcom/almazsh/lambda/core/api/nodes/BinaryNodeType;)V", false)
        methodVisitor.visitTypeInsn(CHECKCAST, "com/almazsh/lambda/core/api/nodes/Node")
    }

    override fun renderDirectly(methodVisitor: MethodVisitor) {
        first.renderDirectly(methodVisitor)
        second.renderDirectly(methodVisitor)
        when (op) {
            is InsnNode -> {
                op.accept(methodVisitor)
            }
            is JumpInsnNode -> {
                val label0 = Label()
                methodVisitor.visitJumpInsn(op.opcode, label0)
                methodVisitor.visitInsn(ICONST_1)
                val label1 = Label()
                methodVisitor.visitJumpInsn(GOTO, label1)
                methodVisitor.visitLabel(label0)
                // auto frame
                methodVisitor.visitInsn(ICONST_0)
                methodVisitor.visitLabel(label1)
                // auto frame
            }
        }
    }

    override fun renderDirectlyAndWrapAsNode(methodVisitor: MethodVisitor) {
        val type = when (op) {
            is InsnNode -> {
                when (op.opcode) {
                    IADD, ISUB, IMUL, IDIV, IREM, IAND, IOR, IXOR -> Type.INT_TYPE
                    else -> throw IllegalArgumentException("unknown InsnNode ${op.opcode}")
                }
            }
            is JumpInsnNode -> {
                when (op.opcode) {
                    IF_ICMPEQ, IF_ICMPNE, IF_ICMPLT, IF_ICMPGE, IF_ICMPGT, IF_ICMPLE -> Type.INT_TYPE
                    else -> throw IllegalArgumentException("unknown JumpInsnNode ${op.opcode}")
                }
            }
            else -> throw IllegalArgumentException("unknown opcode ${op.opcode}")
        }
        methodVisitor.visitWrappingNodeInsns(type) {
            renderDirectly(methodVisitor)
        }
    }

    override val children = listOf(first, second)
    override var mustRenderDirectly = false
}
