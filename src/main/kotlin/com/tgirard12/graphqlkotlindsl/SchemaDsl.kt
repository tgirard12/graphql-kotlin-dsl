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

${if (queries.isNotEmpty())
        """type QueryType {
${queries.joinToString("\n") { it.schemaString() }}
}
 """
    else ""}
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

    private fun QueryDsl.schemaString() = """$TAB$name${if (args.isNotEmpty()) {
        "(${args.joinToString { it.schemaString() }})"
    } else ""
    }: $returnType${if (!returnTypeNullable) "!" else ""}"""

    private fun QueryDsl.Arg.schemaString() = """$name: $type${if (!nullable) "!" else ""}"""

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

    inline fun <reified T : Any> query(queryName: String? = null, f: QueryDsl.() -> Unit): Unit {
        queries += QueryDsl().apply {
            if (T::class.typeParameters.isNotEmpty())
                throw IllegalArgumentException("""
                    |Generic types like List<String> could not be reified du to JVM type erasure
                    |You must set the type manually :
                    |query<Unit>("myQuery") {
                    |    returnType = "[String]"
                    |    returnTypeNullable = true  // not nullable by default
                    |}""".trimMargin())

            f.invoke(this)
            when {
                queryName != null -> name = queryName
                name == null -> name = T::class.gqlName()?.decapitalize()
            }
            returnType nullThen { returnType = T::class.gqlName() }
        }
    }

    inline fun <reified T : Any?> QueryDsl.arg(argName: String? = null, f: QueryDsl.Arg.() -> Unit): Unit {
        args += QueryDsl.Arg().apply {
            f.invoke(this)

            when {
                argName != null -> name = argName
                name == null -> name = T::class.gqlName().decapitalize()
            }
            type nullThen { type = T::class.gqlName() }
            nullable nullThen { nullable = T::class.gqlName().contains("?") }
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
