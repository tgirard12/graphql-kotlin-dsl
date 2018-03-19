package com.tgirard12.graphqlkotlindsl

import graphql.schema.DataFetcher

sealed class ActionDsl : Description {
    var name: String? = null
    override var description: String? = null

    var returnType: String? = null
    var returnTypeNullable: Boolean = false

    val args = mutableListOf<Arg>()

    var dataFetcher: DataFetcher<*>? = null

    class Arg {
        var name: String? = null
        var description: String? = null
        var type: String? = null
        var nullable: Boolean = false
    }

    class QueryDsl : ActionDsl()
    class MutationDsl : ActionDsl()
}