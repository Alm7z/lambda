package com.almazsh.lambda.core.transform

import com.almazsh.lambda.core.transform.expressions.Expression
import com.almazsh.lambda.core.transform.usagesfinder.UsagesFinderInfo
import com.android.build.api.transform.*

class LambdasTransform(
    val id: String,
    val usagesFinderInfos: List<UsagesFinderInfo>,
    val treeModifier: (Expression) -> Expression = { it }
) : Transform() {

    override fun getName() = "lambdas_to_trees-$id"

    override fun isIncremental() = false

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> {
        return mutableSetOf(QualifiedContent.DefaultContentType.CLASSES)
    }

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
        return mutableSetOf(QualifiedContent.Scope.PROJECT)
    }

    override fun transform(
        context: Context?,
        inputs: MutableCollection<TransformInput>?,
        referencedInputs: MutableCollection<TransformInput>?,
        outputProvider: TransformOutputProvider?,
        isIncremental: Boolean
    ) {
        println("start transform")
        println("inputs: $inputs")
        println("referencedInputs: $referencedInputs")

        val outDir = outputProvider!!.getContentLocation("classes", outputTypes, scopes, Format.DIRECTORY)

        outDir.deleteRecursively()
        outDir.mkdirs()

        inputs?.flatMap { it.directoryInputs }?.forEach { directoryInput ->
            println("directoryInput: $directoryInput")
            DirectoryTransformer.transform(
                directoryInput.file,
                outDir,
                usagesFinderInfos,
                treeModifier
            )
        }

        // handle R class jar for gradle android 3.6.0 +
        inputs?.flatMap { it.jarInputs }?.forEach { jarInput ->
            println("jarInput: $jarInput")
            val src = jarInput.file
            val dest = outputProvider.getContentLocation(src.absolutePath, outputTypes, scopes, Format.JAR)
            src.copyTo(dest, true)
            println("copy jar from $src to $dest")
        }

        println("end transform ")
    }
}
