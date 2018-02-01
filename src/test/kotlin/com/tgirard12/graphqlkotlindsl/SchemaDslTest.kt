package com.tgirard12.graphqlkotlindsl

import com.tgirard12.graphqlkotlindsl.graphqljava.GqlJavaExtentions
import com.tgirard12.graphqlkotlindsl.graphqljava.GqlJavaScalars
import com.tgirard12.graphqlkotlindsl.graphqljava.graphQLSchema
import com.tgirard12.graphqlkotlindsl.models.ListTypes
import com.tgirard12.graphqlkotlindsl.models.SimpleEnum
import com.tgirard12.graphqlkotlindsl.models.SimpleTypes
import graphql.Scalars
import graphql.schema.CoercingSerializeException
import graphql.schema.idl.RuntimeWiring
import io.kotlintest.KTestJUnitRunner
import io.kotlintest.specs.WordSpec
import org.junit.runner.RunWith
import java.time.LocalDateTime
import java.util.*

@RunWith(KTestJUnitRunner::class)
class SchemaDslTest : WordSpec() {

    init {
        "SchemaDslTest header" should {

            // Full schema sample
            "QueryDslTest full sample" should {
                val schemaDsl = schemaDsl {

                    // Scalar
                    scalar<Double>()
                    scalar<UUID>()
                    scalar<LocalDateTime>()

                    // Types
                    type<SimpleTypes> { }
                    type<ListTypes> { }

                    // Enum
                    enum<SimpleEnum> { }

                    // Simple query
                    query<String> { }
                    query<Int> { name = "count" }
                    query<Long> { }
                    query<Float> { returnTypeNullable = true }
                    query<Double> { }
                    query<UUID> { name = "id" }
                    query<LocalDateTime> { name = "now" }

                    // complex queries
                    query<Unit> {
                        name = "typesId"
                        returnType = "[UUID]"
                    }
                    query<Unit> {
                        name = "types"
                        returnType = "[SimpleTypes]"

                        arg<Int> { name = "count" }
                        arg<String> {
                            name = "name"
                            nullable = true
                        }
                    }
                    query<SimpleTypes> {
                        name = "type"

                        arg<UUID> { name = "id" }
                    }
                    query<SimpleEnum> { returnType = "[SimpleEnum]" }

                    // Mutations
                    mutation<String> { }
                    mutation<Int> { name = "count" }
                    mutation<Long> { returnType = "Long?" }
                    mutation<Float> { returnTypeNullable = true }
                    mutation<Double> { }
                    mutation<UUID> { name = "id" }
                    mutation<LocalDateTime> { name = "now" }

                    mutation<SimpleEnum> { }
                    mutation<SimpleTypes> {
                        arg<Long> { }
                        arg<Double> { }
                        arg<SimpleEnum> { nullable = true }
                    }
                }
                "be ok" {
                    schemaDsl schemaEqual """

schema {
    query: QueryType
    mutation: MutationType
}

type QueryType {
    count: Int!
    double: Double!
    float: Float
    id: UUID!
    long: Long!
    now: LocalDateTime!
    simpleEnum: [SimpleEnum]!
    string: String!
    type(id: UUID!): SimpleTypes!
    types(count: Int!, name: String): [SimpleTypes]!
    typesId: [UUID]!
}

type MutationType {
    count: Int!
    double: Double!
    float: Float
    id: UUID!
    long: Long?!
    now: LocalDateTime!
    simpleEnum: SimpleEnum!
    simpleTypes(long: Long!, double: Double!, simpleEnum: SimpleEnum): SimpleTypes!
    string: String!
}

scalar Double
scalar LocalDateTime
scalar UUID

enum SimpleEnum {
    val1
    VAL_2
    enum
}

type ListTypes {
    ints: [Int]!
    intsNull: [Int]
}

type SimpleTypes {
    double: Double!
    doubleNull: Double
    float: Float!
    floatNull: Float
    int: Int!
    intNull: Int
    long: Long!
    longNull: Long
    string: String!
    stringNull: String
    uuid: UUID!
    uuidNull: UUID
}

""".trimIndent()
                }
                "be a valid schema" {
                    schemaDsl.graphQLSchema(RuntimeWiring.newRuntimeWiring()
                            .scalar(GqlJavaScalars.uuid)
                            .scalar(GqlJavaExtentions.scalarTypeDsl<Double>(Scalars.GraphQLFloat.coercing) { })
                            .scalar(GqlJavaExtentions.scalarTypeDsl<LocalDateTime> {
                                serialize {
                                    when (it) {
                                        is String -> it.toString()
                                        else -> throw CoercingSerializeException("serialize expected type 'LocalDateTime' " +
                                                "but was ${it?.javaClass?.simpleName ?: "NULL"}")
                                    }
                                }
                                parseValue {
                                    when (it) {
                                        is String -> LocalDateTime.parse(it)
                                        else -> throw CoercingSerializeException("parseValue expected type 'String' " +
                                                "but was ${it?.javaClass?.simpleName ?: "NULL"}")
                                    }
                                }
                            })
                            .build()
                    )
                }
            }

            "with empty schema" {
                schemaDsl { } schemaEqual """
schema {
}
"""
            }
        }
    }
}