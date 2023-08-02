package app.softnetwork.resource.service

import akka.stream.scaladsl.{FileIO, Source}
import akka.util.ByteString
import app.softnetwork.api.server.ApiErrors
import app.softnetwork.resource.config.ResourceSettings
import app.softnetwork.resource.handlers.GenericResourceHandler
import app.softnetwork.resource.message.ResourceMessages._
import app.softnetwork.resource.spi.{ResourceProvider, SimpleResource}
import app.softnetwork.session.service.ServiceWithSessionEndpoints
import org.softnetwork.session.model.Session
import sttp.capabilities.akka.AkkaStreams
import sttp.model.headers.CookieValueWithMeta
import sttp.model.{Method, Part, StatusCode}
import sttp.tapir._
import sttp.tapir.json.json4s.jsonBody
import sttp.tapir.server.{PartialServerEndpointWithSecurityOutput, ServerEndpoint}

import scala.concurrent.Future

trait ResourceServiceEndpoints
    extends LoadResourceService
    with ServiceWithSessionEndpoints[ResourceCommand, ResourceResult] {
  _: GenericResourceHandler with ResourceProvider =>

  import app.softnetwork.serialization._

  def error(e: ResourceError): ApiErrors.ErrorInfo =
    e match {
      case ResourceNotFound => ApiErrors.NotFound(ResourceNotFound)
      case _                => ApiErrors.BadRequest(e.message)
    }

  def secureEndpoint: PartialServerEndpointWithSecurityOutput[
    (Seq[Option[String]], Option[String], Method, Option[String]),
    Session,
    Unit,
    Any,
    (Seq[Option[String]], Option[CookieValueWithMeta]),
    Unit,
    Any,
    Future
  ] =
    ApiErrors
      .withApiErrorVariants(
        antiCsrfWithRequiredSession(sc, gt, checkMode)
      )
      .in(ResourceSettings.ResourcePath)

  val library: ServerEndpoint[Any with AkkaStreams, Future] =
    secureEndpoint.get
      .in("library")
      .in(paths)
      .out(jsonBody[List[SimpleResource]])
      .serverLogicSuccess(_ => segments => Future.successful(listResources(segments.mkString("/"))))

  def loadResourceBusinessLogic(segments: List[String]): Either[
    ApiErrors.ErrorInfo,
    Source[ByteString, Any]
  ] =
    loadResource(segments) match {
      case Some((path, _)) => Right(FileIO.fromPath(path))
      case _               => Left(error(ResourceNotFound))
    }

  val getResource: ServerEndpoint[Any with AkkaStreams, Future] =
    secureEndpoint.get
      .in(paths)
      .out(streamBinaryBody(AkkaStreams)(CodecFormat.OctetStream()))
      .serverLogic(_ => segments => Future.successful(loadResourceBusinessLogic(segments)))

  val getImage: ServerEndpoint[Any with AkkaStreams, Future] =
    secureEndpoint.get
      .in("images")
      .in(paths)
      .out(
        streamBinaryBody(AkkaStreams)(CodecFormat.OctetStream())
      )
      .serverLogic(_ => segments => Future.successful(loadResourceBusinessLogic(segments)))

  def uploadResource[T <: Upload](implicit
    multipartCodec: MultipartCodec[T]
  ): PartialServerEndpointWithSecurityOutput[
    (Seq[Option[String]], Option[String], Method, Option[String]),
    Session,
    (List[String], T),
    Any,
    (Seq[Option[String]], Option[CookieValueWithMeta]),
    ResourceResult,
    Any,
    Future
  ] =
    secureEndpoint
      .in(paths.description("URI of the resource"))
      .in(multipartBody[T].description("Multipart file to upload"))
      .out(
        oneOf[ResourceResult](
          oneOfVariant[ResourceCreated.type](
            statusCode(StatusCode.Created)
              .and(emptyOutputAs(ResourceCreated).description("Resource created"))
          ),
          oneOfVariant[ResourceUpdated.type](
            statusCode(StatusCode.Ok)
              .and(emptyOutputAs(ResourceUpdated).description("Resource updated"))
          )
        )
      )

  def addResource[T <: Upload](implicit
    multipartCodec: MultipartCodec[T]
  ): ServerEndpoint[Any with AkkaStreams, Future] =
    uploadResource[T].post
      .description("Add a resource")
      .serverLogic(session => { case (segments, upload) =>
        val resourceDetails: ResourceDetails = segments
        import resourceDetails._
        uploadResource(
          resourceDetails.copy(uuid = s"${session.id}#$uuid"),
          upload.bytes,
          update = false
        ) map {
          case Right(r) => Right(r)
          case Left(l)  => Left(error(l))
        }
      })

  def updateResource[T <: Upload](implicit
    multipartCodec: MultipartCodec[T]
  ): ServerEndpoint[Any with AkkaStreams, Future] =
    uploadResource[T].put
      .description("Update the resource")
      .serverLogic(session => { case (segments, upload) =>
        val resourceDetails: ResourceDetails = segments
        import resourceDetails._
        uploadResource(
          resourceDetails.copy(uuid = s"${session.id}#$uuid"),
          upload.bytes,
          update = true
        ) map {
          case Right(r) => Right(r)
          case Left(l)  => Left(error(l))
        }
      })

  def uploadImage[T <: Upload](implicit
    multipartCodec: MultipartCodec[T]
  ): PartialServerEndpointWithSecurityOutput[
    (Seq[Option[String]], Option[String], Method, Option[String]),
    Session,
    (List[String], T),
    Any,
    (Seq[Option[String]], Option[CookieValueWithMeta]),
    ResourceResult,
    Any,
    Future
  ] =
    secureEndpoint
      .in("images")
      .in(paths.description("URI of the image"))
      .in(multipartBody[T].description("Multipart image to upload"))
      .out(
        oneOf[ResourceResult](
          oneOfVariant[ResourceCreated.type](
            statusCode(StatusCode.Created)
              .and(emptyOutputAs(ResourceCreated).description("Resource created"))
          ),
          oneOfVariant[ResourceUpdated.type](
            statusCode(StatusCode.Ok)
              .and(emptyOutputAs(ResourceUpdated).description("Resource updated"))
          )
        )
      )

  def addImage[T <: Upload](implicit
    multipartCodec: MultipartCodec[T]
  ): ServerEndpoint[Any with AkkaStreams, Future] =
    uploadImage[T].post
      .description("Add an image")
      .serverLogic(session => { case (segments, upload) =>
        val resourceDetails: ResourceDetails = segments
        import resourceDetails._
        uploadResource(
          resourceDetails.copy(uuid = s"${session.id}#$uuid"),
          upload.bytes,
          update = false
        ) map {
          case Right(r) => Right(r)
          case Left(l)  => Left(error(l))
        }
      })

  def updateImage[T <: Upload](implicit
    multipartCodec: MultipartCodec[T]
  ): ServerEndpoint[Any with AkkaStreams, Future] =
    uploadImage[T].put
      .description("Update the image")
      .serverLogic(session => { case (segments, upload) =>
        val resourceDetails: ResourceDetails = segments
        import resourceDetails._
        uploadResource(
          resourceDetails.copy(uuid = s"${session.id}#$uuid"),
          upload.bytes,
          update = true
        ) map {
          case Right(r) => Right(r)
          case Left(l)  => Left(error(l))
        }
      })

  def deleteResourceBusinessLogic(
    session: Session,
    resourceDetails: ResourceDetails
  ): Future[Either[ResourceError, Unit]] = {
    import resourceDetails._
    run(s"${session.id}#$uuid", DeleteResource(s"${session.id}#$uuid")).map {
      case ResourceDeleted  => Right(())
      case e: ResourceError => Left(e)
    }
  }

  val deleteResource: ServerEndpoint[Any with AkkaStreams, Future] =
    secureEndpoint
      .in(paths)
      .delete
      .serverLogic(session =>
        segments =>
          deleteResourceBusinessLogic(session, segments) map {
            case Right(_) => Right(())
            case Left(l)  => Left(error(l))
          }
      )

  val deleteImage: ServerEndpoint[Any with AkkaStreams, Future] =
    secureEndpoint
      .in("images")
      .in(paths)
      .delete
      .serverLogic(session =>
        segments =>
          deleteResourceBusinessLogic(session, segments) map {
            case Right(_) => Right(())
            case Left(l)  => Left(error(l))
          }
      )

  override def endpoints: List[ServerEndpoint[Any with AkkaStreams, Future]] = List(
    library,
    addImage[UploadImage],
    updateImage[UploadImage],
    getImage,
    deleteImage,
    addResource[UploadResource],
    updateResource[UploadResource],
    getResource,
    deleteResource
  )

}

sealed trait Upload {
  def bytes: Array[Byte]
}

case class UploadResource(file: Part[Array[Byte]]) extends Upload {
  override lazy val bytes: Array[Byte] = file.body
}

case class UploadImage(picture: Part[Array[Byte]]) extends Upload {
  override lazy val bytes: Array[Byte] = picture.body
}
