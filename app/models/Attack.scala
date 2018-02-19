package models

import java.time.ZonedDateTime
import java.util.UUID

import scalikejdbc._
import TypeBinders._
import skinny.orm.{Alias, SkinnyCRUDMapper}

case class Attack(
    roomId: UUID,
    userId: UUID,
    destUserId: UUID,
    result: Boolean, // Foxだと失敗するのでfalseになる
    created: ZonedDateTime
)

object Attack extends SkinnyCRUDMapper[Attack] {
  override def defaultAlias: Alias[Attack] = createAlias("a")

  override def extract(rs: WrappedResultSet, n: ResultName[Attack]): Attack = autoConstruct(rs, n)

  def create(a: Attack)(implicit session: DBSession): Long = createWithAttributes(
    'roomId -> a.roomId,
    'userId -> a.userId,
    'destUserId -> a.destUserId,
    'result -> a.result,
    'created -> a.created
  )
}
