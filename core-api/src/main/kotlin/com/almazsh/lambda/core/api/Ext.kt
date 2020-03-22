package com.almazsh.lambda.core.api

import com.almazsh.lambda.core.api.nodes.Node

fun <L : Function<*>> L.tryGetExpressionTree(): Node? {
    return if (this is TransformedLambda) {
        this.getTree()
    } else {
        null
    }
}

fun <L : Function<*>> L.requireExprTree(): Node {
    return this.tryGetExpressionTree() ?: throw Exception("not transformed lambda")
}
