package app.softnetwork.resource.spi

import app.softnetwork.resource.model.Resource.ProviderType
import org.slf4j.{Logger, LoggerFactory}

class LocalFileSystemProviderFactory extends ResourceProviderSpi {
  override def providerType: ProviderType = ProviderType.LOCAL

  override def provider: ResourceProvider =
    new LocalFileSystemProvider {
      lazy val log: Logger = LoggerFactory getLogger getClass.getName
    }

}
