package com.tgirard12.graphqlkotlindsl

import com.tgirard12.graphqlkotlindsl.ActionDsl.MutationDsl
import com.tgirard12.graphqlkotlindsl.ActionDsl.QueryDsl
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass
import kotlin.reflect.KClassifier
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

val log by lazy { LoggerFactory.getLogger("graphQlKotlinDsl") }

class SchemaDsl internal constructor() {

    companion object {
        private const val TAB = "    "
        private const val NLINE = "\n"
    }

    val scalars = mutableListOf<ScalarDsl>()
    val queries = mutableListOf<QueryDsl>()
    val mutations = mutableListOf<MutationDsl>()
    val types = mutableListOf<TypeDsl>()
    val enums = mutableListOf<EnumDsl>()


    fun schemaString() = """
schema {${queries.queryType()}${mutations.mutationType()}
}
""" + queries.queriesString() + mutations.mutationsString() + scalars.schemaScalar() + enums.schemaEnums() + types.schemaTypes()


    private fun <T> List<T>.joinSchemaString(f: (T) -> String): String = this.joinToString(separator = NLINE, transform = f)

    private fun List<TypeDsl>.schemaTypes() = when {
        isNotEmpty() -> NLINE + types.sortedBy { it.name }.joinSchemaString { it.schemaString() }
        else -> ""
    }

    private fun Description.descriptionString(prefix: String = "") = description?.let {
        """
            |$prefix# $description
            |""".trimMargin()
    } ?: ""

    private fun TypeDsl.schemaString() = """type $name {
${fields.sortedBy { it.name }.joinToString(NLINE) { TAB + it.schemaString() }}
}
"""

    private fun List<EnumDsl>.schemaEnums() = when {
        isNotEmpty() -> NLINE + enums.sortedBy { it.name }.joinSchemaString { it.schemaString() }
        else -> ""
    }

    private fun EnumDsl.schemaString() = """${descriptionString()}enum $name {
${fields.joinToString(NLINE) { TAB + it.name }}
}
"""


    private fun List<ScalarDsl>.schemaScalar() = when {
        isNotEmpty() -> """
${scalars.sortedBy { it.name }.joinSchemaString { it.schemaString() }}
"""
        else -> ""
    }

    private fun ScalarDsl.schemaString() = """scalar $name"""
    private fun TypeDsl.Field.schemaString() = """$name: $type${if (!nullable) "!" else ""}"""

    private fun List<QueryDsl>.queryType() = when {
        isNotEmpty() -> "$NLINE${TAB}query: QueryType"
        else -> ""
    }

    private fun List<QueryDsl>.queriesString() = when {
        isNotEmpty() -> """
type QueryType {
${this.sortedBy { it.name }.joinSchemaString { it.schemaString() }}
}
"""
        else -> ""
    }

    private fun List<MutationDsl>.mutationType() = when {
        isNotEmpty() -> "$NLINE${TAB}mutation: MutationType"
        else -> ""
    }

    private fun List<MutationDsl>.mutationsString() = when {
        isNotEmpty() -> """
type MutationType {
${this.sortedBy { it.name }.joinSchemaString { it.schemaString() }}
}
"""
        else -> ""
    }

    private fun ActionDsl.schemaString() = """$TAB$name${if (args.isNotEmpty()) {
        "(${args.joinToString { it.schemaString() }})"
    } else ""
    }: $returnType${if (!returnTypeNullable) "!" else ""}"""

    private fun ActionDsl.Arg.schemaString() = """$name: $type${if (!nullable) "!" else ""}"""

    /*
     * DSL
     */
    inline fun <reified T : Any> scalar(): Unit {
        scalars += ScalarDsl().apply {
            name = T::class.gqlName()
        }
    }

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

    inline fun <reified T : Enum<T>> enum(enumDescription: String? = null,
                                          f: EnumDsl.() -> Unit): Unit {
        enums += EnumDsl().apply {
            description = enumDescription
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

    inline fun <reified T : Any> mutation(queryName: String? = null, f: MutationDsl.() -> Unit): Unit {
        mutations += MutationDsl().apply {
            if (T::class.typeParameters.isNotEmpty())
                throw IllegalArgumentException("""
                    |Generic types like List<String> could not be reified du to JVM type erasure
                    |You must set the type manually :
                    |mutation<Unit>("myQuery") {
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

    inline fun <reified T : Any?> ActionDsl.arg(argName: String? = null, f: ActionDsl.Arg.() -> Unit): Unit {
        args += ActionDsl.Arg().apply {
            f.invoke(this)

            when {
                argName != null -> name = argName
                name == null -> name = T::class.gqlName().decapitalize()
            }
            type nullThen { type = T::class.gqlName() }
            nullable nullThen { nullable = T::class.gqlName().contains("?") }
        }
    }

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
