package games

import java.util.UUID

import models.{Lynching, Player, Room, Vote}
import scalikejdbc.DBSession

sealed abstract class LynchingResult

object LynchingResult {
  case class Decided(destPlayer: Player) extends LynchingResult {
    def apply(room: Room, players: Seq[Player])(implicit session: DBSession): Unit = {
      destPlayer.kill()
      Lynching.create(room, destPlayer.id)
      VictoryCheck.check(players) match {
        case x: VictoryCheckResult.Decided => x.apply()
        case _ =>
      }
    }
  }
  case class Revote(players: Seq[Player]) extends LynchingResult
  case object Waiting extends LynchingResult

  def apply(room: Room, players: Seq[Player], votes: Seq[Vote]): LynchingResult = {
    if(players.size != votes.size) LynchingResult.Waiting
    else {
      val counts: Seq[(UUID, Int)] = votes.groupBy(_.destPlayerId).mapValues(_.size).toSeq
      val max = counts.map(_._2).max
      if(1 < counts.count(_._2 == max)) {
        val dests = counts.filter(_._2 == max)
        LynchingResult.Revote(players.filter(dests.contains))
      } else {
        val player = counts.find(_._2 == max).get._1
        LynchingResult.Decided(players.find(_.id == player).get)
      }
    }
  }
}
