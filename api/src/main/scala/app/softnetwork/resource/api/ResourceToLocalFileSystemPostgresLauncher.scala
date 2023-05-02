package app.softnetwork.resource.api

import akka.actor.ActorSystem
import app.softnetwork.persistence.jdbc.schema.PostgresSchemaProvider
import app.softnetwork.persistence.typed._
import com.typesafe.config.Config
import org.slf4j.{Logger, LoggerFactory}

object ResourceToLocalFileSystemPostgresLauncher extends ResourceToLocalFileSystemApi {
  lazy val log: Logger = LoggerFactory getLogger getClass.getName

  override def schemaProvider = sys =>
    new PostgresSchemaProvider {
      override implicit def classicSystem: ActorSystem = sys
      override def config: Config = ResourceToLocalFileSystemPostgresLauncher.this.config
    }
}
