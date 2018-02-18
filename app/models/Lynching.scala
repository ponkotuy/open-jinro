package models

import java.time.ZonedDateTime
import java.util.UUID

import scalikejdbc._
import skinny.orm.{Alias, SkinnyCRUDMapper, SkinnyCRUDMapperWithId}

case class Lynching(roomId: UUID, day: Int, userId: UUID, created: ZonedDateTime)

object Lynching extends SkinnyCRUDMapper[Lynching] {
  override val defaultAlias: Alias[Lynching] = createAlias("l")

  override def extract(rs: WrappedResultSet, n: ResultName[Lynching]): Lynching = autoConstruct(rs, n)

  def create(
      room: Room,
      userId: UUID,
      created: ZonedDateTime = ZonedDateTime.now()
  )(implicit session: DBSession): Long =
    createWithAttributes(
      'roomId -> room.id,
      'day -> room.day,
      'userId -> userId,
      'created -> created
    )
}
