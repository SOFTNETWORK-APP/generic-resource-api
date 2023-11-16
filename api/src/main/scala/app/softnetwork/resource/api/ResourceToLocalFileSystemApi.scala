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
import app.softnetwork.session.config.Settings
import app.softnetwork.session.service.SessionMaterials
import com.softwaremill.session.{SessionConfig, SessionManager}
import com.typesafe.config.Config
import org.slf4j.{Logger, LoggerFactory}
import org.softnetwork.session.model.Session

import scala.concurrent.ExecutionContext

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

  def resourceSwagger: ActorSystem[_] => SwaggerEndpoint =
    sys =>
      new LocalFileSystemResourceServiceEndpoints with SwaggerEndpoint with SessionMaterials {
        override implicit def system: ActorSystem[_] = sys
        override lazy val ec: ExecutionContext = sys.executionContext
        lazy val log: Logger = LoggerFactory getLogger getClass.getName
        override implicit def sessionConfig: SessionConfig = self.sessionConfig
        override protected def sessionType: Session.SessionType = self.sessionType
        override implicit def manager(implicit
          sessionConfig: SessionConfig
        ): SessionManager[Session] = self.manager
        override val applicationVersion: String = systemVersion()
      }

}
