package com.tgirard12.graphqlkotlindsl.graphqljava

import graphql.Scalars
import graphql.language.StringValue
import graphql.schema.CoercingSerializeException
import java.util.*


class GqlJavaScalars {
    companion object {

        val uuid by lazy {
            GqlJavaExtentions.scalarTypeDsl<UUID> {
                parseLiteral {
                    when (it) {
                        is StringValue -> UUID.fromString(it.value)
                        else -> null
                    }
                }
                parseValue {
                    when (it) {
                        is String -> UUID.fromString(it)
                        else -> throw CoercingSerializeException("parseValue expected type UUID " +
                                "but was ${it?.javaClass?.simpleName ?: "NULL"}")
                    }
                }
                serialize {
                    when (it) {
                        is UUID -> it.toString()
                        else -> throw CoercingSerializeException("serialize expected type UUID " +
                                "but was ${it?.javaClass?.simpleName ?: "NULL"}")
                    }
                }
            }
        }

        val double by lazy {
            GqlJavaExtentions.scalarTypeDsl<Double>(Scalars.GraphQLFloat.coercing) { }
        }
    }
}
