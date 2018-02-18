package controllers

import java.util.UUID
import javax.inject.Inject

import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.java8.time._
import models.{Room, User}
import play.api.libs.circe.Circe
import play.api.mvc.{InjectedController, Request, Result}
import scalikejdbc.AutoSession

class UserController @Inject()() extends InjectedController with Circe {
  import Responses._
  def show(userId: UUID) = Action {
    User.findById(userId).fold(notFound(s"user id=${userId}")) { user => Ok(user.asJson) }
  }
}

object UserController {
  import Responses._

  val UserCookie = "user-id"
  def getUser[T]()(implicit req: Request[T]): Either[Result, User] = {
    req.cookies.get(UserCookie).map(_.value).flatMap { userId =>
      User.findById(UUID.fromString(userId))(AutoSession)
    }.toRight(notFound("user"))
  }

  def getOrSetUser[T]()(implicit req: Request[T]): User = {
    getUser().getOrElse(createUser())
  }

  def createUser[T]()(implicit req: Request[T]): User = {
    val id = User.create("")(AutoSession)
    User.findById(id)(AutoSession).get
  }

  def authRoom[T](roomId: UUID)(implicit req: Request[T]): Either[Result, (User, Room)] = {
    for {
      user <- getUser()
      room <- Room.findById(roomId).toRight(notFound(s"room: id=${roomId}"))
      // Array(bearer, token) <- req.headers.get("Authorization").toRight(BadReq).map(_.split(" "))
      xs <- req.headers.get("Authorization").toRight(BadReq).map(_.split(" "))
      Array(bearer, token) = xs
      _ <- Either.cond(bearer == "Bearer" && room.matches(token), Nil, BadReq)
    } yield (user, room)
  }

  def authRoomOwner[T](roomId: UUID)(implicit req: Request[T]): Either[Result, (User, Room)] = {
    for {
      user <- getUser()
      room <- Room.findById(roomId).toRight(notFound(s"room: id=${roomId}"))
      _ <- Either.cond(room.owner == user.id, Nil, BadReq)
    } yield (user, room)
  }
}
