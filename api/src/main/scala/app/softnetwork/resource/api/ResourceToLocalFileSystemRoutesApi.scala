package app.softnetwork.resource.api

import akka.actor.typed.ActorSystem
import app.softnetwork.api.server.ApiRoute
import app.softnetwork.persistence.schema.SchemaProvider
import app.softnetwork.resource.launch.ResourceRoutes
import app.softnetwork.resource.model.Resource
import app.softnetwork.resource.service.{LocalFileSystemResourceService, ResourceService}
import app.softnetwork.session.CsrfCheckHeader
import app.softnetwork.session.model.{SessionData, SessionDataCompanion, SessionDataDecorator}
import app.softnetwork.session.service.SessionMaterials
import com.softwaremill.session.{RefreshTokenStorage, SessionConfig, SessionManager}
import org.slf4j.{Logger, LoggerFactory}
import org.softnetwork.session.model.Session

import scala.concurrent.ExecutionContext

trait ResourceToLocalFileSystemRoutesApi[SD <: SessionData with SessionDataDecorator[SD]]
    extends ResourceToLocalFileSystemApi[SD]
    with ResourceRoutes[SD, Resource]
    with CsrfCheckHeader { self: SchemaProvider =>

  override def resourceService: ActorSystem[_] => ResourceService[SD] =
    sys =>
      new LocalFileSystemResourceService[SD] with SessionMaterials[SD] {
        override implicit def manager(implicit
          sessionConfig: SessionConfig,
          companion: SessionDataCompanion[SD]
        ): SessionManager[SD] = self.manager
        override protected def sessionType: Session.SessionType = self.sessionType
        override def log: Logger = LoggerFactory getLogger getClass.getName
        override implicit def sessionConfig: SessionConfig = self.sessionConfig
        override implicit def system: ActorSystem[_] = sys
        override lazy val ec: ExecutionContext = sys.executionContext
        override implicit def refreshTokenStorage: RefreshTokenStorage[SD] =
          self.refreshTokenStorage(sys)
        override implicit def companion: SessionDataCompanion[SD] = self.companion
      }

  override def apiRoutes: ActorSystem[_] => List[ApiRoute] = system =>
    super.apiRoutes(system) :+ resourceSwagger(system)
}
