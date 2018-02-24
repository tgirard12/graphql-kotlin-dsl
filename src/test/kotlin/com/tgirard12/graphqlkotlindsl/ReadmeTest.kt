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
                    type<User>(typeDescription = "An User") {
                        desc("email", "User Email")

                        addField<String>(name = "otherName") { }
                        addField<Right> {
                            description = "User Right"
                            nullable = true
                        }
                    }
                    enum<Right>(enumDescription = "An enum") { }

                    // Queries
                    query<User>(queryDescription = "User By Id") {
                        arg<UUID> { name = "id" }
                    }
                    query<Unit> {
                        name = "users"
                        description = "All Users"
                        returnType = "[User]"
                    }

                    // Mutations
                    mutation<User>(mutationDescription = "Update a user") {
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

        }
    }
}