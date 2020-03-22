package com.almazsh.lambda.testsql.api

import com.almazsh.lambda.core.api.TransformedLambda
import com.almazsh.lambda.core.api.nodes.Node

class SqlExpr<L : Function<*>>(
    val lambda: L
) {
    fun getTree(): Node {
        if (lambda is TransformedLambda) {
            return lambda.getTree()
        } else {
            throw Exception("wrong lambda or not transformed!")
        }
    }
}
