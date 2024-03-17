package app.softnetwork.resource.scalatest

import akka.actor.typed.ActorSystem
import app.softnetwork.resource.model.Resource
import app.softnetwork.resource.service.{LocalFileSystemResourceService, ResourceService}
import app.softnetwork.session.model.{SessionData, SessionDataCompanion, SessionDataDecorator}
import app.softnetwork.session.service.SessionMaterials
import com.softwaremill.session.{RefreshTokenStorage, SessionConfig, SessionManager}
import org.scalatest.Suite
import org.slf4j.{Logger, LoggerFactory}
import org.softnetwork.session.model.Session

import scala.concurrent.ExecutionContext

trait ResourceToLocalFileSystemRoutesTestKit[SD <: SessionData with SessionDataDecorator[SD]]
    extends ResourceToLocalFileSystemRouteTestKit[SD]
    with ResourceRoutesTestKit[SD, Resource] {
  self: Suite with SessionMaterials[SD] =>

  override def resourceService: ActorSystem[_] => ResourceService[SD] =
    sys =>
      new LocalFileSystemResourceService[SD] with SessionMaterials[SD] {
        override implicit def system: ActorSystem[_] = sys

        override lazy val ec: ExecutionContext = sys.executionContext
        lazy val log: Logger = LoggerFactory getLogger getClass.getName

        override protected def sessionType: Session.SessionType = self.sessionType

        override implicit def manager(implicit
          sessionConfig: SessionConfig,
          companion: SessionDataCompanion[SD]
        ): SessionManager[SD] = self.manager

        override implicit def refreshTokenStorage: RefreshTokenStorage[SD] =
          self.refreshTokenStorage

        override implicit def companion: SessionDataCompanion[SD] = self.companion
      }

}
