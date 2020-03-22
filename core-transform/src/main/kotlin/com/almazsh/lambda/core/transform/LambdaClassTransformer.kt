package com.almazsh.lambda.core.transform

import com.almazsh.lambda.core.transform.expressions.*
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassReader.SKIP_DEBUG
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.Type
import org.objectweb.asm.tree.*
import java.util.*

class LambdaClassTransformer {

    private val stack = Stack<Expression>()

    fun convertAddingExpressionTree(bytes: ByteArray, treeModifier: (Expression) -> Expression): ByteArray {
        val cn = ClassNode(ASM7)

        val cr = ClassReader(bytes)
        cr.accept(cn, SKIP_DEBUG)

        val invokeMethod = cn.methods.single { it.name == "invoke" && it.access == (ACC_PUBLIC or ACC_FINAL) }

        var insnNode = invokeMethod.instructions.first
        while (insnNode != null) {
            insnNode = handleNodes(insnNode, cn.name)
        }

        assert(stack.size == 1)

        val tree = stack.pop()
        println(tree.toString())
        val transformedTree = treeModifier(tree)
        println(transformedTree.toString())

        cn.interfaces.add("com/almazsh/lambda/core/api/TransformedLambda")

        val methodVisitor = cn.visitMethod(ACC_PUBLIC, "getTree", "()Lcom/almazsh/lambda/core/api/nodes/Node;", null, null)
        val annotationVisitor = methodVisitor.visitAnnotation("Lorg/jetbrains/annotations/NotNull;", false);
        annotationVisitor.visitEnd()
        methodVisitor.visitCode()

        if (transformedTree.mustRenderDirectly) {
            transformedTree.renderDirectlyAndWrapAsNode(methodVisitor)
        } else {
            transformedTree.renderAsNode(methodVisitor)
        }
        methodVisitor.visitInsn(ARETURN)

        methodVisitor.visitEnd()

        val cw = ClassWriter(ClassWriter.COMPUTE_MAXS or ClassWriter.COMPUTE_FRAMES)
        cn.accept(cw)

        return cw.toByteArray()
    }

    private fun handleNodes(node: AbstractInsnNode, lambdaClassName: String): AbstractInsnNode? {
        when (node) {
            is InsnNode -> when (node.opcode) {
                ICONST_1, ICONST_2, ICONST_3, ICONST_4, ICONST_5 -> {
                    stack.push(ValueExpression(Type.INT_TYPE, listOf(node)))
                    return node.next
                }
                IADD, ISUB, IMUL, IDIV, IREM, IAND, IOR, IXOR -> {
                    val second = stack.pop()
                    val first = stack.pop()
                    stack.push(
                        BinaryOperationExpression(
                            node,
                            first,
                            second
                        )
                    )
                    return node.next
                }
                IRETURN, ARETURN -> {
                    return node.next
                }
                else -> throw IllegalArgumentException("unknown insn ${node.opcode}")
            }
            is IntInsnNode -> {
                when (node.opcode) {
                    BIPUSH -> {
                        stack.push(ValueExpression(Type.INT_TYPE, listOf(node)))
                        return node.next
                    }
                    SIPUSH -> {
                        stack.push(ValueExpression(Type.INT_TYPE, listOf(node)))
                        return node.next
                    }
                    else -> throw IllegalArgumentException("unknown IntInsnNode ${node.opcode}")
                }
            }
            is VarInsnNode -> {
                if (node.`var` == 0) {
                    val nextNode = node.next
                    if (nextNode is FieldInsnNode && nextNode.owner == lambdaClassName) {
                        // getting variable from closure
                        val type = Type.getType(nextNode.desc)
                        stack.push(ValueExpression(type, listOf(node, nextNode)))
                        return node.next.next
                    } else {
                        throw Exception()
                    }
                } else {
                    // lambda's param
                    stack.push(ParamExpression(node.`var` - 1))
                    return node.next
                }
            }
            is JumpInsnNode -> {
                val next1 = node.next
                val next2 = next1.next
                val next3 = next2.next
                val next4 = next3.next
                val next5 = next4.next
                val next6 = next5.next
                val next7 = next6.next

                if (next1.opcode == ICONST_1 &&
                    next2 is JumpInsnNode &&
                    next2.opcode == GOTO &&
                    next3 is LabelNode &&
                    node.label == next3 &&
                    next4 is FrameNode &&
                    next5.opcode == ICONST_0 &&
                    next6 is LabelNode &&
                    next2.label == next6 &&
                    next7 is FrameNode
                ) {
                    when (node.opcode) {
                        IF_ICMPEQ, IF_ICMPNE, IF_ICMPLT, IF_ICMPGE, IF_ICMPGT, IF_ICMPLE -> {
                            val second = stack.pop()
                            val first = stack.pop()
                            stack.push(
                                BinaryOperationExpression(
                                    node,
                                    first,
                                    second
                                )
                            )
                            return next7.next
                        }
                        IFEQ, IFNE, IFLT, IFGE, IFGT, IFLE -> {
                            val mappedOpcode = when (node.opcode) {
                                IFEQ -> IF_ICMPEQ
                                IFNE -> IF_ICMPNE
                                IFLT -> IF_ICMPLT
                                IFGE -> IF_ICMPGE
                                IFGT -> IF_ICMPGT
                                IFLE -> IF_ICMPLE
                                else -> throw IllegalArgumentException("unknown opcode ${node.opcode}")
                            }
                            val mappedNode = JumpInsnNode(mappedOpcode, node.label)
                            val second = ZeroExpression
                            val first = stack.pop()
                            stack.push(
                                BinaryOperationExpression(
                                    mappedNode,
                                    first,
                                    second
                                )
                            )
                            return next7.next
                        }
                        else -> throw IllegalArgumentException("unknown JumpInsnNode ${node.opcode}")
                    }
                } else {
                    throw IllegalArgumentException("unknown JumpInsnNode ${node.opcode}")
                }
            }
            is LdcInsnNode -> {
                stack.push(
                    ValueExpression(
                        Type.getType(node.cst.javaClass),
                        listOf(node)
                    )
                )
                return node.next
            }
            is MethodInsnNode -> {
                if (node.owner == "kotlin/jvm/internal/Intrinsics" && node.name == "checkParameterIsNotNull") {
                    repeat(2) {
                        stack.pop()
                    }
                    return node.next
                } else {
                    val type = Type.getMethodType(node.desc)

                    val argsCount = type.argumentTypes.size
                    val argExp = stack.takeLast(argsCount)
                    repeat(argsCount) { stack.pop() }

                    val receiver = when (node.opcode) {
                        INVOKESTATIC -> {
                            null
                        }
                        INVOKEVIRTUAL -> {
                            stack.pop()
                        }
                        else -> throw IllegalArgumentException("unknown MethodInsnNode ${node.opcode}")
                    }

                    stack.push(
                        MethodCallExpression(
                            node,
                            receiver,
                            argExp
                        )
                    )

                    return node.next
                }
            }
            else -> throw IllegalArgumentException("unknown insn ${node.opcode}")
        }
    }
}
