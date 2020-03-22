package com.almazsh.lambda.core.transform.usagesfinder

import org.objectweb.asm.ClassReader
import org.objectweb.asm.Opcodes.ASM7
import org.objectweb.asm.Opcodes.GETSTATIC
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.TypeInsnNode
import java.io.File

object LambdasUsagesFinder {

    fun findTransforming(fromDir: String, usagesFinderInfos: List<UsagesFinderInfo>): List<File> {
        val classNames = File(fromDir).walk()
            .filter { it.name.endsWith(".class") }
            .toList()
            .flatMap { classFile ->
                val bytes = classFile.readBytes()
                getTransformingLambdasFrom(bytes, usagesFinderInfos)
            }

        val files = classNames
            .map { className ->
                File(fromDir, "$className.class")
            }

        return files
    }

    fun getTransformingLambdasFrom(bytes: ByteArray, usagesFinderInfos: List<UsagesFinderInfo>): List<String> {
        val cn = ClassNode(ASM7)

        val cr = ClassReader(bytes)
        cr.accept(cn, ClassReader.SKIP_DEBUG)

        return cn.methods.flatMap { method ->
            method.instructions.mapNotNull { ins ->
                if (ins is MethodInsnNode &&
                    usagesFinderInfos.any { ins.owner == it.owner && ins.name == it.name && ins.desc == it.descriptor }
                ) {
                    val prev = ins.previous
                    assert(prev is TypeInsnNode && prev.desc == "kotlin/Function")
                    val prevprev = prev.previous
                    if (prevprev is FieldInsnNode &&
                        prevprev.opcode == GETSTATIC &&
                        prevprev.name == "INSTANCE" &&
                        prevprev.desc == "L${prevprev.owner};"
                    ) {
                        // singletone lambda
                        return@mapNotNull prevprev.owner
                    } else if (prevprev is MethodInsnNode && prevprev.name == "<init>") {
                        // lambda with closure
                        return@mapNotNull prevprev.owner
                    } else {
                        // illegal
                        throw Exception()
                    }
                }
                null
            }
        }
    }
}
