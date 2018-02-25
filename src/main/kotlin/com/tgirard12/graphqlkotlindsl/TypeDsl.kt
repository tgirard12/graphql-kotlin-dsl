package com.tgirard12.graphqlkotlindsl

class TypeDsl : Description {
    var name: String? = null
    override var description: String? = null

    val fields = mutableListOf<Field>()
    val descriptions = mutableMapOf<String, String>()
    val addFields = mutableListOf<Field>()
    val dropFields = mutableListOf<Field>()

    class Field : Description {
        var name: String? = null
        var type: String? = null
        override var description: String? = null
        var enable: Boolean = true
        var nullable: Boolean = false
    }
}