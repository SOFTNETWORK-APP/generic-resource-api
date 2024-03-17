package app.softnetwork.resource.api

import akka.actor.typed.ActorSystem
import app.softnetwork.api.server.SwaggerEndpoint
import app.softnetwork.persistence.launch.PersistentEntity
import app.softnetwork.persistence.launch.PersistenceGuardian._
import app.softnetwork.persistence.schema.SchemaProvider
import app.softnetwork.resource.launch.ResourceApplication
import app.softnetwork.resource.message.ResourceEvents.ResourceEvent
import app.softnetwork.resource.message.ResourceMessages.{ResourceCommand, ResourceResult}
import app.softnetwork.resource.model.Resource
import app.softnetwork.resource.persistence.typed.ResourceBehavior
import app.softnetwork.resource.service.LocalFileSystemResourceServiceEndpoints
import app.softnetwork.session.CsrfCheck
import app.softnetwork.session.config.Settings
import app.softnetwork.session.model.{
  SessionData,
  SessionDataCompanion,
  SessionDataDecorator,
  SessionManagers
}
import app.softnetwork.session.service.SessionMaterials
import com.softwaremill.session.{RefreshTokenStorage, SessionConfig, SessionManager}
import org.slf4j.{Logger, LoggerFactory}
import org.softnetwork.session.model.Session

import scala.concurrent.ExecutionContext

trait ResourceApi[SD <: SessionData with SessionDataDecorator[SD]]
    extends ResourceApplication[Resource] { self: SchemaProvider with CsrfCheck =>

  override def resourceEntity
    : ActorSystem[_] => PersistentEntity[ResourceCommand, Resource, ResourceEvent, ResourceResult] =
    _ => ResourceBehavior

  implicit def sessionConfig: SessionConfig = Settings.Session.DefaultSessionConfig

  implicit def companion: SessionDataCompanion[SD]

  protected def manager: SessionManager[SD]

  protected def refreshTokenStorage: ActorSystem[_] => RefreshTokenStorage[SD]

  override protected def sessionType: Session.SessionType =
    Settings.Session.SessionContinuityAndTransport

  def resourceSwagger: ActorSystem[_] => SwaggerEndpoint =
    sys =>
      new LocalFileSystemResourceServiceEndpoints[SD]
        with SwaggerEndpoint
        with SessionMaterials[SD] {
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
        override val applicationVersion: String = systemVersion()
      }

}
