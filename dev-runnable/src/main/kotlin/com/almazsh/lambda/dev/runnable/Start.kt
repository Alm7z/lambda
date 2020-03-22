package com.almazsh.lambda.dev.runnable

import com.almazsh.lambda.core.transform.DirectoryTransformer
import com.almazsh.lambda.testlambdas.plugin.TestLambdasPlugin
import com.almazsh.lambda.testsql.plugin.TestSqlPlugin
import java.io.File

const val SOURCE_CLASSES_DIR = "./dev-classfiles/build/classes/kotlin/main"
const val TRANSFORMED_CLASSES_DIR_1 = "./dev-classfiles/build/transformed-classes-1/kotlin/main"
const val TRANSFORMED_CLASSES_DIR_2 = "./dev-classfiles/build/transformed-classes-2/kotlin/main"

fun main() {
    println("start")

    prepare()

    val dumper = Dumper()

    DirectoryTransformer.transform(
        File(SOURCE_CLASSES_DIR),
        File(TRANSFORMED_CLASSES_DIR_1),
        TestSqlPlugin.usageFinderInfos,
        TestSqlPlugin.treeModifier,
        dumper::dumpFile,
        dumper::dumpFile
    )

    DirectoryTransformer.transform(
        File(TRANSFORMED_CLASSES_DIR_1),
        File(TRANSFORMED_CLASSES_DIR_2),
        TestLambdasPlugin.usageFinderInfos
    )

    println("end")
}

fun prepare() {
    if (!File(SOURCE_CLASSES_DIR).exists()) {
        throw Exception("Sources not found")
    }
    File(TRANSFORMED_CLASSES_DIR_1).mkdirs()
    File(TRANSFORMED_CLASSES_DIR_2).mkdirs()
}
