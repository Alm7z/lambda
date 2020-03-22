package com.almazsh.lambda.testsql.api.nodes

import com.almazsh.lambda.core.api.nodes.Node

data class SqlFunctionNode(
    val name: String,
    val params: List<Node>
) : Node()
