package com.almazsh.lambda.testsql.api

import com.almazsh.lambda.core.api.nodes.BinaryNode
import com.almazsh.lambda.core.api.nodes.BinaryNodeType.*
import com.almazsh.lambda.core.api.nodes.MethodCallNode
import com.almazsh.lambda.core.api.nodes.Node
import com.almazsh.lambda.core.api.nodes.ValueNode
import com.almazsh.lambda.core.api.requireExprTree
import com.almazsh.lambda.testsql.api.nodes.SqlFunctionNode
import com.almazsh.lambda.testsql.api.nodes.SqlParamPropertyNode

class Queryable<T> {

    private val tableName: String
    private val clazz: Class<T>

    private val predicates: List<Node>

    public constructor(tableName: String, clazz: Class<T>) {
        this.tableName = tableName
        this.clazz = clazz
        this.predicates = emptyList()
    }

    private constructor(tableName: String, clazz: Class<T>, predicates: List<Node>) {
        this.tableName = tableName
        this.clazz = clazz
        this.predicates = predicates
    }

    fun where(predicate: (T) -> Boolean): Queryable<T> {
        return Queryable(tableName, clazz, predicates + predicate.requireExprTree())
    }

    fun where(predicate: SqlExpr<(T) -> Boolean>): Queryable<T> {
        return Queryable(tableName, clazz, predicates + predicate.getTree())
    }

    fun asSqlString(): String {
        return buildString {
            append("SELECT * FROM $tableName")

            if (predicates.isNotEmpty()) {
                append(" WHERE ")
                renderNode(predicates[0])
                for (predicate in predicates.drop(1)) {
                    append(" AND ")
                    renderNode(predicate)
                }
            }
        }
    }

    private fun StringBuilder.renderNode(node: Node) {
        when (node) {
            is ValueNode -> {
                append(node.value.toString())
            }
            is BinaryNode -> {
                append('(')
                renderNode(node.first)
                append(
                    when (node.operation) {
                        SUM -> "+"
                        SUBTRACTION -> "-"
                        MULTIPLY -> "*"
                        DIVIDE -> "/"
                        REMAINDER -> "%"
                        AND -> "AND"
                        OR -> "OR"
                        XOR -> throw Exception("XOR not yet implemented")
                        EQ -> "=="
                        NOT_EQ -> "!="
                        GREATER -> ">"
                        LESS -> "<"
                        GREATER_EQ -> ">="
                        LESS_EQ -> "<="
                    }
                )
                renderNode(node.second)
                append(')')
            }
            is SqlParamPropertyNode -> {
                append(node.name)
            }
            is SqlFunctionNode -> {
                append(node.name)
                append('(')
                renderNode(node.params[0])
                for (param in node.params.drop(1)) {
                    append(", ")
                    renderNode(param)
                }
                append(')')
            }
            is MethodCallNode -> {
                throw Exception("MethodCallNode that not transformed found: $node")
            }
            else -> throw Exception("unknown Node found")
        }
    }
}
