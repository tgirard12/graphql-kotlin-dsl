package com.tgirard12.graphqlkotlindsl

import graphql.schema.GraphQLScalarType

class ScalarDsl : Description {
    var name: String? = null
    override var description: String? = null

    var graphQlScalarType: GraphQLScalarType? = null
}