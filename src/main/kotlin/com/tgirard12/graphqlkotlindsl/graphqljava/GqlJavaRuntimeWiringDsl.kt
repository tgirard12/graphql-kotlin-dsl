package com.tgirard12.graphqlkotlindsl.graphqljava

import com.tgirard12.graphqlkotlindsl.gqlName
import graphql.schema.AsyncDataFetcher
import graphql.schema.DataFetchingEnvironment
import graphql.schema.StaticDataFetcher
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.TypeRuntimeWiring

class GqlJavaRuntimeWiringDsl {

    companion object {
        fun newRuntimeWiring(f: RuntimeWiring.Builder.() -> Unit)
                : RuntimeWiring = RuntimeWiring.newRuntimeWiring().apply { f() }.build()
    }
}

fun RuntimeWiring.Builder.scalarUUID() = scalar(GqlJavaScalars.uuid)
fun RuntimeWiring.Builder.scalarDouble() = scalar(GqlJavaScalars.double)

fun RuntimeWiring.Builder.queryType(name: String = "QueryType", f: TypeRuntimeWiring.Builder.() -> Unit)
        : RuntimeWiring.Builder = type<Any>(name, f)

fun RuntimeWiring.Builder.mutationType(name: String = "MutationType", f: TypeRuntimeWiring.Builder.() -> Unit)
        : RuntimeWiring.Builder = type<Any>(name, f)

inline fun <reified T> RuntimeWiring.Builder.type(name: String? = null, f: TypeRuntimeWiring.Builder.() -> Unit)
        : RuntimeWiring.Builder {

    val newTypeWiring = TypeRuntimeWiring.newTypeWiring(name ?: T::class.gqlName())
    f(newTypeWiring)
    return type(newTypeWiring.build())
}

fun <T> TypeRuntimeWiring.Builder.asyncDataFetcher(queryName: String, f: (DataFetchingEnvironment) -> T?)
        : Unit = dataFetcher(queryName, AsyncDataFetcher<T> { f(it) }).let { Unit }


fun <T> TypeRuntimeWiring.Builder.staticDataFetcher(queryName: String, f: () -> T?)
        : Unit = dataFetcher(queryName, StaticDataFetcher(f())).let { Unit }
