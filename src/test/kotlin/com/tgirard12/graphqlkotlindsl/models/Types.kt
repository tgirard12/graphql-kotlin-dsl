package com.tgirard12.graphqlkotlindsl.models

import java.util.*

data class SimpleTypes(
        val int: Int,
        val intNull: Int?,
        val long: Long,
        val longNull: Long?,
        val float: Float,
        val floatNull: Float?,
        val double: Double,
        val doubleNull: Double?,
        val string: String,
        val stringNull: String?,
        val uuid: UUID,
        val uuidNull: UUID?,
        val user: User
)

data class ListTypes(
        val ints: List<Int>,
        val intsNull: List<Int>?
)

enum class SimpleEnum {
    val1, VAL_2, `enum`
}

data class User(
        val name: String,
        val email: String
)

enum class Right {
    read, write, execute
}