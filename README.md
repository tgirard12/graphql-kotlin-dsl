# graphql-kotlin-dsl

[ ![Download](https://api.bintray.com/packages/tgirard12/kotlin/graphql-kotlin-dsl/images/download.svg) ](https://bintray.com/tgirard12/kotlin/graphql-kotlin-dsl/_latestVersion)

Kotlin DSL to generate GraphQL Schema IDL and DSL for [graphql-java](https://github.com/graphql-java/graphql-java) library

- [Kotlin DSL to GraphQL Schema (IDL)](#schema-idl)
  * Add/remove field on any kotlin Class
  * Add description on query, mutation, scalar, type, field and enum
- Extensions for graphql-java library
  * [GraphQL and RuntimeWiring](#graphql-and-runtimewiring-dsl)
  * [GraphQLSchema](#graphqlschema-instance)
  * [Scalar DSL](#scalar-dsl)

## Download

```gradle
compile 'com.tgirard12:graphql-kotlin-dsl:VERSION'
```

## Schema (IDL)

[Show SchemaDsl sample](src/test/kotlin/com/tgirard12/graphqlkotlindsl/SchemaDslTest.kt#L25)

```kotlin
data class User(
    val name: String, 
    val email: String,
    val deleteField: Int
)

enum class Right {
    read, write, execute
}

schemaDsl {

    // Scalar
    scalar<Double>()
    scalar<UUID>()

    // Types and Enums
    type<User>(typeDescription = "An User") {
        desc("email", "User Email")

        addField<String>(name = "otherName") { }
        addField<Right> {
            description = "User Right"
            nullable = true
        }

        dropField("deleteField")
    }
    enum<Right>(enumDescription = "An enum") { }

    // Queries
    query<User>(queryDescription = "User By Id") {
        arg<UUID> { name = "id" }
    }
    query<Unit> {
        name = "users"
        description = "All Users"
        returnType = "[User]"
    }

    // Mutations
    mutation<User>(mutationDescription = "Update a user") {
        name = "updateUser"

        arg<Int> {
            name = "count"
            nullable = true
        }
        arg<String> { name = "name" }
    }
}
```

Generate this GraphQl schema :

```
schema {
    query: QueryType
    mutation: MutationType
}

type QueryType {
    # User By Id
    user(id: UUID!): User!
    # All Users
    users: [User]!
}

type MutationType {
    # Update a user
    updateUser(count: Int, name: String!): User!
}

scalar Double
scalar UUID

# An enum
enum Right {
    read
    write
    execute
}

# An User
type User {
    # User Email
    email: String!
    id: UUID!
    name: String!

    otherName: String!
    # User Right
    right: Right
}
```


## graphql-java Extension functions and DSL

[Show graphQL sample](src/test/kotlin/com/tgirard12/graphqlkotlindsl/execution/ExecutionTest.kt#L39)

### GraphQL and RuntimeWiring DSL

```kotlin
schemaDsl { 
  ...
}.graphQL({ // Define RuntimeWiring.Builder

  scalarUUID()
  scalarDouble()

  queryType {
    staticDataFetcher<List<User>>("users") { users }
    asyncDataFetcher<User>("user") { e ->
      e.arguments["id"]?.let { id ->
        users.firstOrNull {
          id == it.id
        }
      }
    }
  }
  mutationType {
    asyncDataFetcher("updateUser") { e ->
      User(id = UUID.fromString("773b29ba-6b2b-49fe-8cb1-36134689c458"),
        name = e.arguments["name"] as String? ?: "",
        email = e.arguments["email"] as String)
    }
  }
  type<SimpleTypes> {
    asyncDataFetcher<User>("user") { users[0] } // Custom fetcher for SimpleTypes.user
  }
}, { GraphQL.Builder
  queryExecutionStrategy(AsyncExecutionStrategy())
})
```

### GraphQLSchema instance

```kotlin
schemaDsl { 
  ...
}.graphQLSchema(myRuntimeWiring)
```

### Scalar DSL

```kotlin
val scalar = scalarTypeDsl<LocalDateTime> {
  serialize {
    // ...
  }
  parseValue {
    // ...
  }
  parseLiteral {
    // ...
  }
}

```
[Show scalars sample](src/main/kotlin/com/tgirard12/graphqlkotlindsl/graphqljava/GqlJavaScalars.kt#L12)

Custom scalars : `GqlJavaScalars.uuid`, `GqlJavaScalars.double`


