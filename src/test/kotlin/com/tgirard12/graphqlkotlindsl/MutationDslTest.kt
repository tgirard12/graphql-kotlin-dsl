package com.tgirard12.graphqlkotlindsl

import com.tgirard12.graphqlkotlindsl.models.SimpleTypes
import io.kotlintest.specs.WordSpec

class MutationDslTest : WordSpec() {

    init {
        "QueryDslTest no args" should {
            "print simple Type" {
                schemaDsl {
                    mutation<String> { }
                } schemaEqual """
schema {
    mutation: MutationType
}

type MutationType {
    string: String!
}
"""
            }
            "print simple Type custom Name and description" {
                schemaDsl {
                    mutation<String> { }
                    mutation<String>("myString", mutationDescription = "String mutation") { }
                    mutation<SimpleTypes> {
                        name = "simpleTypes"
                        description = "Update a SimpleType"
                    }
                } schemaEqual """
schema {
    mutation: MutationType
}

type MutationType {
    # String mutation
    myString: String!
    # Update a SimpleType
    simpleTypes: SimpleTypes!
    string: String!
}
"""
            }
            "print nullable return type" {
                schemaDsl {
                    mutation<String>("myString") {
                        returnTypeNullable = true
                    }
                } schemaEqual """
schema {
    mutation: MutationType
}

type MutationType {
    myString: String
}
"""
            }
        }
        "QueryDslTest with args" should {
            "one arg default value" {
                schemaDsl {
                    mutation<String> {
                        arg<String> { }
                    }
                } schemaEqual """
schema {
    mutation: MutationType
}

type MutationType {
    string(string: String!): String!
}
"""
            }
            "several args and config" {
                schemaDsl {
                    mutation<String> {
                        arg<String> { nullable = true }
                        arg<SimpleTypes>("type") { }
                    }
                } schemaEqual """
schema {
    mutation: MutationType
}

type MutationType {
    string(string: String, type: SimpleTypes!): String!
}
"""
            }
            "Custom Mutation Name" {
                schemaDsl {
                    mutation<String>("myMutation") {
                        arg<String> { nullable = true }
                    }
                    mutation<String> {
                        name = "secondMutation"
                        arg<SimpleTypes>("type") { }
                        arg<Int> {
                            name = "count"
                            nullable = true
                        }
                    }
                } schemaEqual """
schema {
    mutation: MutationType
}

type MutationType {
    myMutation(string: String): String!
    secondMutation(type: SimpleTypes!, count: Int): String!
}
"""
            }
        }
        "MutationDslTest List Type" should {
            "fail on List<*>" {
                shouldThrow<IllegalArgumentException> {
                    schemaDsl {
                        mutation<List<String>> { }
                    }
                }
            }
            "set it manually" {
                schemaDsl {
                    mutation<Unit>("myMutation") {
                        returnType = "[String]"
                    }
                    mutation<Unit>("myMutation2") {
                        returnType = "[String]"
                        returnTypeNullable = true
                    }
                } schemaEqual """
schema {
    mutation: MutationType
}

type MutationType {
    myMutation: [String]!
    myMutation2: [String]
}
"""
            }
        }
    }
}