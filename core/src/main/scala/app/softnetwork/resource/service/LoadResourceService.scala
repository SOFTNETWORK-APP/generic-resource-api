package app.softnetwork.resource.service

import app.softnetwork.persistence.service.Service
import app.softnetwork.resource.handlers.GenericResourceHandler
import app.softnetwork.resource.model.Resource.ProviderType
import app.softnetwork.resource.message.ResourceMessages.{
  CreateResource,
  LoadResource,
  ResourceCommand,
  ResourceCreated,
  ResourceError,
  ResourceLoaded,
  ResourceResult,
  ResourceUpdated,
  UpdateResource
}
import app.softnetwork.resource.spi.{ResourceProvider, ResourceProviders}
import app.softnetwork.utils.MimeTypeTools
import org.apache.tika.mime.MediaType

import java.nio.file.Path
import scala.concurrent.Future

trait LoadResourceService extends Service[ResourceCommand, ResourceResult] {
  _: GenericResourceHandler =>

  def providerType: ProviderType

  def provider: ResourceProvider = ResourceProviders.provider(providerType)

  def loadResource(segments: List[String]): Option[(Path, Option[MediaType])] = {
    val resourceDetails: ResourceDetails = segments
    import resourceDetails._
    provider.loadResource(uuid, uri, None, options: _*) match {
      case Some(path) =>
        Some(
          path,
          MimeTypeTools.detectMimeType(path).flatMap(mimeType => Option(MediaType.parse(mimeType)))
        )
      case _ =>
        run(uuid, LoadResource(uuid)) match {
          case result: ResourceLoaded =>
            provider.loadResource(uuid, uri, Option(result.resource.content), options: _*) match {
              case Some(path) =>
                Some(
                  path,
                  result.resource.mimetype match {
                    case Some(mimeType) => Option(MediaType.parse(mimeType))
                    case _ =>
                      MimeTypeTools
                        .detectMimeType(path)
                        .flatMap(mimeType => Option(MediaType.parse(mimeType)))
                  }
                )
              case _ =>
                log.warn(s"Could not find resource for ${segments.mkString("/")}")
                None
            }
          case _ =>
            log.warn(s"Could not find resource for ${segments.mkString("/")}")
            None
        }
    }
  }

  def uploadResource(
    resourceDetails: ResourceDetails,
    bytes: Array[Byte],
    update: Boolean
  ): Future[Either[ResourceError, ResourceResult]] = {
    import resourceDetails._
    log.info(s"Resource $uuid uploaded successfully")
    run(
      uuid,
      if (update) {
        UpdateResource(
          uuid,
          bytes,
          uri
        )
      } else {
        CreateResource(
          uuid,
          bytes,
          uri
        )
      }
    ).map {
      case ResourceCreated  => Right(ResourceCreated)
      case ResourceUpdated  => Right(ResourceUpdated)
      case r: ResourceError => Left(r)
    }
  }

}
