package app.softnetwork.resource.scalatest

import app.softnetwork.persistence.scalatest.InMemoryPersistenceTestKit
import app.softnetwork.resource.config.ResourceSettings
import app.softnetwork.resource.launch.ResourceGuardian
import app.softnetwork.resource.model.GenericResource
import org.scalatest.Suite

trait ResourceTestKit[Resource <: GenericResource]
    extends ResourceGuardian[Resource]
    with InMemoryPersistenceTestKit { _: Suite =>

  /** @return
    *   roles associated with this node
    */
  override def roles: Seq[String] = Seq(ResourceSettings.AkkaNodeRole)

}
