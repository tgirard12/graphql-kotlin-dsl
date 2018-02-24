package com.tgirard12.graphqlkotlindsl

class EnumDsl : Description {
    var name: String? = null
    override var description: String? = null

    val fields = mutableListOf<Field>()

    data class Field(
            val name: String,
            val description: String
    )
}