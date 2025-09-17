package app.softnetwork.resource.persistence.query

import app.softnetwork.persistence.ManifestWrapper
import app.softnetwork.resource.model.Resource.ProviderType
import app.softnetwork.resource.model.GenericResource

trait LocalFileSystemGenericResourceProvider[Resource <: GenericResource]
    extends ExternalGenericResourceProvider[Resource] { _: ManifestWrapper[Resource] =>
  override def providerType: ProviderType = ProviderType.LOCAL
}
