package com.tgirard12.graphqlkotlindsl

import io.kotlintest.KTestJUnitRunner
import io.kotlintest.specs.WordSpec
import org.junit.runner.RunWith

@RunWith(KTestJUnitRunner::class)
class SchemaDslTest : WordSpec() {

    init {
        "SchemaDslTest header" should {
            "with empty schema" {
                schemaDsl { } schemaEqual """
schema {
}
"""
            }
        }
    }
}