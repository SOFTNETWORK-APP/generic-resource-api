package app.softnetwork.resource.api

import akka.actor.typed.ActorSystem
import app.softnetwork.persistence.jdbc.schema.{JdbcSchemaProvider, JdbcSchemaTypes}
import app.softnetwork.persistence.schema.SchemaType
import app.softnetwork.session.service.{OneOffCookieSessionEndpoints, SessionEndpoints}
import com.softwaremill.session.CsrfCheckHeader
import org.slf4j.{Logger, LoggerFactory}

object ResourceToLocalFileSystemEndpointsWithOneOffCookiePostgresLauncher
    extends ResourceToLocalFileSystemEndpointsApi
    with CsrfCheckHeader
    with JdbcSchemaProvider {
  lazy val log: Logger = LoggerFactory getLogger getClass.getName

  def schemaType: SchemaType = JdbcSchemaTypes.Postgres

  override def sessionEndpoints: ActorSystem[_] => SessionEndpoints = system =>
    SessionEndpoints.oneOffCookie(system, checkHeaderAndForm)
}