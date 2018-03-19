package com.tgirard12.graphqlkotlindsl.graphqljava

import com.tgirard12.graphqlkotlindsl.*
import graphql.GraphQL
import graphql.schema.*
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.SchemaGenerator
import graphql.schema.idl.SchemaParser
import graphql.schema.idl.TypeRuntimeWiring


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

// Query and Mutation dataFetcher

inline fun <reified T> ActionDsl.asyncDataFetcher(
        noinline f: (DataFetchingEnvironment) -> T?): Unit {
    dataFetcher = AsyncDataFetcher<T> { f.invoke(it) }
}

inline fun <reified T> ActionDsl.staticDataFetcher(
        noinline f: () -> T?): Unit {
    dataFetcher = StaticDataFetcher(f.invoke())
}

inline fun <reified T> ActionDsl.dataFetcher(
        d: DataFetcher<T>): Unit {
    dataFetcher = d
}

// TypeDsl dataFetcher

inline fun <reified T> TypeDsl.asyncDataFetcher(
        fieldName: String? = null,
        noinline f: (DataFetchingEnvironment) -> T?): Unit {
    dataFetcher[fieldName ?: T::class.gqlNameDecapitalized()] = AsyncDataFetcher<T> { f.invoke(it) }
}

inline fun <reified T> TypeDsl.staticDataFetcher(
        fieldName: String? = null,
        noinline f: () -> T?): Unit {
    dataFetcher[fieldName ?: T::class.gqlNameDecapitalized()] = StaticDataFetcher(f.invoke())
}

inline fun <reified T> TypeDsl.dataFetcher(
        fieldName: String? = null,
        d: DataFetcher<T>): Unit {
    dataFetcher[fieldName ?: T::class.gqlNameDecapitalized()] = d
}

//

fun SchemaDsl.graphQLSchema(runtimeWiring: RuntimeWiring): GraphQLSchema =
        SchemaParser().parse(schemaString()).let { typeRegistry ->
            SchemaGenerator().makeExecutableSchema(typeRegistry, runtimeWiring)
        }

fun SchemaDsl.graphQl(runtimeWiring: RuntimeWiring, f: GraphQL.Builder.() -> Unit): GraphQL = GraphQL.newGraphQL(
        SchemaParser().parse(schemaString()).let { typeRegistry ->
            SchemaGenerator().makeExecutableSchema(typeRegistry, runtimeWiring)
        }
).apply { f() }.build()

fun SchemaDsl.graphQL(r: RuntimeWiring.Builder.() -> Unit, g: GraphQL.Builder.() -> Unit): GraphQL =
        graphQl(GqlJavaRuntimeWiringDsl.newRuntimeWiring(r), g)
