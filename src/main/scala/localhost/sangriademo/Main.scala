package localhost.sangriademo

import argonaut.Json
import sangria.execution.{ErrorWithResolver, Executor}
import sangria.marshalling.argonaut._
import sangria.parser.{QueryParser, SyntaxError}
import sangria.schema.Schema

import scala.io.Source
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.TimeoutException
import scala.concurrent.duration._

object Main extends App {

  import Server._

  val graphiql: String = Source.fromResource("srv/graphiql.html").mkString

  val graphqlDocs: String = Source.fromResource("srv/graphql-docs.html").mkString

  val specSchema: Schema[_, _] =
    Schema.buildFromAst(QueryParser.parse(
      Source.fromResource("schema.graphql").mkString
    ).get)

  val implSchema: Schema[Context, Unit] = SchemaDef.schema(specSchema)

  def format(msg: String): String = Json(
    "data" -> Json.jNull,
    "errors" -> Json.array(Json("message" -> Json.jString(msg)))
  ).toString

  def handlePost( postBody: String,
                  authToken: Option[String]
                ): Either[Response, Response] = for {

    query <- argonaut.Parse.parseOption(postBody)
      .flatMap(_.field("query")).flatMap(_.string)
      .`catch` {
        val msg = s"Unable to parse request body: $postBody"
        (400, "application/json", format(msg))
      }

    parsedQuery <- QueryParser.parse(query).`catch` {

      case err: SyntaxError =>
        (400, "application/json", format(err.getMessage))

      case err =>
        (500, "application/json", format(err.getMessage))
    }

    executedQuery <- Executor.execute(
      queryAst    = parsedQuery,
      schema      = implSchema,
      userContext = FalsoDB.context(authToken)
    ).`catch`(await = 1.minute) {

      case err: ErrorWithResolver =>
        (400, "application/json", err.resolveError.toString)

      case err: AuthError =>
        (401, "application/json", format(err.getMessage))

      case _: TimeoutException =>
        val msg = "Computational limit reached, please refine your query."
        (403, "application/json", format(msg))

      case err =>
        (500, "application/json", format(err.getMessage))
    }
  } yield (200, "application/json", executedQuery.toString)

  assert(
    assertion = implSchema.compare(specSchema).isEmpty,
    message = "Implemented schema must match specified schema"
  )

  serve(port = 8080) {

    case ("/", "GET", _, _) =>
      (200, "text/html", graphiql)

    case ("/", "POST", body, token) =>
      handlePost(body, token).converge

    case ("/", _, _, _) =>
      (405, "text/plain", "METHOD NOT ALLOWED")

    case ("/docs", "GET", _, _) =>
      (200, "text/html", graphqlDocs)

    case ("/docs", _, _, _) =>
      (405, "text/plain", "METHOD NOT ALLOWED")

    case ("/schema", "GET", _, _) =>
      (200, "text/plain", implSchema.renderPretty)

    case ("/schema", _, _, _) =>
      (405, "text/plain", "METHOD NOT ALLOWED")

    case (_, _, _, _) =>
      (404, "text/plain", "NOT FOUND")
  }
}
