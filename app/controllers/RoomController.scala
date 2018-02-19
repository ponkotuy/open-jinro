package controllers

import java.util.UUID
import java.util.concurrent.ThreadLocalRandom
import javax.inject.{Inject, Singleton}

import io.circe.generic.auto._
import models.{Join, Player, Role, Room}
import play.api.libs.circe.Circe
import play.api.mvc.InjectedController
import queries.CreateRoom
import scalikejdbc._

import scala.util.Random

@Singleton
class RoomController @Inject()() extends InjectedController with Circe {
  import Responses._
  import models.TypeBinders._
  import RoomController.roles

  def random = new Random(ThreadLocalRandom.current())

  def create() = Action(circe.tolerantJson[CreateRoom]) { implicit req =>
    UserController.getUser().map{ user =>
      DB.localTx{ implicit session =>
        val roomId = Room.create(req.body, user.id)
        Join.create(user.id, roomId)
      }
      Success
    }.merge
  }

  def join(roomId: UUID) = Action { implicit req =>
    UserController.authRoom(roomId).map { auth =>
      Join.create(auth.user.id, auth.room.id)(AutoSession)
      Success
    }.merge
  }

  def start(roomId: UUID) = Action { implicit req =>
    UserController.authRoomOwner(roomId).map { case (_, room) =>
      import models.Aliases.j
      val joins = Join.findAllBy(sqls.eq(j.roomId, room.id))
      Either.cond(4 <= joins.length, createPlayers(joins, roomId), BadReq).merge
    }.merge
  }

  private def createPlayers(joins: Seq[Join], roomId: UUID) = {
    random.shuffle(joins).zip(roles(joins.length)).foreach{ case (join, role) =>
      Player.create(join.userId, roomId, role)(AutoSession)
    }
    Success
  }
}

object RoomController {
  def roles(count: Int): Seq[Role] = {
    val wolf = count / 4
    val villager = count - wolf - 1
    Role.Seer :: List.fill(wolf)(Role.Werewolf) ++ List.fill(villager)(Role.Villager)
  }
}
