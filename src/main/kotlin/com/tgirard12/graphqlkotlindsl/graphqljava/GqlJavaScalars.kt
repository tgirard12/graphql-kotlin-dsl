package com.tgirard12.graphqlkotlindsl.graphqljava

import graphql.schema.CoercingSerializeException
import java.util.*


class GqlJavaScalars {
    companion object {

        val uuid by lazy {
            GqlJavaExtentions.scalarTypeDsl<UUID> {
                parseLiteral {
                    when (it) {
                        is String -> UUID.fromString(it)
                        else -> null
                    }
                }
                parseValue {
                    when (it) {
                        is String -> UUID.fromString(it)
                        else -> throw CoercingSerializeException("parseValue expected type 'UUID' " +
                                "but was ${it?.javaClass?.simpleName ?: "NULL"}")
                    }
                }
                serialize {
                    when (it) {
                        is String -> it.toString()
                        else -> throw CoercingSerializeException("serialize expected type 'UUID' " +
                                "but was ${it?.javaClass?.simpleName ?: "NULL"}")
                    }
                }
            }
        }
    }
}
