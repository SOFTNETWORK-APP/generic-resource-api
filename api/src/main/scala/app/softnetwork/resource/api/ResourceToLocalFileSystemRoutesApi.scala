package app.softnetwork.resource.api

import akka.actor.typed.ActorSystem
import app.softnetwork.api.server.ApiRoute
import app.softnetwork.persistence.schema.SchemaProvider
import app.softnetwork.resource.launch.ResourceRoutes
import app.softnetwork.resource.model.Resource
import app.softnetwork.resource.service.{LocalFileSystemResourceService, ResourceService}
import app.softnetwork.session.CsrfCheckHeader
import app.softnetwork.session.service.SessionMaterials
import com.softwaremill.session.{SessionConfig, SessionManager}
import org.slf4j.{Logger, LoggerFactory}
import org.softnetwork.session.model.Session

import scala.concurrent.ExecutionContext

trait ResourceToLocalFileSystemRoutesApi
    extends ResourceToLocalFileSystemApi
    with ResourceRoutes[Resource]
    with CsrfCheckHeader { self: SchemaProvider =>

  override def resourceService: ActorSystem[_] => ResourceService =
    sys =>
      new LocalFileSystemResourceService with SessionMaterials {
        override implicit def manager(implicit
          sessionConfig: SessionConfig
        ): SessionManager[Session] = self.manager
        override protected def sessionType: Session.SessionType = self.sessionType
        override def log: Logger = LoggerFactory getLogger getClass.getName
        override implicit def system: ActorSystem[_] = sys
        override implicit lazy val ec: ExecutionContext = sys.executionContext
        override implicit def sessionConfig: SessionConfig = self.sessionConfig
      }

  override def apiRoutes: ActorSystem[_] => List[ApiRoute] = system =>
    super.apiRoutes(system) :+ resourceSwagger(system)
}
