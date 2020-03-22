package com.almazsh.lambda.testsql.plugin

import com.almazsh.lambda.core.transform.expressions.*
import com.almazsh.lambda.testsql.plugin.expressions.SqlFunctionExpression
import com.almazsh.lambda.testsql.plugin.expressions.SqlParamPropertyExpression

object SqlTreeModifier : (Expression) -> Expression {

    override fun invoke(expression: Expression): Expression {
        expression.handleExpressionWithParams()
        return mapExpressionRecursively(expression)
    }

    private fun mapExpressionRecursively(original: Expression): Expression {
        val mapped = mapExp(original)
        mapped.mapChildren(::mapExpressionRecursively)
        return mapped
    }

    private fun mapExp(original: Expression): Expression {
        if (original.mustRenderDirectly) return original

        when (original) {
            is MethodCallExpression -> {
                when {
                    original.op.owner == "java/lang/String" && original.op.name == "length" && original.op.desc == "()I" -> {
                        return SqlFunctionExpression("LENGTH", listOf(original.receiver!!))
                    }
                    original.op.name.startsWith("get") && original.args.isEmpty() && original.receiver is ParamExpression -> {
                        // TODO rework check in ORM (use annotations?)
                        return SqlParamPropertyExpression(
                            original.op.name.drop(3).toLowerCase(),
                            original.receiver as ParamExpression
                        )
                    }
                    else -> {
                        throw Exception("not supported function: ${original.op.owner} ${original.op.name} ${original.op.desc}")
                    }
                }
            }
            is BinaryOperationExpression -> {
                val first = original.first
                val second = original.second
                if (second == ZeroExpression &&
                    first is MethodCallExpression &&
                    first.op.owner == "kotlin/jvm/internal/Intrinsics" &&
                    first.op.name == "compare" &&
                    first.op.desc == "(II)I"
                ) {
                    return BinaryOperationExpression(original.op, first.args[0], first.args[1])
                } else {
                    return original
                }
            }
            else -> {
                return original
            }
        }
    }
}

fun Expression.handleExpressionWithParams() {
    if (this is ParamExpression) {
        mustRenderDirectly = false
    } else {
        children.forEach { it.handleExpressionWithParams() }
        mustRenderDirectly = children.all { it.mustRenderDirectly }
    }
}

fun Expression.mapChildren(mapper: (Expression) -> Expression) {
    when (this) {
        is BinaryOperationExpression -> {
            first = mapper(first)
            second = mapper(second)
        }
        is MethodCallExpression -> {
            receiver?.let {
                receiver = mapper(it)
            }
            args = args.map(mapper)
        }
        is SqlFunctionExpression -> {
            args = args.map(mapper)
        }
    }
}
