package com.tgirard12.graphqlkotlindsl

class EnumDsl {
    var name: String? = null
    var description: String? = null

    val fields = mutableListOf<Field>()

    data class Field(
            val name: String,
            val description: String
    )
}