---
title: GraphQL and Sangria
subtitle: How to get a GraphQL API Server Up and Running
date: 5 March 2018
author:
  - Daniel Brice, CJ Affiliate (dbrice@cj.com)
  - David Ron, Pay Junction
institute:
  - CJ Affiliate, Santa Barbara, California
  - Pay Junction, Santa Barbara, California
theme: default
colortheme: beaver
classoption:
  - aspectratio=169
  - 10pt
fonttheme: serif
fontthemeoptions:
  - onlymath
---

# Designing Your GraphQL Schema

## Designing Your GraphQL Schema (1)

:::::: {.columns}
::: {.column width="50%"}

### Database Layout

```
TABLE foo
    INT    id,
    STRING name,
    INT    bar.id

TABLE bar
    INT    id,
    STRING name
```

:::
::: {.column width="50%"}

### GraphQL Schema

```graphql
type Foo {
    id:    Int!
    name:  String!
    barId: Int!
}
type Bar {
    id:   Int!
    name: String!
}
type Query {
    foos: [Foo!]!
    bars: [Bar!]!
}
```

:::
::::::

## Designing Your GraphQL Schema (2)

:::::: {.columns}
::: {.column width="50%"}

### Database Layout

```
TABLE foo
    INT    id,
    STRING name,
    INT    bar.id

TABLE bar
    INT    id,
    STRING name
```

:::
::: {.column width="50%"}

### GraphQL Schema

```graphql
type Foo {
    id:   Int!
    name: String!
    bar:  Bar!
}
type Bar {
    id:   Int!
    name: String!
}
type Query {
    foos: [Foo!]!
    bars: [Bar!]!
}
```

:::
::::::

## Designing Your GraphQL Schema (3)

:::::: {.columns}
::: {.column width="50%"}

### Database Layout

```
TABLE foo
    INT    id,
    STRING name,
    INT    bar.id

TABLE bar
    INT    id,
    STRING name
```

:::
::: {.column width="50%"}

### GraphQL Schema

```graphql
type Foo {
    id:   Int!
    name: String!
    bar:  Bar!
}
type Bar {
    id:   Int!
    name: String!
    foos: [Foo!]!
}
type Query {
    foos: [Foo!]!
    bars: [Bar!]!
}
```

:::
::::::

## Designing Your GraphQL Schema (4)

:::::: {.columns}
::: {.column width="50%"}

### Database Layout

```
TABLE foo
    INT    id,
    STRING name,
    INT    bar.id

TABLE bar
    INT    id,
    STRING name
```

:::
::: {.column width="50%"}

### GraphQL Schema

```graphql
type Foo {
    id:   Int!
    name: String!
    bar:  Bar!
}
type Bar {
    id:   Int!
    name: String!
    foos: [Foo!]!
}
type Query {
    foos(ids: [Int!]): [Foo!]!
    bars(ids: [Int!]): [Bar!]!
}
```

:::
::::::

# Executing GraphQL Queries

## Executing GraphQL Queries

```scala
// Global constant.
val schema: Schema[DAO, Unit] = ...

// Create in response to incoming request.
val : DAO = ...

// Contained in POST body of incoming request.
val unparsedQuery: String = ...

// May contain a SyntaxError
val parsedQuery: Try[Document] = QueryParser.parse(unparsedQuery)

// May contain a ValidationError
val futureResult: Future[Json] = Executor.execute(
  queryAst    = parsedQuery.get,
  userContext = dao,
  schema      = schema
)
```

# Defining Your Data Layer

## Defining Your Data Layer

:::::: {.columns}
::: {.column width="50%"}

### Database Layout

```
TABLE foo
    INT    id,
    STRING name,
    INT    bar.id

TABLE bar
    INT    id,
    STRING name
```

:::
::: {.column width="50%"}

### Data Layer

```scala
case class Foo( id:    Int,
                name:  String,
                barId: Int     )

case class Bar( id:   Int,
                name: String )

trait DAO {}
```

:::
::::::

# Implementing Your Schema

## Implementing Your Schema (1)

:::::: {.columns}
::: {.column width="50%"}

### Data Layer

```scala
case class Foo( id:    Int,
                name:  String,
                barId: Int     )

trait DAO {}
```

### GraphQL Schema

```graphql
type Foo {
    id:   Int!
    name: String!
    bar:  Bar!
}
```

:::
::: {.column width="50%"}

### Sangria Schema Implementation

```scala
lazy val foo: GqlObject[DAO, Foo] = ???
```

:::
::::::

## Implementing Your Schema (2)

:::::: {.columns}
::: {.column width="50%"}

### Data Layer

```scala
case class Foo( id:    Int,
                name:  String,
                barId: Int     )

trait DAO {}
```

### GraphQL Schema

```graphql
type Foo {
    id:   Int!
    name: String!
    bar:  Bar!
}
```

:::
::: {.column width="50%"}

### Sangria Schema Implementation

```scala
lazy val foo: GqlObject[DAO, Foo] =
  deriveObjectType[DAO, Foo]()
```

:::
::::::

## Implementing Your Schema (3)

:::::: {.columns}
::: {.column width="50%"}

### Data Layer

```scala
case class Foo( id:    Int,
                name:  String,
                barId: Int     )

trait DAO {}
```

### GraphQL Schema

```graphql
type Foo {
    id:   Int!
    name: String!
    bar:  Bar!
}
```

:::
::: {.column width="50%"}

### Sangria Schema Implementation

```scala
lazy val foo: GqlObject[DAO, Foo] =
  deriveObjectType[DAO, Foo](
    ReplaceField(
      fieldName = "barId",
      field     = ???
    )
  )
```

:::
::::::

## Implementing Your Schema (4)

:::::: {.columns}
::: {.column width="50%"}

### Data Layer

```scala
case class Foo( id:    Int,
                name:  String,
                barId: Int     )

trait DAO {}
```

### GraphQL Schema

```graphql
type Foo {
    id:   Int!
    name: String!
    bar:  Bar!
}
```

:::
::: {.column width="50%"}

### Sangria Schema Implementation

```scala
lazy val foo: GqlObject[DAO, Foo] =
  deriveObjectType[DAO, Foo](
    ReplaceField(
      fieldName = "barId",
      field     = GqlField(
        name      = "bar",
        fieldType = bar,
        resolve   = cc => ???
      )
    )
  )
```

:::
::::::

## Implementing Your Schema (5)

:::::: {.columns}
::: {.column width="50%"}

### Data Layer

```scala
case class Foo( id:    Int,
                name:  String,
                barId: Int     )

trait DAO {
  def fooBar(foo: Foo): Bar
}
```

### GraphQL Schema

```graphql
type Foo {
    id:   Int!
    name: String!
    bar:  Bar!
}
```

:::
::: {.column width="50%"}

### Sangria Schema Implementation

```scala
lazy val foo: GqlObject[DAO, Foo] =
  deriveObjectType[DAO, Foo](
    ReplaceField(
      fieldName = "barId",
      field     = GqlField(
        name      = "bar",
        fieldType = bar,
        resolve   = cc =>
          cc.ctx.fooBar(cc.value)
      )
    )
  )
```

:::
::::::

## Implementing Your Schema (6)

:::::: {.columns}
::: {.column width="50%"}

### Data Layer

```scala
case class Bar( id:   Int,
                name: String )

trait DAO {
  def fooBar(foo: Foo): Bar
  def barFoos(bar: Bar): Foo
}
```

### GraphQL Schema

```graphql
type Bar {
    id:   Int!
    name: String!
    foos: [Foo!]!
}
```

:::
::: {.column width="50%"}

### Sangria Schema Implementation

```scala
lazy val bar: GqlObject[DAO, Bar] =
  deriveObjectType[DAO, Bar](
    AddFields(
      GqlField(
        name =      "foos",
        fieldType = GqlList(foo),
        resolve =   cc =>
          cc.ctx.barFoos(cc.value)
      )
    )
  )
```

:::
::::::

## Implementing Your Schema (7)

:::::: {.columns}
::: {.column width="50%"}

### Data Layer

```scala
trait DAO {
  ...
  def queryFoos()
  def queryBars()
}
```

### GraphQL Schema

```graphql
type Query {
  foos(ids: [Int!]): [Foo!]!
  bars(ids: [Int!]): [Bar!]!
}
```

:::
::: {.column width="50%"}

### Sangria Schema Implementation

```scala
lazy val query: GqlObject[DAO, Unit] = ???
```

:::
::::::

## Implementing Your Schema (8)

:::::: {.columns}
::: {.column width="50%"}

### Data Layer

```scala
trait DAO {
  ...
  def queryFoos(
    ids: Option[Seq[Int]]): Seq[Foo]

  def queryBars(
    ids: Option[Seq[Int]]): Seq[Bar]
}
```

### GraphQL Schema

```graphql
type Query {
  foos(ids: [Int!]): [Foo!]!
  bars(ids: [Int!]): [Bar!]!
}
```

:::
::: {.column width="50%"}

### Sangria Schema Implementation

```scala
lazy val query: GqlObject[DAO, Unit] = ???
```

:::
::::::

## Implementing Your Schema (9)

:::::: {.columns}
::: {.column width="50%"}

### Data Layer

```scala
trait DAO {
  ...
  def queryFoos(
    ids: Option[Seq[Int]]): Seq[Foo]

  def queryBars(
    ids: Option[Seq[Int]]): Seq[Bar]
}
```

### GraphQL Schema

```graphql
type Query {
  foos(ids: [Int!]): [Foo!]!
  bars(ids: [Int!]): [Bar!]!
}
```

:::
::: {.column width="50%"}

### Sangria Schema Implementation

```scala
lazy val ids:
  GqlArgument[Option[Seq[Int]]] =
    GqlArgument(
      name         = "ids",
      argumentType =
        GqlOptionInput(GqlListInput(GqlInt))
    )

lazy val query: GqlObject[DAO, Unit] = ???
```

:::
::::::

## Implementing Your Schema (10)

:::::: {.columns}
::: {.column width="50%"}

### Data Layer

```scala
trait DAO {
  ...
  def queryFoos(
    ids: Option[Seq[Int]]): Seq[Foo]

  def queryBars(
    ids: Option[Seq[Int]]): Seq[Bar]
}
```

### GraphQL Schema

```graphql
type Query {
  foos(ids: [Int!]): [Foo!]!
  bars(ids: [Int!]): [Bar!]!
}
```

:::
::: {.column width="50%"}

### Sangria Schema Implementation

```scala
lazy val ids = ...

lazy val query: GqlObject[DAO, Unit] =
  GqlObject(
    name   = "Query",
    fields = gqlFields[DAO, Unit](
      ???, // foos field
      ???  // bars field
    )
  )
```

:::
::::::

## Implementing Your Schema (11)

:::::: {.columns}
::: {.column width="50%"}

### Data Layer

```scala
trait DAO {
  ...
  def queryFoos(
    ids: Option[Seq[Int]]): Seq[Foo]

  def queryBars(
    ids: Option[Seq[Int]]): Seq[Bar]
}
```

### GraphQL Schema

```graphql
type Query {
  foos(ids: [Int!]): [Foo!]!
  bars(ids: [Int!]): [Bar!]!
}
```

:::
::: {.column width="50%"}

### Sangria Schema Implementation

```scala
lazy val query: GqlObject[DAO, Unit] =
  GqlObject(
    name   = "Query",
    fields = gqlFields[DAO, Unit](
      GqlField(
        name      = "foos",
        fieldType = GqlList(foo),
        arguments = List(ids),
        resolve   = cc => ???
      ),
      GqlField(
        name      = "bars",
        fieldType = GqlList(bar),
        arguments = List(ids),
        resolve   = cc => ???
      )
    )
  )
```

:::
::::::

## Implementing Your Schema (12)

:::::: {.columns}
::: {.column width="50%"}

### Data Layer

```scala
trait DAO {
  ...
  def queryFoos(
    ids: Option[Seq[Int]]): Seq[Foo]

  def queryBars(
    ids: Option[Seq[Int]]): Seq[Bar]
}
```

### GraphQL Schema

```graphql
type Query {
  foos(ids: [Int!]): [Foo!]!
  bars(ids: [Int!]): [Bar!]!
}
```

:::
::: {.column width="50%"}

### Sangria Schema Implementation

```scala
lazy val query: GqlObject[DAO, Unit] =
  GqlObject(
    name   = "Query",
    fields = gqlFields[DAO, Unit](
      GqlField(
        ...
        resolve   = cc =>
          cc.ctx.queryFoos(cc.arg(ids))
      ),
      GqlField(
        ...
        resolve   = cc =>
          cc.ctx.queryBars(cc.arg(ids))
      )
    )
  )
```

:::
::::::
