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
    val name = providerType.name.toLowerCase
    resourceProviders.get(name) match {
      case Some(provider) => provider
      case None =>
        val factories = resourceProviderFactories.iterator().asScala
        factories
          .find(_.providerType.name.toLowerCase == name)
          .map { factory =>
            val provider = factory.provider
            registerProvider(name, provider)
            provider
          }
          .getOrElse {
            val providerTypeNames = factories.map(_.providerType.name.toLowerCase).toList
            throw new Exception(
              s"No provider found for type $name within available providers: [${providerTypeNames
                .mkString(",")}]"
            )
          }
    }
  }
}
