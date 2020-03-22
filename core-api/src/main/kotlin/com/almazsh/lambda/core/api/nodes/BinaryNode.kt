package com.almazsh.lambda.core.api.nodes

data class BinaryNode(
    val first: Node,
    val second: Node,
    val operation: BinaryNodeType
) : Node()

enum class BinaryNodeType {
    SUM,
    SUBTRACTION,
    MULTIPLY,
    DIVIDE,
    REMAINDER,
    AND,
    OR,
    XOR,
    EQ,
    NOT_EQ,
    GREATER,
    LESS,
    GREATER_EQ,
    LESS_EQ
}
