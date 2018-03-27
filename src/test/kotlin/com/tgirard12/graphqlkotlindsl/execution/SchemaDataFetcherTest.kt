package com.tgirard12.graphqlkotlindsl.execution

import Stubs.simpleTypes
import Stubs.users
import com.tgirard12.graphqlkotlindsl.graphqljava.GqlJavaScalars
import com.tgirard12.graphqlkotlindsl.graphqljava.asyncDataFetcher
import com.tgirard12.graphqlkotlindsl.graphqljava.graphQL
import com.tgirard12.graphqlkotlindsl.graphqljava.staticDataFetcher
import com.tgirard12.graphqlkotlindsl.models.Right
import com.tgirard12.graphqlkotlindsl.models.SimpleTypes
import com.tgirard12.graphqlkotlindsl.models.User
import com.tgirard12.graphqlkotlindsl.schemaDsl
import graphql.ExecutionInput
import graphql.GraphQLError
import graphql.execution.AsyncExecutionStrategy
import io.kotlintest.KTestJUnitRunner
import io.kotlintest.specs.WordSpec
import org.junit.runner.RunWith
import java.util.*

@RunWith(KTestJUnitRunner::class)
class SchemaDataFetcherTest : WordSpec() {

    val graphQl = schemaDsl {
        // Scalar
        scalar<Double> {
            GqlJavaScalars.double
        }
        scalar<UUID> {
            GqlJavaScalars.uuid
        }

        // Types and Enums
        type<User> { }
        type<SimpleTypes> {
            asyncDataFetcher<User>("user") {
                users[0]
            }
        }
        enum<Right> { }

        // Queries
        query<User> {
            arg<UUID> { name = "id" }
            returnTypeNullable = true

            asyncDataFetcher {
                users[0]
            }
        }
        query<Unit> {
            name = "users"
            returnType = "[User]"

            staticDataFetcher {
                users
            }
        }
        query<Unit>("typeByNames") {
            returnType = "[SimpleTypes]"
            arg<String>("name") {
                nullable = true
            }
            arg<Int> {
                name = "count"
            }
            asyncDataFetcher { e ->
                e.arguments["count"]?.let { count ->
                    if (count is Int) simpleTypes.take(count)
                    else null
                }
            }
        }

        // Mutations
        mutation<User> {
            name = "updateUser"
            arg<String>("name") {
                nullable = true
            }
            arg<String>("email") {
            }
            asyncDataFetcher { e ->
                User(id = UUID.fromString("773b29ba-6b2b-49fe-8cb1-36134689c458"),
                        name = e.arguments["name"] as String? ?: "",
                        email = e.arguments["email"] as String,
                        deleteField = 2)
            }
        }
    }.graphQL {
        queryExecutionStrategy(AsyncExecutionStrategy())
    }

    init {
        "Query execution" should {
            "query users" {
                val execution = graphQl.executeAsync(
                        ExecutionInput.newExecutionInput()
                                .query("""query users {
                                         |   users {
                                         |       id
                                         |       name
                                         |       email
                                         |   }
                                         |}""".trimMargin())
                                .operationName("users")
                                .build())
                        .get()
                execution.errors shouldEqual listOf<GraphQLError>()

                val usersRes = execution.getData<Map<String, List<Map<String, Any>>>>()["users"]!!
                usersRes.size shouldEqual 2
                usersRes[0].let {
                    it["id"] shouldEqual "b6214ea0-fc5a-493c-91ea-939e17b2e95f"
                    it["name"] shouldEqual "John"
                    it["email"] shouldEqual "john@mail.com"
                }
                usersRes[1].let {
                    it["id"] shouldEqual "c682a4c5-e66b-4dbf-a077-d97579c308dc"
                    it["name"] shouldEqual "Doe"
                    it["email"] shouldEqual "doe@mail.com"
                }
            }
            "query user by id" {
                val execution = graphQl.executeAsync(
                        ExecutionInput.newExecutionInput()
                                .query("""query user {
                                         |   user(id: "b6214ea0-fc5a-493c-91ea-939e17b2e95f") {
                                         |       id
                                         |       name
                                         |       email
                                         |   }
                                         |}""".trimMargin())
                                .operationName("user")
                                .build())
                        .get()
                execution.errors shouldEqual listOf<GraphQLError>()

                val user = execution.getData<Map<String, Map<String, Any>>>()["user"]!!
                user.size shouldEqual 3
                user["id"] shouldEqual "b6214ea0-fc5a-493c-91ea-939e17b2e95f"
                user["name"] shouldEqual "John"
                user["email"] shouldEqual "john@mail.com"
            }
            "query simpleType" {
                val execution = graphQl.executeAsync(
                        ExecutionInput.newExecutionInput()
                                .query("""query typeByNames {
                                         |   typeByNames(count: 2) {
                                         |       int
                                         |       intNull
                                         |       long
                                         |       longNull
                                         |       float
                                         |       floatNull
                                         |       double
                                         |       doubleNull
                                         |       string
                                         |       stringNull
                                         |       uuid
                                         |       uuidNull
                                         |       user {
                                         |          id
                                         |          name
                                         |          email
                                         |       }
                                         |   }
                                         |}""".trimMargin())
                                .operationName("typeByNames")
                                .build())
                        .get()
                execution.errors shouldEqual listOf<GraphQLError>()

                val types = execution.getData<Map<String, List<Map<String, Any>>>>()["typeByNames"]!!
                types.size shouldEqual 2
                types[0].let {
                    it["int"] shouldEqual 1
                    it["intNull"] shouldEqual 2
                    it["long"] shouldEqual 3L
                    it["longNull"] shouldEqual 4L
                    it["float"] shouldEqual 5.1
                    it["floatNull"] shouldEqual 5.2
                    it["double"] shouldEqual 6.1
                    it["doubleNull"] shouldEqual 6.2
                    it["string"] shouldEqual "val"
                    it["stringNull"] shouldEqual "null val"
                    it["uuid"] shouldEqual "dac5310f-484b-4f81-9756-bce0349ceaa5"
                    it["uuidNull"] shouldEqual "acb53d26-3cba-4177-ba54-88232b5066c5"
                    (it["user"] as Map<*, *>).let {
                        it["id"] shouldEqual "b6214ea0-fc5a-493c-91ea-939e17b2e95f"
                        it["name"] shouldEqual "John"
                        it["email"] shouldEqual "john@mail.com"
                    }
                }
            }
        }
        "Mutation execution" should {
            "mutation updateUser" {
                val mutation = graphQl.executeAsync(
                        ExecutionInput.newExecutionInput()
                                .query("""mutation updateUser {
                            |   updateUser(name: "john doe", email: "john.doe@mail.com") {
                            |       id
                            |       name
                            |       email
                            |   }
                            |}""".trimMargin())
                                .operationName("updateUser")
                                .build())
                        .get()
                        .getData<Map<String, Map<String, Any>>>()["updateUser"]!!
                mutation.let {
                    it["id"] shouldEqual "773b29ba-6b2b-49fe-8cb1-36134689c458"
                    it["name"] shouldEqual "john doe"
                    it["email"] shouldEqual "john.doe@mail.com"
                }
            }
        }
    }
}
