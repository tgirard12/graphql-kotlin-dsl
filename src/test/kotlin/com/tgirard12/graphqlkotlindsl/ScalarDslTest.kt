package com.tgirard12.graphqlkotlindsl

import com.tgirard12.graphqlkotlindsl.graphqljava.GqlJavaScalars
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

        "ScalarDslTest dataFetcher" should {
            "test datafetcher" {
                schemaDsl {
                    scalar<Double> {
                        GqlJavaScalars.double
                    }
                }.scalars
                        .first { it.name == "Double" }
                        .graphQlScalarType
                        .let {
                            (it != null) shouldEqual true
                        }
            }
        }
    }
}