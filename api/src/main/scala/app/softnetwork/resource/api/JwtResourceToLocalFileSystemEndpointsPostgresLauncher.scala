package app.softnetwork.resource.api

import akka.actor.typed.ActorSystem
import app.softnetwork.persistence.jdbc.schema.{JdbcSchemaProvider, JdbcSchemaTypes}
import app.softnetwork.persistence.schema.SchemaType
import app.softnetwork.session.CsrfCheckHeader
import app.softnetwork.session.handlers.JwtClaimsRefreshTokenDao
import app.softnetwork.session.model.{SessionDataCompanion, SessionManagers}
import com.softwaremill.session.{RefreshTokenStorage, SessionManager}
import org.slf4j.{Logger, LoggerFactory}
import org.softnetwork.session.model.JwtClaims

object JwtResourceToLocalFileSystemEndpointsPostgresLauncher
    extends ResourceToLocalFileSystemEndpointsApi[JwtClaims]
    with CsrfCheckHeader
    with JdbcSchemaProvider {
  lazy val log: Logger = LoggerFactory getLogger getClass.getName

  def schemaType: SchemaType = JdbcSchemaTypes.Postgres

  override protected def manager: SessionManager[JwtClaims] = SessionManagers.jwt

  override implicit def companion: SessionDataCompanion[JwtClaims] = JwtClaims

  override protected def refreshTokenStorage: ActorSystem[_] => RefreshTokenStorage[JwtClaims] =
    system => JwtClaimsRefreshTokenDao(system)
}
