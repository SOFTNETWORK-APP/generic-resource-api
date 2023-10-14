package app.softnetwork.resource.api

import akka.actor.typed.ActorSystem
import app.softnetwork.api.server.SwaggerEndpoint
import app.softnetwork.persistence.jdbc.query.{JdbcJournalProvider, JdbcOffsetProvider}
import app.softnetwork.persistence.schema.SchemaProvider
import app.softnetwork.resource.model.Resource
import app.softnetwork.resource.persistence.query.{
  GenericResourceToExternalProcessorStream,
  ResourceToLocalFileSystemProcessorStream
}
import app.softnetwork.resource.service.LocalFileSystemResourceServiceEndpoints
import app.softnetwork.session.CsrfCheck
import app.softnetwork.session.service.SessionEndpoints
import com.typesafe.config.Config
import org.slf4j.{Logger, LoggerFactory}

trait ResourceToLocalFileSystemApi extends ResourceApi { self: SchemaProvider with CsrfCheck =>
  override def resourceToExternalProcessorStream
    : ActorSystem[_] => GenericResourceToExternalProcessorStream[Resource] =
    sys =>
      new ResourceToLocalFileSystemProcessorStream()
        with JdbcJournalProvider
        with JdbcOffsetProvider {
        override implicit def system: ActorSystem[_] = sys
        override def config: Config = ResourceToLocalFileSystemApi.this.config
      }

  def resourceSwagger: ActorSystem[_] => SwaggerEndpoint = sys =>
    new LocalFileSystemResourceServiceEndpoints with SwaggerEndpoint {
      override implicit def system: ActorSystem[_] = sys
      lazy val log: Logger = LoggerFactory getLogger getClass.getName
      override def sessionEndpoints: SessionEndpoints = self.sessionEndpoints(system)
      override val applicationVersion: String = systemVersion()
    }

}
