package app.softnetwork.resource.spi

import app.softnetwork.resource.model.Resource.ProviderType

import java.util.ServiceLoader
import scala.collection.JavaConverters._

object ResourceProviders {

  private[this] lazy val resourceProviderFactories: ServiceLoader[ResourceProviderSpi] =
    java.util.ServiceLoader.load(classOf[ResourceProviderSpi])

  private[this] var resourceProviders: Map[String, ResourceProvider] = Map.empty

  private[this] def registerProvider(providerType: String, provider: ResourceProvider): Unit = {
    resourceProviders += (providerType -> provider)
  }

  def provider(providerType: ProviderType): ResourceProvider = {
    resourceProviders.get(providerType.name) match {
      case Some(provider) => provider
      case None =>
        resourceProviderFactories
          .iterator()
          .asScala
          .find(_.providerType == providerType)
          .map { factory =>
            val provider = factory.provider
            registerProvider(providerType.name, provider)
            provider
          }
          .getOrElse(throw new Exception(s"No provider found for type ${providerType.name}"))
    }
  }
}
