package localhost.sangriademo

import java.io.InputStream

import fi.iki.elonen.NanoHTTPD

object Main extends App {

/*
NanoHTTPD.newFixedLengthResponse(
  /* status */ ???,
  /* mimeType */ ???,
  /* txt */ ???
)
*/

  val handleNotFound: NanoHTTPD.Response =
    NanoHTTPD.newFixedLengthResponse(
      /* status */ NanoHTTPD.Response.Status.NOT_FOUND,
      /* mimeType */ ???,
      /* txt */ ???
    )

  val handleNotAllowed: NanoHTTPD.Response =
    NanoHTTPD.newFixedLengthResponse(
      /* status */ NanoHTTPD.Response.Status.METHOD_NOT_ALLOWED,
      /* mimeType */ ???,
      /* txt */ ???
    )

  val handleGet: NanoHTTPD.Response =
    NanoHTTPD.newFixedLengthResponse(
      /*status = */ NanoHTTPD.Response.Status.OK,
      /*mimeType = */ NanoHTTPD.MIME_HTML,
      /*txt = */ scala.io.Source.fromResource("graphiql.html").mkString
    )

  def handlePost(body: InputStream): NanoHTTPD.Response = ???

  val server: NanoHTTPD = new NanoHTTPD(8080) {

    override def serve(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response =
      session.getUri match {

        case "/" => session.getMethod match {
          case NanoHTTPD.Method.GET => handleGet
          case NanoHTTPD.Method.POST => handlePost(session.getInputStream)
          case _ => handleNotAllowed
        }

        case _ => handleNotFound
      }
  }

  server.start(Int.MaxValue, false)
}
