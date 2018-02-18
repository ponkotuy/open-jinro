package models

import java.sql.ResultSet
import java.util.UUID

import scalikejdbc.{ParameterBinderFactory, TypeBinder}

object TypeBinders {
  implicit val uuid = new TypeBinder[UUID] {
    override def apply(rs: ResultSet, columnIndex: Int): UUID = UUID.fromString(rs.getString(columnIndex))
    override def apply(rs: ResultSet, columnLabel: String): UUID = UUID.fromString(rs.getString(columnLabel))
  }

  implicit val uuidBindFactory: ParameterBinderFactory[UUID] = ParameterBinderFactory {
    value => (stmt, idx) => stmt.setString(idx, value.toString)
  }
}
