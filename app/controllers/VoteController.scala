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
    UserController.authRoom(roomId).map { auth =>
      DB localTx { implicit session =>
        Vote.create(auth.room, auth.player.id, req.body.dest)
        val players = Player.findAllByRoom(auth.room.id)
        val votes = Vote.findAllByRoom(auth.room)
        LynchingResult(auth.room, players, votes) match {
          case x: Decided => x.apply(auth.room, players)
          case _ =>
        }
        Success
      }
    }.merge
  }
}
