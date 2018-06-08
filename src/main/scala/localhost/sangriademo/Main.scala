package localhost.sangriademo

import argonaut.Json
import localhost.sangriademo.guts.Eithers._
import localhost.sangriademo.guts.FalsoDB
import sangria.ast.Document
import sangria.execution.{Executor, ValidationError}
import sangria.marshalling.argonaut._
import sangria.parser.{QueryParser, SyntaxError}

import scala.io.Source
import scala.concurrent.ExecutionContext.Implicits.global

object Main extends App {

  import localhost.sangriademo.guts.Server._

  def format(msg: String): String =
    s"""{"data":null,"errors":[{"message":"${msg.filterNot(_.isControl)}"}]}"""

  def handlePost(postBody: String): Response = {

    val maybeQuery: Either[Response, String] =
      argonaut.Parse.parseOption(postBody)
        .flatMap(json => json.field("query"))
        .flatMap(queryField => queryField.string)
        .ifFailed {
          val msg = s"Unable to parse request body: $postBody"
          (401, "application/json", format(msg))
        }

    val maybeParsedQuery: Either[Response, Document] =
      maybeQuery.flatMap { query =>
        QueryParser.parse(query) // From Sangria. Parses a GraphQL query.
          .ifFailed {

            case err: SyntaxError =>
              (401, "application/json", format(err.getMessage))

            case err =>
              (500, "application/json", format(err.getMessage))
          }
      }

    val maybeExecutedQuery: Either[Response, Json] =
      maybeParsedQuery.flatMap { parsedQuery =>
        Executor.execute( // From Sangria.
          queryAst    = parsedQuery,       // Accepts the query.
          schema      = SchemaDef.schema,  // Validates it against the Schema.
          userContext = FalsoDB.appContext // Executes it using our AppContext.
        ).ifFailed {

          case err: ValidationError =>
            (401, "application/json", err.resolveError.nospaces)

          case err =>
            (500, "application/json", format(err.getMessage))
        }
      }

    val finalResponse: Either[Response, Response] =
      maybeExecutedQuery.map { json =>
        (200, "application/json", json.nospaces)
      }

    finalResponse.converge
  }

  serve(8080) {

    case ("/", "GET", _) =>
      (200, "text/html", Source.fromResource("graphiql.html").mkString)

    case ("/", "POST", body) =>
      handlePost(body)

    case ("/", _, _) =>
      (405, "text/plain", "METHOD NOT ALLOWED")

    case (_, _, _) =>
      (404, "text/plain", "NOT FOUND")
  }
}
