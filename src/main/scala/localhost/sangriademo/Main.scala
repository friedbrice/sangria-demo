package localhost.sangriademo

import argonaut.Json
import localhost.sangriademo.Eithers._
import sangria.ast.Document
import sangria.execution.{Executor, QueryAnalysisError}
import sangria.marshalling.argonaut._
import sangria.parser.QueryParser

import scala.io.Source
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

object Main extends App {

  import Server._

  val handleGet: Response =
    htmlResponse(Status.OK, Source.fromResource("graphiql.html").mkString)

  def handlePost(requestBody: String): Response = {

    val maybeQuery: Either[Response, String] = {

      val optionString: Option[String] =
        argonaut.Parse.parseOption(requestBody)
          .flatMap(_.field("query"))
          .flatMap(_.string)

      optionString.either {
        errorResponse(Status.BAD_REQUEST,
          s"Unable to parse request body: $requestBody")
      }
    }

    val maybeParsedQuery: Either[Response, Document] =
      maybeQuery.flatMap { query =>

        val tryParsedQuery: Try[Document] = QueryParser.parse(query)

        tryParsedQuery.either { case err: QueryAnalysisError =>
          jsonResponse(Status.BAD_REQUEST, err.resolveError)
        }
      }

    val maybeExecutedQuery: Either[Response, Json] =
      maybeParsedQuery.flatMap { parsedQuery =>

        val future: Future[Json] = Executor.execute(
          schema = SchemaDef.schema,
          queryAst = parsedQuery,
          userContext = FalsoDB.appContext
        )

        future.either { err =>
          errorResponse(Status.INTERNAL_ERROR, err.toString)
        }
      }

    val maybeResponse: Either[Response, Response] =
      maybeExecutedQuery.map { json =>
        jsonResponse(Status.OK, json)
      }

    maybeResponse.unify
  }

  val server: Runnable = makeServer(8080) {
    case ("/", Method.GET, _) => handleGet
    case ("/", Method.POST, body) => handlePost(body)
    case ("/", _, _) => errorResponse(Status.METHOD_NOT_ALLOWED)
    case (_, _, _) => errorResponse(Status.NOT_FOUND)
  }

  server.run()
}
