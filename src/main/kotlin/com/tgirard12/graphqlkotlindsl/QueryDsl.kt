package com.tgirard12.graphqlkotlindsl

sealed class ActionDsl {
    var name: String? = null
    var description: String? = null

    var returnType: String? = null
    var returnTypeNullable: Boolean = false

    val args = mutableListOf<Arg>()

    class Arg {
        var name: String? = null
        var description: String? = null
        var type: String? = null
        var nullable: Boolean = false
    }

    class QueryDsl : ActionDsl()
    class MutationDsl : ActionDsl()
}