package com.almazsh.lambda.testsql.api.nodes

import com.almazsh.lambda.core.api.nodes.Node

data class SqlParamPropertyNode(
    val name: String,
    val param: Node
) : Node()
