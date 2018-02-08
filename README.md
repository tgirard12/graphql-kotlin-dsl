# graphql-kotlin-dsl

[ ![Download](https://api.bintray.com/packages/tgirard12/kotlin/graphql-kotlin-dsl/images/download.svg) ](https://bintray.com/tgirard12/kotlin/graphql-kotlin-dsl/_latestVersion)

Kotlin DSL to generate GraphQL Schema IDL and DSL for [graphql-java](https://github.com/graphql-java/graphql-java) library

- [Kotlin DSL to GraphQL Schema (IDL)](#schema-idl)
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
  val email: String
)

enum class Right {
    read, write, execute
}

schemaDsl {
 
  // Scalar
  scalar<Double>()
  scalar<UUID>()

  // Types and Enums
  type<User>()
  enum<Right>

  // Queries
  query<User> {
    arg<UUID> { name = "id" }
  }
  query<Unit> {
    name = "users"
    returnType = "[User]"
  }

  // Mutations
  mutation<User> {
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
    user(id: UUID!): User!
    users: [User]!
}

type MutationType {
    updateUser(count: Int, name: String!): User!
}

scalar Double
scalar UUID

enum Right {
    read
    write
    execute
}

type User {
    email: String!
    name: String!
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


