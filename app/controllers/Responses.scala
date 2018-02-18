package controllers

import play.api.mvc.Results._

object Responses {
  val Success = Ok("Success")
  def notFound(str: String) = NotFound(s"Not found ${str}")
  val BadReq = BadRequest(s"Bad request")
}
