package models

import java.time.ZonedDateTime
import java.util.UUID

import scalikejdbc._
import skinny.orm.{Alias, SkinnyCRUDMapperWithId}
import TypeBinders._

case class User(
    id: UUID,
    name: String,
    created: ZonedDateTime
) {
  def player()(implicit session: DBSession): Option[Player] = Player.findLastByUser(id)
}

object User extends SkinnyCRUDMapperWithId[UUID, User] {
  override def idToRawValue(id: UUID): Any = id

  override def rawValueToId(value: Any): UUID = UUID.fromString(value.toString)

  override val defaultAlias: Alias[User] = createAlias("u")

  override def extract(rs: WrappedResultSet, n: ResultName[User]): User = autoConstruct(rs, n)

  def create(name: String)(implicit session: DBSession): UUID = {
    val id = UUID.randomUUID()
    createWithAttributes(
      'id -> id,
      'name -> name,
      'created -> ZonedDateTime.now()
    )
    id
  }
}
