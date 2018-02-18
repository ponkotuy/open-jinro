package models

import java.time.ZonedDateTime
import java.util.UUID

import queries.CreateRoom
import scalikejdbc._
import skinny.orm.{Alias, SkinnyCRUDMapperWithId}
import TypeBinders._
import utils.BCrypt

case class Room(
    id: UUID,
    name: String,
    pass: Option[String],
    owner: UUID,
    day: Int,
    created: ZonedDateTime
) {
  def matches(input: String): Boolean = pass.forall { p => BCrypt().matches(input, p) }
}

object Room extends SkinnyCRUDMapperWithId[UUID, Room] {
  override def idToRawValue(id: UUID): Any = id

  override def rawValueToId(value: Any): UUID = UUID.fromString(value.toString)

  override val defaultAlias: Alias[Room] = createAlias("r")

  override def extract(rs: WrappedResultSet, n: ResultName[Room]): Room = autoConstruct(rs, n)

  def create(r: CreateRoom, owner: UUID)(implicit session: DBSession): UUID = createWithAttributes(
    'id -> UUID.randomUUID(),
    'name -> r.name,
    'pass -> BCrypt().encode(r.pass),
    'owner -> owner,
    'created -> ZonedDateTime.now()
  )
}
