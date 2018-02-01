package com.tgirard12.graphqlkotlindsl.graphqljava

class GraphQLScalarTypeDsl<T> {

    var name: String? = null
    var description: String? = null

    var _parseLiteral: (input: Any?) -> T? = { TODO() }
    var _parseValue: (input: Any?) -> T = { TODO() }
    var _serialize: (dataFetcherResult: Any?) -> String = { TODO() }

    fun parseLiteral(f: (input: Any?) -> T?) {
        _parseLiteral = f
    }

    fun parseValue(f: (input: Any?) -> T) {
        _parseValue = f
    }

    fun serialize(f: (dataFetcherResult: Any?) -> String) {
        _serialize = f
    }
}
