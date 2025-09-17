package app.softnetwork.resource.scalatest

import akka.actor.typed.ActorSystem
import app.softnetwork.persistence.scalatest.InMemoryPersistenceTestKit
import app.softnetwork.resource.config.ResourceSettings
import app.softnetwork.resource.handlers.ResourceDao
import app.softnetwork.resource.launch.ResourceGuardian
import app.softnetwork.resource.message.ResourceEvents.ResourceEvent
import app.softnetwork.resource.message.ResourceMessages.{
  ResourceCreated,
  ResourceDeleted,
  ResourceLoaded,
  ResourceUpdated
}
import app.softnetwork.resource.model.Resource.ProviderType
import app.softnetwork.resource.model.GenericResource
import app.softnetwork.resource.spi.{ResourceProvider, ResourceProviders}
import app.softnetwork.session.CsrfCheck
import org.scalatest.Suite

trait ResourceTestKit[Resource <: GenericResource]
    extends ResourceGuardian[Resource]
    with InMemoryPersistenceTestKit { _: Suite with CsrfCheck =>

  /** @return
    *   roles associated with this node
    */
  override def roles: Seq[String] = Seq(ResourceSettings.AkkaNodeRole)

  def providerType: ProviderType

  lazy val resourceProvider: ResourceProvider = ResourceProviders.provider(providerType)

  private[this] def resourceDao: ResourceDao = ResourceDao

  def createResource(entityId: String, bytes: Array[Byte], uri: Option[String])(implicit
    system: ActorSystem[_]
  ): ResourceEvent = {
    val probe = createTestProbe[ResourceEvent]()
    subscribeProbe(probe)
    resourceDao.createResource(entityId, bytes, uri) await {
      case ResourceCreated => probe.receiveMessage()
      case _               => fail(s"Resource $entityId not created")
    }
  }

  def updateResource(entityId: String, bytes: Array[Byte], uri: Option[String])(implicit
    system: ActorSystem[_]
  ): ResourceEvent = {
    val probe = createTestProbe[ResourceEvent]()
    subscribeProbe(probe)
    resourceDao.updateResource(entityId, bytes, uri) await {
      case ResourceUpdated => probe.receiveMessage()
      case _               => fail(s"Resource $entityId not updated")
    }
  }

  def deleteResource(entityId: String)(implicit system: ActorSystem[_]): ResourceEvent = {
    val probe = createTestProbe[ResourceEvent]()
    subscribeProbe(probe)
    resourceDao.deleteResource(entityId) await {
      case ResourceDeleted => probe.receiveMessage()
      case _               => fail(s"Resource $entityId not deleted")
    }
  }

  def loadResource(entityId: String)(implicit system: ActorSystem[_]): Resource = {
    resourceDao.loadResource(entityId) await {
      case r: ResourceLoaded => r.resource.asInstanceOf[Resource]
      case _                 => fail(s"Resource $entityId not found")
    }
  }

}
