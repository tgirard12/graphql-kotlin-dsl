package com.tgirard12.graphqlkotlindsl

class TypeDsl : Description {
    var name: String? = null
    override var description: String? = null

    val fields = mutableListOf<Field>()

    data class Field(
            val name: String,
            val type: String,
            override var description: String? = null,
            val enable: Boolean,
            val nullable: Boolean
    ) : Description
}