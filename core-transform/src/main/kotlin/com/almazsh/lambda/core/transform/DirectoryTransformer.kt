package com.almazsh.lambda.core.transform

import com.almazsh.lambda.core.transform.expressions.Expression
import com.almazsh.lambda.core.transform.usagesfinder.LambdasUsagesFinder
import com.almazsh.lambda.core.transform.usagesfinder.UsagesFinderInfo
import java.io.File

object DirectoryTransformer {

    fun transform(
        inputDir: File,
        outDir: File,
        usagesFinderInfos: List<UsagesFinderInfo>,
        treeModifier: (Expression) -> Expression = { it },
        onEachTransforming: ((File) -> Unit)? = null,
        onEachNonTransforming: ((File) -> Unit)? = null
    ) {
        outDir.mkdirs()
        println("transform:\n    $inputDir\n    $outDir")
        val transformingFiles = LambdasUsagesFinder.findTransforming(inputDir.path, usagesFinderInfos)
        println("transformingFiles: $transformingFiles")
        transformOrCopyClasses(
            inputDir.path,
            outDir.path,
            transformingFiles,
            treeModifier,
            onEachTransforming,
            onEachNonTransforming
        )
    }

    fun transformOrCopyClasses(
        fromDir: String,
        toDir: String,
        transformingFiles: List<File>,
        treeModifier: (Expression) -> Expression,
        onEachTransforming: ((File) -> Unit)?,
        onEachNonTransforming: ((File) -> Unit)?
    ) {
        File(fromDir).walk()
            .filter { it.name.endsWith(".class") }
            .forEach { classFile ->
                val relativePath = classFile.toRelativeString(File(fromDir))
                val targetFile = File("$toDir/$relativePath")
                if (classFile in transformingFiles) {
                    onEachTransforming?.invoke(classFile)
                    transformClass(classFile, targetFile, treeModifier)
                    println("transformClass: $classFile")
                } else {
                    onEachNonTransforming?.invoke(classFile)
                    classFile.copyTo(targetFile, true)
                    println("copy: $classFile")
                }
            }
    }

    fun transformClass(source: File, target: File, treeModifier: (Expression) -> Expression) {
        val transformer = LambdaClassTransformer()

        val sourceBytes = source.readBytes()
        val targetBytes = transformer.convertAddingExpressionTree(sourceBytes, treeModifier)

        target.parentFile.mkdirs()

        target.writeBytes(targetBytes)
    }
}
