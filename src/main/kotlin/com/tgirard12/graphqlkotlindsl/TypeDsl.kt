package com.tgirard12.graphqlkotlindsl

class TypeDsl {
    var name: String? = null
    var description: String? = null

    val fields = mutableListOf<Field>()

    data class Field(
            val name: String,
            val type: String,
            val description: String,
            val enable: Boolean,
            val nullable: Boolean
    )
}