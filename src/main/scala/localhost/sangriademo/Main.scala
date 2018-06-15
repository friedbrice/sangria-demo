package localhost.sangriademo

import argonaut.Json
import localhost.sangriademo.guts.Eithers._
import localhost.sangriademo.guts.FalsoDB
import sangria.execution.{Executor, ValidationError}
import sangria.marshalling.argonaut._
import sangria.parser.{QueryParser, SyntaxError}

import scala.io.Source
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.TimeoutException
import scala.concurrent.duration._

object Main extends App {

  import localhost.sangriademo.guts.Server._

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
        (401, "application/json", format(msg))
      }

    parsedQuery <- QueryParser.parse(query).`catch` {
      case err: SyntaxError =>
        (401, "application/json", format(err.getMessage))
      case err =>
        (500, "application/json", format(err.getMessage))
    }

    executedQuery <- Executor.execute(
      queryAst    = parsedQuery,
      schema      = SchemaDef.schema,
      userContext = FalsoDB.context(authToken)
    ).`catch`(await = 1.minute) {
      case err: ValidationError =>
        (401, "application/json", err.resolveError.toString)
      case err: AuthError =>
        (403, "application/json", format(err.getMessage))
      case _: TimeoutException =>
        val msg = "Computational limit reached, please refine your query."
        (403, "application/json", format(msg))
      case err =>
        (500, "application/json", format(err.getMessage))
    }
  } yield (200, "application/json", executedQuery.toString)

  val graphIql: String = Source.fromResource("srv/graphiql.html").mkString

  serve(port = 8080) {
    case ("/", "GET", _, _)         => (200, "text/html", graphIql)
    case ("/", "POST", body, token) => handlePost(body, token).converge
    case ("/", _, _, _)             => (405, "text/plain", "METHOD NOT ALLOWED")
    case (_, _, _, _)               => (404, "text/plain", "NOT FOUND")
  }
}
