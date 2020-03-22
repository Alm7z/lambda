package com.almazsh.lambda.testsql.plugin

import com.almazsh.lambda.core.transform.LambdasTransform
import com.almazsh.lambda.core.transform.expressions.Expression
import com.almazsh.lambda.core.transform.usagesfinder.UsagesFinderInfo
import com.android.build.gradle.BaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class TestSqlPlugin : Plugin<Project> {

    companion object {
        val usageFinderInfos = listOf(
            UsagesFinderInfo(
                "com/almazsh/lambda/testsql/api/SqlExpr",
                "<init>",
                "(Lkotlin/Function;)V"
            ),
            UsagesFinderInfo(
                "com/almazsh/lambda/testsql/api/Queryable",
                "where",
                "(Lkotlin/jvm/functions/Function1;)Lcom/almazsh/lambda/testsql/api/Queryable;"
            )
        )

        val treeModifier: (Expression) -> Expression = SqlTreeModifier
    }

    override fun apply(target: Project) {
        val android = target.extensions.findByName("android") as BaseExtension
        val transform = LambdasTransform("testsql", usageFinderInfos, treeModifier)
        android.registerTransform(transform)
    }
}
