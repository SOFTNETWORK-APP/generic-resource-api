package app.softnetwork.resource.api

import app.softnetwork.persistence.jdbc.schema.JdbcSchemaTypes.Postgres
import app.softnetwork.persistence.schema.SchemaType
import org.slf4j.{Logger, LoggerFactory}

object ResourceToLocalFileSystemPostgresLauncher extends ResourceToLocalFileSystemApi {
  lazy val log: Logger = LoggerFactory getLogger getClass.getName

  override val schemaType: SchemaType = Postgres
}
