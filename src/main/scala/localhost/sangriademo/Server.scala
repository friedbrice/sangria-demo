package localhost.sangriademo

import javax.servlet.http
import org.eclipse.jetty

import scala.io.Source

object Server {

  type Path = String
  type Method = String
  type Body = String
  type AuthToken = String

  type Status = Int
  type ContentType = String
  type Content = String

  type Request = (Path, Method, Body, Option[AuthToken])
  type Response = (Status, ContentType, Content)

  def serve(port: Int)(routes: Request => Response): Unit = {

    val handler = new jetty.server.handler.AbstractHandler {
      def handle( target: String,
                  base: jetty.server.Request,
                  req: http.HttpServletRequest,
                  res: http.HttpServletResponse ): Unit = {

        val path = req.getRequestURI
        val method = req.getMethod
        val token = Option(req.getHeader("Authorization"))
        val body = Source.fromInputStream(req.getInputStream).mkString

        val (status, contentType, content) = routes((path, method, body, token))

        res.setStatus(status)
        res.setContentType(contentType)
        res.getWriter.print(content)

        base.setHandled(true)
      }
    }

    val server = new jetty.server.Server(port)
    server.setHandler(handler)
    server.start()
    server.join()
  }
}
