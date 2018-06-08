package localhost.sangriademo

import java.io.InputStream

import argonaut.Json
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoHTTPD.{IHTTPSession, Method, Response}
import fi.iki.elonen.NanoHTTPD.Response.Status

import scala.io.Source

object Main extends App {

  def htmlResponse(status: Status, body: String): Response =
    NanoHTTPD.newFixedLengthResponse(status, "text/html", body)

  def jsonResponse(status: Status, json: Json): Response =
    NanoHTTPD.newFixedLengthResponse(status, "application/json", json.toString)

  def errorResponse(status: Status, msg: String = null): Response = {
    def body = Json.obj(
      "data" -> Json.jNull,
      "errors" -> Json.array(
        Json.obj(
          "message" -> Json.jString(Option(msg).getOrElse(status.toString))
        )
      )
    )

    jsonResponse(status, body)
  }

  val handleGet: Response =
    htmlResponse(Status.OK, Source.fromResource("graphiql.html").mkString)

  def handlePost(input: InputStream): Response = ???

  val server: NanoHTTPD = new NanoHTTPD(8080) {

    override def serve(session: IHTTPSession): Response =
      session.getUri match {

        case "/" => session.getMethod match {
          case Method.GET => handleGet
          case Method.POST => handlePost(session.getInputStream)
          case _ => errorResponse(Status.METHOD_NOT_ALLOWED)
        }

        case _ => errorResponse(Status.NOT_FOUND)
      }
  }

  server.start(Int.MaxValue, false)
}
