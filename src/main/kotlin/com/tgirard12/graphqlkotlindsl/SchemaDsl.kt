package com.tgirard12.graphqlkotlindsl

import graphql.schema.GraphQLSchema
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass
import kotlin.reflect.KClassifier
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

val log by lazy { LoggerFactory.getLogger("graphQlKotlinDsl") }

class SchemaDsl internal constructor() {

    companion object {
        private const val TAB = "    "
    }

    var graphQLSchema: GraphQLSchema? = null

    val queries = mutableListOf<QueryDsl>()
    val mutations = mutableListOf<MutationDsl>()
    val types = mutableListOf<TypeDsl>()
    val enums = mutableListOf<EnumDsl>()


    fun schemaString() = """
schema {
    ${if (queries.isNotEmpty()) "query: QueryType" else ""}
    ${if (mutations.isNotEmpty()) "mutations: MutattionType" else ""}
}

${enums.joinToString { it.schemaString() }}
${types.joinToString { it.schemaString() }}
""".trimEnd()

    private fun TypeDsl.schemaString() = """
type $name {
${fields.joinToString("\n") { TAB + it.schemaString() }}
}""".trim()

    private fun EnumDsl.schemaString() = """
enum $name {
${fields.joinToString("\n") { TAB + it.name }}
}""".trim()

    private fun TypeDsl.Field.schemaString() = """$name: $type${if (!nullable) "!" else ""}"""

    /*
     * DSL
     */
    inline fun <reified T : Any> type(f: TypeDsl.() -> Unit): Unit {
        types += TypeDsl().apply {
            f.invoke(this)
            name nullThen { name = T::class.gqlName() }

            T::class.gqlField()
                    .sortedBy { it.name }
                    .forEach {
                        fields += TypeDsl.Field(
                                name = it.name,
                                description = "",
                                enable = true,
                                type = it.gqlType(),
                                nullable = it.gqlNullable()
                        )
                    }
        }
    }

    inline fun <reified T : Enum<T>> enum(f: EnumDsl.() -> Unit): Unit {
        enums += EnumDsl().apply {
            f.invoke(this)
            name nullThen { name = T::class.gqlName() }

            enumValues<T>().forEach {
                fields += EnumDsl.Field(
                        name = it.name,
                        description = ""
                )
            }
        }
    }

    // Extensions
    inline infix fun <T> T?.nullThen(f: () -> Unit) {
        if (this == null) f()
    }
}

fun schemaDsl(f: SchemaDsl.() -> Unit): SchemaDsl = SchemaDsl().apply { f() }


fun <T : Any> KClass<out T>.gqlName(): String? = this.simpleName
fun <T : Any> KClass<out T>.gqlField() = this.memberProperties

fun <T : Any> KProperty1<out T, Any?>.gqlType(): String =
        when (returnType.arguments.isEmpty()) {
            true -> returnType.classifier.gqlName()
            false -> "[${returnType.arguments.first().type?.classifier.gqlName()}]"
        }

fun KClassifier?.gqlName(): String = (this as? KClass<*>)?.simpleName ?: "ERROR"
fun <T : Any> KProperty1<out T, Any?>.gqlNullable(): Boolean = returnType.isMarkedNullable
