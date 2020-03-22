package com.almazsh.lambda.dev.classfiles

import com.almazsh.lambda.testlambdas.api.TestExpr
import com.almazsh.lambda.testlambdas.api.toFormattedString
import com.almazsh.lambda.testsql.api.Queryable

fun testAll() {
    val z = 55
    val zz = 66
    val zzz = 77
    val str = "qwerty"

    listOf(
        TestExpr { t: Int -> t + 11 },
        TestExpr { t: Int -> t - 22 },
        TestExpr { t: Int -> t * 33 },
        TestExpr { t: Int -> t / 44 },
        TestExpr { t: Int -> t and 55 },
        TestExpr { t: Int -> t or 66 },
        TestExpr { t: Int -> t xor 77 },
        TestExpr { t: Int -> t + z }
    ).forEach { println(it.getTree()) }

    listOf(
        TestExpr { t: Int -> t == 11 },
        TestExpr { t: Int -> t != 22 },
        TestExpr { t: Int -> t > 33 },
        TestExpr { t: Int -> t < 44 },
        TestExpr { t: Int -> t >= 55 },
        TestExpr { t: Int -> t <= 66 },
        TestExpr { t: Int -> t > z }
    ).forEach { println(it.getTree()) }

    listOf<TestExpr<*>>(
        TestExpr { t: String -> t.length },
        TestExpr { t: String -> t.length > 5 },
        TestExpr { t: String -> t.length == str.length },
        TestExpr { t: String -> t.subSequence(11, 22) },
        TestExpr { t: String -> t > str }
    ).forEach { println(it.getTree().toFormattedString()) }

    Queryable("Users", User::class.java)
        .where { it.id == z }
        .asSqlString()
        .println()

    Queryable("Users", User::class.java)
        .where { it.age > (z + zz - zzz) }
        .asSqlString()
        .println()

    Queryable("Users", User::class.java)
        .where { (it.age > z) and (it.age < zz) }
        .asSqlString()
        .println()

    Queryable("Users", User::class.java)
        .where { it.name.length == z }
        .asSqlString()
        .println()
}

fun String.println() = println(this)
