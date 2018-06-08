package localhost.sangriademo

import argonaut.Json
import localhost.sangriademo.guts.Eithers._
import sangria.ast.Document
import sangria.execution.{Executor, QueryAnalysisError}
import sangria.marshalling.argonaut._
import sangria.parser.QueryParser

import scala.io.Source
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/** Application Layer */
object Main extends App {

  import localhost.sangriademo.guts.Server._

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

  def format(msg: String): String =
    s"""{"data":null,"errors":[{"message":"$msg"}]}"""

  def handlePost(requestBody: String): Response = {

    val maybeQuery: Either[Response, String] =
      argonaut.Parse.parseOption(requestBody)
        .flatMap(json => json.field("query"))
        .flatMap(queryField => queryField.string)
        .ifFailed {
          val msg = s"Unable to parse request body: $requestBody"
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

        val future: Future[Json] = Executor.execute(
          schema = SchemaDef.schema,
          queryAst = parsedQuery,
          userContext = FalsoDB.appContext
        )

        future.ifFailed { err =>
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
}
