package com.almazsh.lambda.core.api.nodes

data class ParamNode(
    val index: Int // first param = 0 (without "this" local variable)
) : Node()
