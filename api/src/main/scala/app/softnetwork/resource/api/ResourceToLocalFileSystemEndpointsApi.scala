package app.softnetwork.resource.api

import akka.actor.typed.ActorSystem
import app.softnetwork.api.server.Endpoint
import app.softnetwork.persistence.schema.SchemaProvider
import app.softnetwork.resource.launch.ResourceEndpoints
import app.softnetwork.resource.model.Resource
import app.softnetwork.resource.service.{
  LocalFileSystemResourceServiceEndpoints,
  ResourceServiceEndpoints
}
import app.softnetwork.session.CsrfCheck
import app.softnetwork.session.service.SessionMaterials
import com.softwaremill.session.{SessionConfig, SessionManager}
import org.slf4j.{Logger, LoggerFactory}
import org.softnetwork.session.model.Session

import scala.concurrent.ExecutionContext

trait ResourceToLocalFileSystemEndpointsApi
    extends ResourceToLocalFileSystemApi
    with ResourceEndpoints[Resource] { self: SchemaProvider with CsrfCheck =>

  override def resourceEndpoints: ActorSystem[_] => ResourceServiceEndpoints =
    sys =>
      new LocalFileSystemResourceServiceEndpoints with SessionMaterials {
        override implicit def manager(implicit
          sessionConfig: SessionConfig
        ): SessionManager[Session] = self.manager
        override protected def sessionType: Session.SessionType = self.sessionType
        override def log: Logger = LoggerFactory getLogger getClass.getName
        override implicit def system: ActorSystem[_] = sys
        override implicit lazy val ec: ExecutionContext = sys.executionContext
        override implicit def sessionConfig: SessionConfig = self.sessionConfig
      }

  override def endpoints: ActorSystem[_] => List[Endpoint] = system =>
    super.endpoints(system) :+ resourceSwagger(system)
}
