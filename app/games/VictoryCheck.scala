package games

import models.{Player, Side}
import scalikejdbc.DBSession

object VictoryCheck {
  def check(players: Seq[Player]): VictoryCheckResult = {
    import VictoryCheckResult._
    val counts: Map[Side, Int] = players.filter(_.alive).groupBy(_.role.side).mapValues(_.size).withDefaultValue(0)
    val tmp: Option[Side] =
      if(counts(Side.Werewolf) == 0) Some(Side.Villager)
      else if(counts(Side.Villager) <= counts(Side.Werewolf)) Some(Side.Werewolf)
      else None
    if(tmp.isDefined && 0 < counts(Side.Fox)) Decided(Side.Fox) else tmp.fold[VictoryCheckResult](Continue)(Decided)
  }
}

sealed abstract class VictoryCheckResult

object VictoryCheckResult {
  case class Decided(side: Side) extends VictoryCheckResult {
    def apply(players: Seq[Player])(implicit session: DBSession): Unit = {
      players.foreach { player =>
        if(player.role.side == side) player.win() else player.lose()
      }
    }
  }
  case object Continue extends VictoryCheckResult
}
