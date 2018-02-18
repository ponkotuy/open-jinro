package models

import java.time.ZonedDateTime
import java.util.UUID

import scalikejdbc._
import skinny.orm.{Alias, SkinnyCRUDMapper}
import TypeBinders._

case class Join(
    userId: UUID,
    roomId: UUID,
    created: ZonedDateTime
)

object Join extends SkinnyCRUDMapper[Join] {
  override val defaultAlias: Alias[Join] = createAlias("j")

  override def extract(rs: WrappedResultSet, n: ResultName[Join]): Join = autoConstruct(rs, n)

  def create(userId: UUID, roomId: UUID)(implicit session: DBSession) = {
    createWithAttributes(
      'userId -> userId,
      'roomId -> roomId,
      'created -> ZonedDateTime.now()
    )
  }
}
