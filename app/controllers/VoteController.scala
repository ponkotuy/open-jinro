package controllers

import java.util.UUID
import javax.inject.Inject

import games.LynchingResult
import io.circe.generic.auto._
import models.{Player, Vote}
import play.api.libs.circe.Circe
import play.api.mvc.InjectedController
import queries.CreateVote
import scalikejdbc.DB

class VoteController @Inject()() extends InjectedController with Circe {
  import Responses._

  def vote(roomId: UUID) = Action(circe.tolerantJson[CreateVote]) { implicit req =>
    import LynchingResult._
    UserController.authRoom(roomId).map { case (user, room) =>
      DB localTx { implicit session =>
        Vote.create(room, user.id, req.body.dest)
        val players = Player.findAllByRoom(room.id)
        val votes = Vote.findAllByRoom(room)
        LynchingResult(room, players, votes) match {
          case x: Decided => x.apply(room, players)
          case _ =>
        }
        Success
      }
    }.merge
  }
}
