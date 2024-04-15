package app.softnetwork.resource.api

import app.softnetwork.persistence.jdbc.schema.{JdbcSchemaProvider, JdbcSchemaTypes}
import app.softnetwork.persistence.schema.SchemaType
import app.softnetwork.session.api.SessionApi
import org.slf4j.{Logger, LoggerFactory}
import org.softnetwork.session.model.Session

object ResourceToLocalFileSystemRoutesPostgresLauncher
    extends ResourceToLocalFileSystemRoutesApi[Session]
    with SessionApi
    with JdbcSchemaProvider {
  lazy val log: Logger = LoggerFactory getLogger getClass.getName

  def schemaType: SchemaType = JdbcSchemaTypes.Postgres

}
