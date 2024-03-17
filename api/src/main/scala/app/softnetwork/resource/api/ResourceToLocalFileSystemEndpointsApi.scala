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
import app.softnetwork.session.model.{SessionData, SessionDataCompanion, SessionDataDecorator}
import app.softnetwork.session.service.SessionMaterials
import com.softwaremill.session.{RefreshTokenStorage, SessionConfig, SessionManager}
import org.slf4j.{Logger, LoggerFactory}
import org.softnetwork.session.model.Session

import scala.concurrent.ExecutionContext

trait ResourceToLocalFileSystemEndpointsApi[SD <: SessionData with SessionDataDecorator[SD]]
    extends ResourceToLocalFileSystemApi[SD]
    with ResourceEndpoints[SD, Resource] {
  self: SchemaProvider with CsrfCheck =>

  override def resourceEndpoints: ActorSystem[_] => ResourceServiceEndpoints[SD] =
    sys =>
      new LocalFileSystemResourceServiceEndpoints[SD] with SessionMaterials[SD] {
        override implicit def system: ActorSystem[_] = sys
        override lazy val ec: ExecutionContext = sys.executionContext
        lazy val log: Logger = LoggerFactory getLogger getClass.getName
        override protected def sessionType: Session.SessionType = self.sessionType
        override implicit def manager(implicit
          sessionConfig: SessionConfig,
          companion: SessionDataCompanion[SD]
        ): SessionManager[SD] = self.manager
        override implicit def refreshTokenStorage: RefreshTokenStorage[SD] =
          self.refreshTokenStorage(sys)
        override implicit def companion: SessionDataCompanion[SD] = self.companion
      }

  override def endpoints: ActorSystem[_] => List[Endpoint] = system =>
    super.endpoints(system) :+ resourceSwagger(system)
}
