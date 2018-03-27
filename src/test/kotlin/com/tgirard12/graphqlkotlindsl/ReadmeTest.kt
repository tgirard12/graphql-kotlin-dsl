package com.tgirard12.graphqlkotlindsl

import Stubs
import com.tgirard12.graphqlkotlindsl.graphqljava.GqlJavaScalars
import com.tgirard12.graphqlkotlindsl.graphqljava.asyncDataFetcher
import com.tgirard12.graphqlkotlindsl.graphqljava.graphQL
import com.tgirard12.graphqlkotlindsl.graphqljava.staticDataFetcher
import com.tgirard12.graphqlkotlindsl.models.Right
import com.tgirard12.graphqlkotlindsl.models.User
import graphql.GraphQLError
import graphql.execution.AsyncExecutionStrategy
import io.kotlintest.KTestJUnitRunner
import io.kotlintest.specs.WordSpec
import org.junit.runner.RunWith
import java.util.*

@RunWith(KTestJUnitRunner::class)
class ReadmeTest : WordSpec() {

    private val schemaDsl = schemaDsl {
        // Scalar
        scalar<Double> { GqlJavaScalars.double }
        scalar<UUID> { GqlJavaScalars.uuid }

        // Types and Enums
        type<User>(typeDescription = "An User") {
            desc("email", "User Email")

            addField<String>(name = "otherName") {
                asyncDataFetcher<String>("otherName") {
                    "MyOtherName"
                }
            }
            addField<Right> {
                description = "User Right"
                nullable = true
                asyncDataFetcher<Right>("right") {
                    Right.execute
                }
            }

            dropField("deleteField")
        }
        enum<Right>(enumDescription = "An enum") { }

        // Queries
        query<User>(queryDescription = "User By Id") {
            arg<UUID> { name = "id" }

            asyncDataFetcher { env ->
                Stubs.users.firstOrNull { it.id == env.arguments["id"] }
            }
        }
        query<Unit> {
            name = "users"
            description = "All Users"
            returnType = "[User]"

            staticDataFetcher {
                Stubs.users
            }
        }

        // Mutations
        mutation<User>(mutationDescription = "Update a user") {
            name = "updateUser"

            arg<Int>("count") {
                nullable = true
            }
            arg<String> { name = "name" }

            asyncDataFetcher { env ->
                User(UUID.randomUUID(), env.arguments["name"] as String, "email@gql.io", 5)
            }
        }
    }

    init {
        "readme" should {
            "valid README Schema Sample" {
                schemaDsl schemaEqual """
schema {
    query: QueryType
    mutation: MutationType
}

type QueryType {
    # User By Id
    user(id: UUID!): User!
    # All Users
    users: [User]!
}

type MutationType {
    # Update a user
    updateUser(count: Int, name: String!): User!
}

scalar Double
scalar UUID

# An enum
enum Right {
    read
    write
    execute
}

# An User
type User {
    # User Email
    email: String!
    id: UUID!
    name: String!

    otherName: String!
    # User Right
    right: Right
}
"""
            }

            "valid README DataFectcherDsl" {
                val graphQL = schemaDsl.graphQL {
                    queryExecutionStrategy(AsyncExecutionStrategy())
                }

                val queryRes = graphQL.execute("""
                    |query user {
                    |   user(id: "b6214ea0-fc5a-493c-91ea-939e17b2e95f") {
                    |       id
                    |       email
                    |       name
                    |       otherName
                    |       right
                    |   }
                    |}""".trimMargin())
                queryRes.errors shouldBe listOf<GraphQLError>()
                queryRes.getData<Map<String, Map<String, Any>>>() shouldBe hashMapOf(
                        "user" to hashMapOf(
                                "id" to "b6214ea0-fc5a-493c-91ea-939e17b2e95f",
                                "email" to "john@mail.com",
                                "name" to "John",
                                "right" to Right.execute.name,
                                "otherName" to "MyOtherName"
                        )
                )
            }
        }
    }
}