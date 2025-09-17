package app.softnetwork.resource.scalatest

import app.softnetwork.persistence.scalatest.InMemoryPersistenceTestKit
import app.softnetwork.resource.config.ResourceSettings
import app.softnetwork.resource.launch.ResourceGuardian
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

  lazy val provider: ResourceProvider = ResourceProviders.provider(providerType)
}
