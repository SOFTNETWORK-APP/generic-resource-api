package app.softnetwork.resource.launch

import akka.actor.typed.ActorSystem
import app.softnetwork.persistence.launch.PersistentEntity
import app.softnetwork.persistence.query.EventProcessorStream
import app.softnetwork.persistence.schema.SchemaProvider
import app.softnetwork.persistence.typed.Singleton
import app.softnetwork.resource.ResourceCoreBuildInfo
import app.softnetwork.resource.message.ResourceEvents.ResourceEvent
import app.softnetwork.resource.message.ResourceMessages.{ResourceCommand, ResourceResult}
import app.softnetwork.resource.model.GenericResource
import app.softnetwork.resource.persistence.query.GenericResourceToExternalProcessorStream
import app.softnetwork.session.CsrfCheck
import app.softnetwork.session.launch.SessionGuardian
import com.typesafe.scalalogging.StrictLogging

trait ResourceGuardian[Resource <: GenericResource] extends SessionGuardian with StrictLogging {
  _: SchemaProvider with CsrfCheck =>

  def resourceEntity
    : ActorSystem[_] => PersistentEntity[ResourceCommand, Resource, ResourceEvent, ResourceResult]

  /** initialize all entities
    */
  override def entities: ActorSystem[_] => Seq[PersistentEntity[_, _, _, _]] = sys =>
    Seq(resourceEntity(sys))

  /** initialize all singletons
    */
  override def singletons: ActorSystem[_] => Seq[Singleton[_]] = _ => Seq.empty

  def resourceToExternalProcessorStream
    : ActorSystem[_] => GenericResourceToExternalProcessorStream[Resource]

  /** initialize all event processor streams
    */
  override def eventProcessorStreams: ActorSystem[_] => Seq[EventProcessorStream[_]] = sys =>
    Seq(
      resourceToExternalProcessorStream(sys)
    )

  override def systemVersion(): String =
    sys.env.getOrElse("VERSION", ResourceCoreBuildInfo.version)
}
