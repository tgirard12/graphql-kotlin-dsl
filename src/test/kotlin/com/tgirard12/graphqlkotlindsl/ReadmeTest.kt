package com.tgirard12.graphqlkotlindsl

import com.tgirard12.graphqlkotlindsl.models.Right
import com.tgirard12.graphqlkotlindsl.models.User
import io.kotlintest.KTestJUnitRunner
import io.kotlintest.specs.WordSpec
import org.junit.runner.RunWith
import java.util.*

@RunWith(KTestJUnitRunner::class)
class ReadmeTest : WordSpec() {

    init {
        "readme" should {
            "valid README Sample" {
                schemaDsl {
                    // Scalar
                    scalar<Double>()
                    scalar<UUID>()

                    // Types and Enums
                    type<User> { }
                    enum<Right>(enumDescription = "An enum") { }

                    // Queries
                    query<User> {
                        arg<UUID> { name = "id" }
                    }
                    query<Unit> {
                        name = "users"
                        returnType = "[User]"
                    }

                    // Mutations
                    mutation<User> {
                        name = "updateUser"

                        arg<Int> {
                            name = "count"
                            nullable = true
                        }
                        arg<String> { name = "name" }
                    }
                } schemaEqual """
schema {
    query: QueryType
    mutation: MutationType
}

type QueryType {
    user(id: UUID!): User!
    users: [User]!
}

type MutationType {
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

type User {
    email: String!
    id: UUID!
    name: String!
}
"""
            }

        }
    }
}