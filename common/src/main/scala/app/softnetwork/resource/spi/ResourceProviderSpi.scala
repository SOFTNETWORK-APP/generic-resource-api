package app.softnetwork.resource.spi

import app.softnetwork.resource.model.Resource.ProviderType

trait ResourceProviderSpi {

  def providerType: ProviderType

  def provider: ResourceProvider
}
