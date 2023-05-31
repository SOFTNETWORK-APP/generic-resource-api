package app.softnetwork.resource.service

import app.softnetwork.persistence.service.Service
import app.softnetwork.resource.handlers.GenericResourceHandler
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
import app.softnetwork.resource.spi.ResourceProvider
import app.softnetwork.utils.MimeTypeTools
import org.apache.tika.mime.MediaType
import org.softnetwork.session.model.Session

import java.nio.file.Path
import scala.concurrent.Future

trait LoadResourceService extends Service[ResourceCommand, ResourceResult] {
  _: GenericResourceHandler with ResourceProvider =>

  def loadResource(resourceDetails: ResourceDetails): Option[(Path, Option[MediaType])] = {
    import resourceDetails._
    loadResource(uuid, uri, None, options: _*) match {
      case Some(path) =>
        Some(
          path,
          MimeTypeTools.detectMimeType(path).flatMap(mimeType => Option(MediaType.parse(mimeType)))
        )
      case _ =>
        run(uuid, LoadResource(uuid)) match {
          case result: ResourceLoaded =>
            loadResource(uuid, uri, Option(result.resource.content), options: _*) match {
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
              case _ => None
            }
          case _ => None
        }
    }
  }

  def uploadResource(
    session: Session,
    resourceDetails: ResourceDetails,
    bytes: Array[Byte],
    update: Boolean
  ): Future[Either[ResourceError, ResourceResult]] = {
    import resourceDetails._
    log.info(s"Resource ${session.id}#$uuid uploaded successfully")
    run(
      s"${session.id}#$uuid",
      if (update) {
        UpdateResource(
          s"${session.id}#$uuid",
          bytes,
          uri
        )
      } else {
        CreateResource(
          s"${session.id}#$uuid",
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
