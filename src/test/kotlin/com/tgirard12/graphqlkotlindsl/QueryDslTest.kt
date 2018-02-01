package com.tgirard12.graphqlkotlindsl

import com.tgirard12.graphqlkotlindsl.models.SimpleTypes
import io.kotlintest.KTestJUnitRunner
import io.kotlintest.specs.WordSpec
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class QueryDslTest : WordSpec() {

    init {
        "QueryDslTest no args" should {
            "print simple Type" {
                schemaDsl {
                    query<String> { }
                } schemaEqual """
schema {
    query: QueryType
}

type QueryType {
    string: String!
}
"""
            }
            "print simple Type custom Name" {
                schemaDsl {
                    query<String> { }
                    query<String>("myString") { }
                    query<SimpleTypes> { name = "simpleTypes" }
                } schemaEqual """
schema {
    query: QueryType
}

type QueryType {
    myString: String!
    simpleTypes: SimpleTypes!
    string: String!
}
"""
            }
            "print nullable return type" {
                schemaDsl {
                    query<String>("myString") {
                        returnTypeNullable = true
                    }
                } schemaEqual """
schema {
    query: QueryType
}

type QueryType {
    myString: String
}
"""
            }
        }
        "QueryDslTest with args" should {
            "one arg default value" {
                schemaDsl {
                    query<String> {
                        arg<String> { }
                    }
                } schemaEqual """
schema {
    query: QueryType
}

type QueryType {
    string(string: String!): String!
}
"""
            }
            "several args and config" {
                schemaDsl {
                    query<String> {
                        arg<String> { nullable = true }
                        arg<SimpleTypes>("type") { }
                    }
                } schemaEqual """
schema {
    query: QueryType
}

type QueryType {
    string(string: String, type: SimpleTypes!): String!
}
"""
            }
            "Custom Query Name" {
                schemaDsl {
                    query<String>("myQuery") {
                        arg<String> { nullable = true }
                    }
                    query<String> {
                        name = "secondQuery"
                        arg<SimpleTypes>("type") { }
                        arg<Int> {
                            name = "count"
                            nullable = true
                        }
                    }
                } schemaEqual """
schema {
    query: QueryType
}

type QueryType {
    myQuery(string: String): String!
    secondQuery(type: SimpleTypes!, count: Int): String!
}
"""
            }
        }
        "QueryDslTest List Type" should {
            "fail on List<*>" {
                shouldThrow<IllegalArgumentException> {
                    schemaDsl {
                        query<List<String>> { }
                    }
                }
            }
            "set it manually" {
                schemaDsl {
                    query<Unit>("myQuery") {
                        returnType = "[String]"
                    }
                    query<Unit>("myQuery2") {
                        returnType = "[String]"
                        returnTypeNullable = true
                    }
                } schemaEqual """
schema {
    query: QueryType
}

type QueryType {
    myQuery: [String]!
    myQuery2: [String]
}
"""
            }
        }
    }
}
