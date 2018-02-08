package com.tgirard12.graphqlkotlindsl.execution

import com.tgirard12.graphqlkotlindsl.graphqljava.*
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
class ExecutionTest : WordSpec() {

    val users = listOf(
            User(id = UUID.fromString("b6214ea0-fc5a-493c-91ea-939e17b2e95f"), name = "John", email = "john@mail.com"),
            User(id = UUID.fromString("c682a4c5-e66b-4dbf-a077-d97579c308dc"), name = "Doe", email = "doe@mail.com")
    )

    val simpleTypes = listOf(
            SimpleTypes(1, 2, 3, 4, 5.1f, 5.2f, 6.1, 6.2,
                    "val", "null val",
                    UUID.fromString("dac5310f-484b-4f81-9756-bce0349ceaa5"), UUID.fromString("acb53d26-3cba-4177-ba54-88232b5066c5"),
                    null),
            SimpleTypes(10, null, 30, null, 5.1f, null, 6.1, null,
                    "val 10", null,
                    UUID.fromString("b10fc3b8-b96b-4344-9430-ff7c861dc1f6"), null,
                    null),
            SimpleTypes(100, 200, 300, 400, 500.1f, 500.2f, 600.1, 600.2,
                    "val 100", "null val 100",
                    UUID.fromString("49307d83-a4cc-4452-8fdc-6462843e8f66"), UUID.fromString("454473e5-2673-4e45-a429-50bdd449c0fb"),
                    null)
    )

    val graphQl = schemaDsl {
        // Scalar
        scalar<Double>()
        scalar<UUID>()

        // Types and Enums
        type<User> { }
        type<SimpleTypes> { }
        enum<Right> { }

        // Queries
        query<User> {
            arg<UUID> { name = "id" }
            returnTypeNullable = true
        }
        query<Unit> {
            name = "users"
            returnType = "[User]"
        }
        query<Unit>("typeByNames") {
            returnType = "[SimpleTypes]"
            arg<String>("name") {
                nullable = true
            }
            arg<Int> {
                name = "count"
            }
        }

        // Mutations
        mutation<User> {
            name = "updateUser"

            arg<String>("name") {
                nullable = true
            }
            arg<String>("email") { }
        }
    }.graphQL({
        scalarUUID()
        scalarDouble()
        queryType {
            asyncDataFetcher<User>("user") { e ->
                e.arguments["id"]?.let { id ->
                    users.firstOrNull {
                        id == it.id
                    }
                }
            }
            staticDataFetcher<List<User>>("users") { users }
            asyncDataFetcher<List<SimpleTypes>>("typeByNames") { e ->
                e.arguments["count"]?.let { count ->
                    if (count is Int) simpleTypes.take(count)
                    else null
                }
            }
        }
        mutationType {
            asyncDataFetcher("updateUser") { e ->
                User(id = UUID.fromString("773b29ba-6b2b-49fe-8cb1-36134689c458"),
                        name = e.arguments["name"] as String? ?: "",
                        email = e.arguments["email"] as String)
            }
        }
        type<SimpleTypes> {
            asyncDataFetcher<User>("user") {
                users[0]
            }
        }
    }, {
        queryExecutionStrategy(AsyncExecutionStrategy())
    })

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
