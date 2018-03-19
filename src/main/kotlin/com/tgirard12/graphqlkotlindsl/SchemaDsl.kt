package com.tgirard12.graphqlkotlindsl

import com.tgirard12.graphqlkotlindsl.ActionDsl.MutationDsl
import com.tgirard12.graphqlkotlindsl.ActionDsl.QueryDsl
import graphql.schema.GraphQLScalarType
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

    private fun TypeDsl.schemaString() = "${descriptionString()}type $name {$NLINE" +
            fields.filter { it.enable == true }
                    .sortedBy { it.name }
                    .joinToString(NLINE) { it.schemaString() } +
            addFields.sortedBy { it.name }
                    .joinToString(separator = NLINE) { it.schemaString() }
                    .let {
                        if (it.isBlank()) ""
                        else NLINE + NLINE + it
                    } +
            NLINE +
            "}" +
            NLINE

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

    private fun ScalarDsl.schemaString() = """${descriptionString()}scalar $name"""
    private fun TypeDsl.Field.schemaString() = """${descriptionString(TAB)}$TAB$name: $type${if (!nullable) "!" else ""}"""

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

    private fun ActionDsl.schemaString() = """${descriptionString(TAB)}$TAB$name${if (args.isNotEmpty()) {
        "(${args.joinToString { it.schemaString() }})"
    } else ""
    }: $returnType${if (!returnTypeNullable) "!" else ""}"""

    private fun ActionDsl.Arg.schemaString() = """$name: $type${if (!nullable) "!" else ""}"""

    /*
     * DSL
     */
    inline fun <reified T : Any> scalar(
            scalarDescription: String? = null,
            noinline f: (() -> GraphQLScalarType)? = null
    ): Unit {
        scalars += ScalarDsl().apply {
            description = scalarDescription
            name = T::class.gqlName()

            graphQlScalarType = f?.invoke()
        }
    }

    inline fun <reified T : Any> type(typeDescription: String? = null,
                                      f: TypeDsl.() -> Unit): Unit {
        types += TypeDsl().apply {
            description = typeDescription
            name = T::class.gqlName()
            f.invoke(this)

            T::class.gqlField()
                    .sortedBy { it.name }
                    .forEach {

                        val cf = descriptions[it.name]

                        fields += TypeDsl.Field().apply {
                            name = it.name
                            description = cf
                            enable = !dropFields.any { df -> df.name == it.name }
                            type = it.gqlType()
                            nullable = it.gqlNullable()
                        }
                    }

            descriptions.forEach { cf ->
                if (!fields.any { it.name == cf.key })
                    throw IllegalArgumentException("Type '$name.${cf.key}' does not exist")
            }
            dropFields.forEach { df ->
                if (!fields.any { it.name == df.name })
                    throw IllegalArgumentException("Type '$name.${df.name}' does not exist")
            }
        }
    }

    fun TypeDsl.desc(fieldName: String, description: String) {
        if (descriptions.any { it.key == fieldName })
            throw IllegalArgumentException("Description '$description' on type '${this.name}.$fieldName' does not exist")

        descriptions[fieldName] = description
    }

    inline fun <reified T : Any> TypeDsl.addField(name: String? = null,
                                                  description: String? = null,
                                                  f: TypeDsl.Field.() -> Unit): Unit {
        addFields += TypeDsl.Field().apply {
            this.name = name
            this.name nullThen { this.name = T::class.gqlName()?.decapitalize() }
            this.type = T::class.gqlName()
            this.description = description
            f.invoke(this)
        }
    }

    fun TypeDsl.dropField(name: String): Unit {
        dropFields += TypeDsl.Field().apply {
            this.name = name
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

    inline fun <reified T : Any> query(queryName: String? = null,
                                       queryDescription: String? = null,
                                       f: QueryDsl.() -> Unit): Unit {
        queries += QueryDsl().apply {
            if (T::class.typeParameters.isNotEmpty())
                throw IllegalArgumentException("""
                    |Generic types like List<String> could not be reified du to JVM type erasure
                    |You must set the type manually :
                    |query<Unit>("myQuery") {
                    |    returnType = "[String]"
                    |    returnTypeNullable = true  // not nullable by default
                    |}""".trimMargin())

            description = queryDescription
            f.invoke(this)
            when {
                queryName != null -> name = queryName
                name == null -> name = T::class.gqlName()?.decapitalize()
            }
            returnType nullThen { returnType = T::class.gqlName() }
        }
    }

    inline fun <reified T : Any> mutation(mutationName: String? = null,
                                          mutationDescription: String? = null,
                                          f: MutationDsl.() -> Unit): Unit {
        mutations += MutationDsl().apply {
            if (T::class.typeParameters.isNotEmpty())
                throw IllegalArgumentException("""
                    |Generic types like List<String> could not be reified du to JVM type erasure
                    |You must set the type manually :
                    |mutation<Unit>("myQuery") {
                    |    returnType = "[String]"
                    |    returnTypeNullable = true  // not nullable by default
                    |}""".trimMargin())

            description = mutationDescription
            f.invoke(this)
            when {
                mutationName != null -> name = mutationName
                name == null -> name = T::class.gqlName()?.decapitalize()
            }
            returnType nullThen { returnType = T::class.gqlName() }
        }
    }

    inline fun <reified T : Any?> ActionDsl.arg(argName: String? = null,
                                                f: ActionDsl.Arg.() -> Unit): Unit {
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
fun KClassifier?.gqlNameDecapitalized(): String = gqlName().decapitalize()

fun <T : Any> KProperty1<out T, Any?>.gqlNullable(): Boolean = returnType.isMarkedNullable
