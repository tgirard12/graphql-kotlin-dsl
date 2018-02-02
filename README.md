# graphql-kotlin-dsl

[ ![Download](https://api.bintray.com/packages/tgirard12/kotlin/graphql-kotlin-dsl/images/download.svg) ](https://bintray.com/tgirard12/kotlin/graphql-kotlin-dsl/_latestVersion)

Kotlin DSL to generate GraphQL Schema

- Use a DSL to easily generate your GraphQL Schema (IDL)
- Generate a GraphQLSchema with the graphql-java library
- Kotlin DSL to define custom scalars

## Download

```
compile 'com.tgirard12:graphql-kotlin-dsl:VERSION'
```

## GraphQLSchema

```kotlin
val schema: GraphQLSchema = schemaDsl {

  // ... schema definition

}.graphQLSchema(myRuntimeWiring)
```

## Scalar DSL

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

## Schema (IDL)

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
