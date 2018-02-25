package com.tgirard12.graphqlkotlindsl

import com.tgirard12.graphqlkotlindsl.models.ListTypes
import com.tgirard12.graphqlkotlindsl.models.SimpleTypes
import io.kotlintest.KTestJUnitRunner
import io.kotlintest.specs.WordSpec
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class TypeDslTest : WordSpec() {

    init {
        "TypeDslTest schema" should {
            "print simple Type" {
                schemaDsl {
                    type<SimpleTypes> { }
                } schemaEqual """
schema {
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
    user: User
    uuid: UUID!
    uuidNull: UUID
}
"""
            }
            "print List types" {
                schemaDsl {
                    type<ListTypes> { }
                } schemaEqual """
schema {
}

type ListTypes {
    ints: [Int]!
    intsNull: [Int]
}
"""
            }
            "print Description" {
                schemaDsl {
                    type<ListTypes>(typeDescription = "List Type") {}
                } schemaEqual """
schema {
}

# List Type
type ListTypes {
    ints: [Int]!
    intsNull: [Int]
}
"""
            }
        }
        "TypeDslTest.Field schema" should {
            "fail if 2 has same name" {
                shouldThrow<IllegalArgumentException> {
                    schemaDsl {
                        type<ListTypes> {
                            desc("ints", "Ints descr 1")
                            desc("ints", "Ints descr 2")
                        }
                    }
                }.message shouldEqual "Description 'Ints descr 2' on type 'ListTypes.ints' does not exist"
            }
            "fail if field name not exist" {
                shouldThrow<IllegalArgumentException> {
                    schemaDsl {
                        type<ListTypes> {
                            desc("intNotExist", "Ints Not Exist")
                        }
                    }
                }.message shouldEqual "Type 'ListTypes.intNotExist' does not exist"
            }
            "add description on field" {
                schemaDsl {
                    type<ListTypes> {
                        desc("ints", "Ints description")
                    }
                } schemaEqual """
schema {
}

type ListTypes {
    # Ints description
    ints: [Int]!
    intsNull: [Int]
}
"""
            }
        }
        "TypeDslTest addField schema" should {
            "fail on exist Type" {
                schemaDsl {
                    type<ListTypes> {
                        addField<Int> {}
                        addField<Long> {
                            name = "countLong"
                            description = "Long description"
                            nullable = true
                        }
                        addField<String>(name = "stringField", description = "string decr") { }
                    }
                } schemaEqual """
schema {
}

type ListTypes {
    ints: [Int]!
    intsNull: [Int]

    # Long description
    countLong: Long
    int: Int!
    # string decr
    stringField: String!
}
"""
            }
        }
        "TypeDslTest dropField schema" should {
            "not print drop field" {
                schemaDsl {
                    type<ListTypes> {
                        dropField("intsNull")
                    }
                } schemaEqual """
schema {
}

type ListTypes {
    ints: [Int]!
}
"""
            }
            "throw if field name not exist" {
                shouldThrow<IllegalArgumentException> {
                    schemaDsl {
                        type<ListTypes> {
                            dropField("intsNotExist")
                        }
                    }
                }
            }
        }
    }
}
