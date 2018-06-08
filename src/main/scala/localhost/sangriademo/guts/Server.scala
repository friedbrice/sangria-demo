package localhost.sangriademo
package guts

import javax.servlet.http
import org.eclipse.jetty

/** This file is some standard embedded Jetty. Safe to ignore. */
object Server {

  type Path = String
  type Method = String
  type Body = String

  type Status = Int
  type ContentType = String
  type Content = String

  type Request = (Path, Method, Body)
  type Response = (Status, ContentType, Content)

  def serve(port: Int)(routes: Request => Response): Unit = {

    val handler = new jetty.server.handler.AbstractHandler {
      def handle( target: String,
                  base: jetty.server.Request,
                  req: http.HttpServletRequest,
                  res: http.HttpServletResponse ): Unit = {

        val path = base.getOriginalURI
        val method = base.getMethod
        val input = req.getInputStream

        val body = Option(input)
          .map(scala.io.Source.fromInputStream(_).mkString)
          .getOrElse("")

        val (status, contentType, content) = routes( (path, method, body) )

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
