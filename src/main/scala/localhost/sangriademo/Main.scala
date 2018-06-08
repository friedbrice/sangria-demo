package localhost.sangriademo

import argonaut.Json
import localhost.sangriademo.guts.FalsoDB
import sangria.ast.Document
import sangria.execution.{Executor, ValidationError}
import sangria.marshalling.argonaut._
import sangria.parser.{QueryParser, SyntaxError}

import scala.io.Source
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object Main extends App {

  import localhost.sangriademo.guts.Server._

  def format(msg: String): String =
    s"""{"data":null,"errors":[{"message":"${msg.filterNot(_.isControl)}"}]}"""

  case class Err(response: Response) extends Exception(response._3)

  def handlePost(postBody: String): Response = {
    try {

      // Grab the GraphQL query out of the request body.
      val query: String = {
        try {
          argonaut.Parse.parseOption(postBody)
            .flatMap { json => json.field("query") }
            .flatMap { queryField => queryField.string }
            .get
        }
        catch {
          case _: Throwable =>
            val msg = s"Unable to parse request body: $postBody"
            throw Err( (401, "application/json", format(msg)) )
        }
      }

      // Make sure the GraphQL query is syntactically valid.
      val parsedQuery: Document = {
        try {
          QueryParser.parse(query).get // From Sangria. Parses query.
        }
        catch {
          case err: SyntaxError =>
            throw Err( (401, "application/json", format(err.getMessage)) )
          case err: Throwable =>
            throw Err( (500, "application/json", format(err.getMessage)) )
        }
      }

      // Make sure the GraphQL query fits our schema and execute it.
      val executedQuery: Json = {
        try {
          val futureJson: Future[Json] =
            Executor.execute( // From Sangria.
              queryAst    = parsedQuery,       // Accepts the query.
              schema      = SchemaDef.schema,  // Validates against the Schema.
              userContext = FalsoDB.appContext // Executes using our AppContext.
            )
          Await.result(futureJson, Duration.Inf)
        }
        catch {
          case err: ValidationError =>
            throw Err( (401, "application/json", err.resolveError.toString) )
          case err: Throwable =>
            throw Err( (500, "application/json", format(err.getMessage)) )
        }
      }

      (200, "application/json", executedQuery.toString)
    }
    catch {
      case Err(response) => response
    }
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
