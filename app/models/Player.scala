package models

import java.sql.ResultSet
import java.time.ZonedDateTime
import java.util.UUID

import scalikejdbc._
import skinny.orm.{Alias, SkinnyCRUDMapperWithId}
import models.TypeBinders._

case class Player(
    id: UUID,
    userId: UUID,
    roomId: UUID,
    role: Role,
    alive: Boolean,
    victory: Option[Boolean],
    created: ZonedDateTime
) {
  def kill()(implicit session: DBSession): Int = Player.kill(id)
  def win()(implicit session: DBSession): Int = Player.win(id)
  def lose()(implicit session: DBSession): Int = Player.lose(id)
}

object Player extends SkinnyCRUDMapperWithId[UUID, Player] {
  import Aliases.p
  implicit def roleBinder = Role.binder

  override val defaultAlias: Alias[Player] = createAlias("p")

  override def extract(rs: WrappedResultSet, n: ResultName[Player]): Player = autoConstruct(rs, n)

  override def idToRawValue(id: UUID): Any = id
  override def rawValueToId(value: Any): UUID = UUID.fromString(value.toString)

  def findAllByRoom(roomId: UUID)(implicit session: DBSession): Seq[Player] = findAllBy(sqls.eq(p.roomId, roomId))

  def create(
      userId: UUID,
      roomId: UUID,
      role: Role,
      created: ZonedDateTime = ZonedDateTime.now()
  )(implicit session: DBSession): UUID =
    createWithAttributes(
      'id -> UUID.randomUUID(),
      'userId -> userId,
      'roomId -> roomId,
      'role -> role,
      'alive -> true,
      'victory -> None,
      'created -> created
    )

  def kill(id: UUID)(implicit session: DBSession): Int =
    updateById(id).withAttributes('alive -> false)

  def win(id: UUID)(implicit session: DBSession): Int =
    updateById(id).withAttributes('victory -> true)
  def lose(id: UUID)(implicit session: DBSession): Int =
    updateById(id).withAttributes('victory -> false)
}

sealed abstract class Role(val value: Int, val side: Side)

object Role {
  case object Werewolf extends Role(1, Side.Werewolf)
  case object Villager extends Role(2, Side.Villager)
  case object Seer extends Role(3, Side.Villager)
  case object Minion extends Role(4, Side.Werewolf)
  case object Fox extends Role(5, Side.Fox)
  case object Hunter extends Role(6, Side.Villager)

  val values = Vector(Werewolf, Villager, Seer, Minion, Fox, Hunter)

  def find(value: Int): Option[Role] = values.find(_.value == value)

  implicit val binder = new TypeBinder[Role] {
    override def apply(rs: ResultSet, columnIndex: Int): Role = Role.find(rs.getInt(columnIndex)).get
    override def apply(rs: ResultSet, columnLabel: String): Role = Role.find(rs.getInt(columnLabel)).get
  }
}

sealed abstract class Side(val value: Int)

object Side {
  case object Werewolf extends Side(1)
  case object Villager extends Side(2)
  case object Fox extends Side(3)
}
