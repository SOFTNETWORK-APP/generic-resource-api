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
import app.softnetwork.session.CsrfCheck
import app.softnetwork.session.config.Settings
import app.softnetwork.session.model.SessionManagers
import com.softwaremill.session.{SessionConfig, SessionManager}
import org.softnetwork.session.model.Session

trait ResourceApi extends ResourceApplication[Resource] { _: SchemaProvider with CsrfCheck =>

  override def resourceEntity
    : ActorSystem[_] => PersistentEntity[ResourceCommand, Resource, ResourceEvent, ResourceResult] =
    _ => ResourceBehavior

  def resourceSwagger: ActorSystem[_] => SwaggerEndpoint

  def sessionConfig: SessionConfig = Settings.Session.DefaultSessionConfig

  override protected def sessionType: Session.SessionType =
    Settings.Session.SessionContinuityAndTransport

  override protected def manager(implicit sessionConfig: SessionConfig): SessionManager[Session] =
    SessionManagers.basic

}
