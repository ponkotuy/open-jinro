package controllers

import java.time.ZonedDateTime
import java.util.UUID
import javax.inject.Inject

import games.{VictoryCheck, VictoryCheckResult}
import io.circe.generic.auto._
import models.{Attack, Player, Role}
import play.api.libs.circe.Circe
import play.api.mvc.InjectedController
import queries.CreateAttack
import scalikejdbc.DB

class AttackController @Inject()() extends InjectedController with Circe {
  import Responses._
  def attack(roomId: UUID) = Action(circe.tolerantJson[CreateAttack]) { implicit req =>
    UserController.authRoom(roomId).map { auth =>
      if(auth.player.role != Role.Werewolf) Forbidden("You're not werewolf,")
      else {
        val destId = req.body.dest
        DB localTx { implicit session =>
          Player.findById(destId).fold(notFound(s"dest: ${destId}")){ dest =>
            val success = dest.role != Role.Fox
            val attack = Attack(roomId, auth.user.id, req.body.dest, success, ZonedDateTime.now())
            Attack.create(attack)
            if(success) dest.kill()
            VictoryCheck.check(Player.findAllByRoom(roomId)) match {
              case x: VictoryCheckResult.Decided => x.apply()
              case _ =>
            }
            Success
          }
        }
      }
    }.merge
  }
}
