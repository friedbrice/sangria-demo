package localhost.sangriademo

import java.io.InputStream

import fi.iki.elonen.NanoHTTPD
import sangria.schema.Schema

object Main extends App {

  def handleError(status: NanoHTTPD.Response.Status): NanoHTTPD.Response =
    NanoHTTPD.newFixedLengthResponse(
      status,
      "application/json",
      s"""{"data":null,"errors":[{"message":"$status"}]}"""
    )

  val handleGet: NanoHTTPD.Response =
    NanoHTTPD.newFixedLengthResponse(
      NanoHTTPD.Response.Status.OK,
      "text/html",
      scala.io.Source.fromResource("graphiql.html").mkString
    )

  def handlePost(schema: Schema[AppContext, Unit],
                 appContext: AppContext,
                 body: InputStream): NanoHTTPD.Response = {

//    ???
//
//    NanoHTTPD.newFixedLengthResponse(
//      /* status */ ???,
//      /* mimeType */ ???,
//      /* txt */ ???
//    )

    handleError(NanoHTTPD.Response.Status.INTERNAL_ERROR)
  }

  val schema: Schema[AppContext, Unit] = SchemaDef.schema

  val appContext: AppContext = FalsoDB.appContext

  val server: NanoHTTPD = new NanoHTTPD(8080) {

    override def serve(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response =
      session.getUri match {

        case "/" => session.getMethod match {

          case NanoHTTPD.Method.GET =>
            handleGet

          case NanoHTTPD.Method.POST =>
            handlePost(schema, appContext, session.getInputStream)

          case _ =>
            handleError(NanoHTTPD.Response.Status.METHOD_NOT_ALLOWED)
        }

        case _ =>
          handleError(NanoHTTPD.Response.Status.NOT_FOUND)
      }
  }

  server.start(Int.MaxValue, false)
}
