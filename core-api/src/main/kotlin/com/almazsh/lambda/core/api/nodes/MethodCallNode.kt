package com.almazsh.lambda.core.api.nodes

data class MethodCallNode(
    val receiver: Node?,
    val params: List<Node>,
    val methodOwner: String,
    val methodName: String,
    val methodDescriptor: String
) : Node()
