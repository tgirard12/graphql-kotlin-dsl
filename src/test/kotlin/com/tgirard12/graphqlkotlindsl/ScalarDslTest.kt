package com.tgirard12.graphqlkotlindsl

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.specs.WordSpec
import org.junit.runner.RunWith
import java.time.LocalDateTime
import java.util.*


@RunWith(KTestJUnitRunner::class)
class ScalarDslTest : WordSpec() {

    init {
        "ScalarDslTest schema" should {
            "print double" {
                schemaDsl {
                    scalar<Double>()
                } schemaEqual """
schema {
}

scalar Double
"""
            }
            "print several with names and Description" {
                schemaDsl {
                    scalar<Double>()
                    scalar<UUID>(scalarDescription = "The ID")
                    scalar<LocalDateTime>()
                } schemaEqual """
schema {
}

scalar Double
scalar LocalDateTime
# The ID
scalar UUID
"""
            }
        }
    }
}