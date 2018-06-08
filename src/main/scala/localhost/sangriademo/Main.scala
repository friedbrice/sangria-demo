package localhost.sangriademo

import argonaut.Json
import localhost.sangriademo.guts.Eithers._
import localhost.sangriademo.guts.FalsoDB
import sangria.ast.Document
import sangria.execution.{Executor, QueryAnalysisError}
import sangria.marshalling.argonaut._
import sangria.parser.QueryParser

import scala.io.Source
import scala.concurrent.ExecutionContext.Implicits.global

object Main extends App {

  import localhost.sangriademo.guts.Server._

  def format(msg: String): String =
    s"""{"data":null,"errors":[{"message":"$msg"}]}"""

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
        QueryParser.parse(query)
          .ifFailed { case err: QueryAnalysisError =>
            (401, "application/json", err.resolveError.nospaces)
          }
      }

    val maybeExecutedQuery: Either[Response, Json] =
      maybeParsedQuery.flatMap { parsedQuery =>
        Executor.execute(
          schema = SchemaDef.schema,
          queryAst = parsedQuery,
          userContext = FalsoDB.appContext
        ).ifFailed { err =>
          (500, "application/json", format(err.toString))
        }
      }

    val maybeResponse: Either[Response, Response] =
      maybeExecutedQuery.map { json =>
        (200, "application/json", json.nospaces)
      }

    val response: Response = maybeResponse.converge

    response
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
