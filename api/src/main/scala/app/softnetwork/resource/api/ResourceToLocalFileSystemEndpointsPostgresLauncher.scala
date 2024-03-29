package app.softnetwork.resource.api

import akka.actor.typed.ActorSystem
import app.softnetwork.persistence.jdbc.schema.{JdbcSchemaProvider, JdbcSchemaTypes}
import app.softnetwork.persistence.schema.SchemaType
import app.softnetwork.session.CsrfCheckHeader
import app.softnetwork.session.handlers.SessionRefreshTokenDao
import app.softnetwork.session.model.{SessionDataCompanion, SessionManagers}
import com.softwaremill.session.{RefreshTokenStorage, SessionManager}
import org.slf4j.{Logger, LoggerFactory}
import org.softnetwork.session.model.Session

object ResourceToLocalFileSystemEndpointsPostgresLauncher
    extends ResourceToLocalFileSystemEndpointsApi[Session]
    with CsrfCheckHeader
    with JdbcSchemaProvider {
  lazy val log: Logger = LoggerFactory getLogger getClass.getName

  def schemaType: SchemaType = JdbcSchemaTypes.Postgres

  override protected def manager: SessionManager[Session] = SessionManagers.basic

  override implicit def companion: SessionDataCompanion[Session] = Session

  override protected def refreshTokenStorage: ActorSystem[_] => RefreshTokenStorage[Session] =
    system => SessionRefreshTokenDao(system)
}
