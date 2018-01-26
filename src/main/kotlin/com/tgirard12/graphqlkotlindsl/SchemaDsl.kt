package com.tgirard12.graphqlkotlindsl

import com.tgirard12.graphqlkotlindsl.ActionDsl.MutationDsl
import com.tgirard12.graphqlkotlindsl.ActionDsl.QueryDsl
import graphql.schema.GraphQLSchema
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.SchemaGenerator
import graphql.schema.idl.SchemaParser
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

    fun graphQLSchema(runtimeWiring: RuntimeWiring): GraphQLSchema =
            SchemaParser().parse(schemaString()).let { typeRegistry ->
                SchemaGenerator().makeExecutableSchema(typeRegistry, runtimeWiring)
            }

    val queries = mutableListOf<QueryDsl>()
    val mutations = mutableListOf<MutationDsl>()
    val types = mutableListOf<TypeDsl>()
    val enums = mutableListOf<EnumDsl>()


    fun schemaString() = """
schema {
    ${if (queries.isNotEmpty()) "query: QueryType" else ""}
    ${if (mutations.isNotEmpty()) "mutation: MutationType" else ""}
}

${if (queries.isNotEmpty())
        """type QueryType {
${queries.sortedBy { it.name }.joinToString("\n") { it.schemaString() }}
}
 """
    else ""}
${if (mutations.isNotEmpty())
        """type MutationType {
${mutations.sortedBy { it.name }.joinToString("\n") { it.schemaString() }}
}
 """
    else ""}
${enums.sortedBy { it.name }.joinToString { it.schemaString() }}
${types.sortedBy { it.name }.joinToString { it.schemaString() }}
""".trimEnd()

    private fun TypeDsl.schemaString() = """
type $name {
${fields.sortedBy { it.name }.joinToString("\n") { TAB + it.schemaString() }}
}""".trim()

    private fun EnumDsl.schemaString() = """
enum $name {
${fields.joinToString("\n") { TAB + it.name }}
}""".trim()

    private fun TypeDsl.Field.schemaString() = """$name: $type${if (!nullable) "!" else ""}"""

    private fun ActionDsl.schemaString() = """$TAB$name${if (args.isNotEmpty()) {
        "(${args.joinToString { it.schemaString() }})"
    } else ""
    }: $returnType${if (!returnTypeNullable) "!" else ""}"""

    private fun ActionDsl.Arg.schemaString() = """$name: $type${if (!nullable) "!" else ""}"""

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
