package models

import java.util.UUID

import scalikejdbc._
import skinny.orm.{Alias, SkinnyCRUDMapperWithId}
import models.TypeBinders._

case class Vote(
    id: UUID,
    roomId: UUID,
    playerId: UUID,
    destPlayerId: UUID,
    day: Int
)

object Vote extends SkinnyCRUDMapperWithId[UUID, Vote] {
  import Aliases.v

  override def idToRawValue(id: UUID): Any = id
  override def rawValueToId(value: Any): UUID = UUID.fromString(value.toString)

  override val defaultAlias: Alias[Vote] = createAlias("v")
  override def extract(rs: WrappedResultSet, n: ResultName[Vote]): Vote = autoConstruct(rs, n)

  def findAllByRoom(room: Room)(implicit session: DBSession): Seq[Vote] =
    findAllBy(sqls.eq(v.roomId, room.id).and.eq(v.day, room.day))

  def create(room: Room, playerId: UUID, destPlayerId: UUID)(implicit session: DBSession): UUID = createWithAttributes(
    'id -> UUID.randomUUID(),
    'roomId -> room.id,
    'playerId -> playerId,
    'destPlayerId -> destPlayerId,
    'day -> room.day
  )
}
