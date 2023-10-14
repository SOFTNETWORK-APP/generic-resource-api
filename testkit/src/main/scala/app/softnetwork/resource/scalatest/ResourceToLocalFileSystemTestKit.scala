package app.softnetwork.resource.scalatest

import akka.actor.typed.ActorSystem
import app.softnetwork.persistence.launch.PersistentEntity
import app.softnetwork.persistence.launch.PersistenceGuardian._
import app.softnetwork.persistence.query.{InMemoryJournalProvider, InMemoryOffsetProvider}
import app.softnetwork.resource.message.ResourceEvents.ResourceEvent
import app.softnetwork.resource.message.ResourceMessages.{ResourceCommand, ResourceResult}
import app.softnetwork.resource.model.Resource
import app.softnetwork.resource.persistence.query.{
  GenericResourceToExternalProcessorStream,
  ResourceToLocalFileSystemProcessorStream
}
import app.softnetwork.resource.persistence.typed.ResourceBehavior
import app.softnetwork.session.{CsrfCheck, CsrfCheckHeader}
import org.scalatest.Suite
import org.slf4j.{Logger, LoggerFactory}

trait ResourceToLocalFileSystemTestKit extends ResourceTestKit[Resource] with CsrfCheckHeader {
  _: Suite =>

  override def resourceEntity
    : ActorSystem[_] => PersistentEntity[ResourceCommand, Resource, ResourceEvent, ResourceResult] =
    _ => ResourceBehavior

  override def resourceToExternalProcessorStream
    : ActorSystem[_] => GenericResourceToExternalProcessorStream[Resource] = sys =>
    new ResourceToLocalFileSystemProcessorStream
      with InMemoryJournalProvider
      with InMemoryOffsetProvider {
      override val forTests = true

      override implicit def system: ActorSystem[_] = sys
      lazy val log: Logger = LoggerFactory getLogger getClass.getName
    }

}
