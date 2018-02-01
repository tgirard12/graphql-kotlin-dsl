package com.tgirard12.graphqlkotlindsl.graphqljava

import com.tgirard12.graphqlkotlindsl.SchemaDsl
import com.tgirard12.graphqlkotlindsl.gqlName
import graphql.schema.Coercing
import graphql.schema.GraphQLScalarType
import graphql.schema.GraphQLSchema
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.SchemaGenerator
import graphql.schema.idl.SchemaParser


class GqlJavaExtentions {
    companion object {

        inline fun <reified T> scalarTypeDsl(coercing: Coercing<Any, Any>? = null, f: GraphQLScalarTypeDsl<T>.() -> Unit): GraphQLScalarType =
                GraphQLScalarTypeDsl<T>().run {
                    f()

                    name nullThen { name = T::class.gqlName() }

                    GraphQLScalarType(name, description, coercing ?: object : Coercing<T, String> {
                        override fun parseValue(input: Any?): T = _parseValue(input)
                        override fun parseLiteral(input: Any?): T? = _parseLiteral(input)
                        override fun serialize(dataFetcherResult: Any?): String = _serialize(dataFetcherResult)
                    })
                }

        inline infix fun <T> T?.nullThen(f: () -> Unit) {
            if (this == null) f()
        }
    }
}

fun SchemaDsl.graphQLSchema(runtimeWiring: RuntimeWiring): GraphQLSchema =
        SchemaParser().parse(schemaString()).let { typeRegistry ->
            SchemaGenerator().makeExecutableSchema(typeRegistry, runtimeWiring)
        }
