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
$TAB
$TAB
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
}"""
            }
            "print List types" {
                schemaDsl {
                    type<ListTypes> { }
                } schemaEqual """
schema {
$TAB
$TAB
}


type ListTypes {
    ints: [Int]!
    intsNull: [Int]
}"""
            }
        }
    }
}