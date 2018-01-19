package com.tgirard12.graphqlkotlindsl

import org.junit.Assert

infix fun String.stringEqual(s: String) = Assert.assertEquals(s, this)
infix fun SchemaDsl.schemaEqual(s: String) = Assert.assertEquals(s, this.schemaString())

const val TAB = "    "