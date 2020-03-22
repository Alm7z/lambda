package com.almazsh.lambda.dev.runnable

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassReader.SKIP_DEBUG
import org.objectweb.asm.util.ASMifier
import org.objectweb.asm.util.TraceClassVisitor
import java.io.File
import java.io.PrintWriter

class Dumper {

    val dumpsDir = "./dev-runnable/src/main/java/asm/com/almazsh/lambda/dev/classfiles"

    init {
        File(dumpsDir).mkdirs()
    }

    fun dumpFile(file: File) {
        val bytes = file.readBytes()
        val className = file.nameWithoutExtension
        dumpClass(bytes, className)
    }

    private fun dumpClass(bytes: ByteArray, className: String) {
        val cr = ClassReader(bytes)

        val dumpFilePath = "$dumpsDir/${className}Dump.java"
        val dumpFile = File(dumpFilePath)

        cr.accept(TraceClassVisitor(null, ASMifier(), PrintWriter(dumpFile)), SKIP_DEBUG)
    }
}
