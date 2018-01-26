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
$TAB
$TAB
}

scalar Double"""
            }
            "print several with names" {
                schemaDsl {
                    scalar<Double>()
                    scalar<UUID>()
                    scalar<LocalDateTime>()
                } schemaEqual """
schema {
$TAB
$TAB
}

scalar Double
scalar LocalDateTime
scalar UUID"""
            }
        }
    }
}