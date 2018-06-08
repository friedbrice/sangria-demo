package localhost.sangriademo

import argonaut.Json

object Server {

  type Response
  type Status
  type Method

  object Status {
    val OK: Status = ???
    val BAD_REQUEST: Status = ???
    val INTERNAL_ERROR: Status = ???
    val METHOD_NOT_ALLOWED: Status = ???
    val NOT_FOUND: Status = ???
  }

  object Method {
    val GET: Method = ???
    val POST: Method = ???
  }

  def htmlResponse(status: Status, body: String): Response = ???

  def jsonResponse(status: Status, json: Json): Response = ???

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

  def makeServer(port: Int)(f: (String, Method, String) => Response): Runnable = ???
}
