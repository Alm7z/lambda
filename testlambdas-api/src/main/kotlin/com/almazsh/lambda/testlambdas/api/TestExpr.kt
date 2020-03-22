package com.almazsh.lambda.testlambdas.api

import com.almazsh.lambda.core.api.TransformedLambda
import com.almazsh.lambda.core.api.nodes.Node

class TestExpr<L : Function<*>>(
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

fun Node.toFormattedString(): String {
    val str = toString()
    return buildString {
        append("TestExpr: ")
        var indent = 0
        for (c in str) {
            if (c == ')') {
                indent -= 4
                if (indent < 0) indent = 0
                append('\n')
                repeat(indent) { append(' ') }
            }
            append(c)
            when (c) {
                '(' -> {
                    indent += 4
                    append('\n')
                    repeat(indent) { append(' ') }
                }
                ',' -> {
                    append('\n')
                    repeat(indent) { append(' ') }
                }
            }
        }
    }
}
