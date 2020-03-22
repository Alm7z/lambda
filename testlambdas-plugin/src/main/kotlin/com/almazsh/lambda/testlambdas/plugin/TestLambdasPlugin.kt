package com.almazsh.lambda.testlambdas.plugin

import com.almazsh.lambda.core.transform.LambdasTransform
import com.almazsh.lambda.core.transform.usagesfinder.UsagesFinderInfo
import com.android.build.gradle.BaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class TestLambdasPlugin : Plugin<Project> {

    companion object {
        val usageFinderInfos = listOf(
            UsagesFinderInfo(
                "com/almazsh/lambda/testlambdas/api/TestExpr",
                "<init>",
                "(Lkotlin/Function;)V"
            )
        )
    }

    override fun apply(target: Project) {
        val android = target.extensions.findByName("android") as BaseExtension
        val transform = LambdasTransform("testlambdas", usageFinderInfos)
        android.registerTransform(transform)
    }
}
