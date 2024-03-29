package app.softnetwork.resource.service

import akka.actor.typed.ActorSystem
import akka.stream.scaladsl.{FileIO, Source}
import akka.util.ByteString
import app.softnetwork.api.server.ApiErrors
import app.softnetwork.resource.config.ResourceSettings
import app.softnetwork.resource.handlers.GenericResourceHandler
import app.softnetwork.resource.message.ResourceMessages._
import app.softnetwork.resource.spi.{ResourceProvider, SimpleResource}
import app.softnetwork.session.config.Settings
import app.softnetwork.session.model.{SessionData, SessionDataCompanion, SessionDataDecorator}
import app.softnetwork.session.service.{ServiceWithSessionEndpoints, SessionMaterials}
import com.softwaremill.session.SessionConfig
import org.apache.tika.mime.MediaType
import sttp.capabilities.akka.AkkaStreams
import sttp.model.headers.CookieValueWithMeta
import sttp.model.{HeaderNames, Method, Part, StatusCode}
import sttp.tapir._
import sttp.tapir.json.json4s.jsonBody
import sttp.tapir.server.{PartialServerEndpointWithSecurityOutput, ServerEndpoint}

import scala.concurrent.Future

trait ResourceServiceEndpoints[SD <: SessionData with SessionDataDecorator[SD]]
    extends LoadResourceService
    with ServiceWithSessionEndpoints[ResourceCommand, ResourceResult, SD] {
  _: GenericResourceHandler with ResourceProvider with SessionMaterials[SD] =>

  import app.softnetwork.serialization._

  implicit def sessionConfig: SessionConfig = Settings.Session.DefaultSessionConfig

  implicit def companion: SessionDataCompanion[SD]

  override implicit def ts: ActorSystem[_] = system

  def error(e: ResourceError): ApiErrors.ErrorInfo =
    e match {
      case ResourceNotFound => ApiErrors.NotFound(ResourceNotFound)
      case _                => ApiErrors.BadRequest(e.message)
    }

  def secureEndpoint: PartialServerEndpointWithSecurityOutput[
    (Seq[Option[String]], Option[String], Method, Option[String]),
    SD,
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
    (Source[ByteString, Any], Option[MediaType])
  ] =
    loadResource(segments) match {
      case Some((path, media)) => Right((FileIO.fromPath(path), media))
      case _                   => Left(error(ResourceNotFound))
    }

  def getResource(prefix: Option[String]): ServerEndpoint[Any with AkkaStreams, Future] = {
    (prefix match {
      case Some(value) => secureEndpoint.get.in(value)
      case _           => secureEndpoint.get
    })
      .in(paths)
      .out(streamBinaryBody(AkkaStreams)(CodecFormat.OctetStream()))
      .out(header[Option[String]](HeaderNames.ContentType))
      .serverLogic(_ =>
        segments =>
          Future.successful(loadResourceBusinessLogic(segments) match {
            case Left(l)  => Left(l)
            case Right(r) => Right(r._1, r._2.map(_.toString))
          })
      )
  }

  val getImage: ServerEndpoint[Any with AkkaStreams, Future] = getResource(Some("images"))

  def uploadResource[T <: Upload](implicit
    multipartCodec: MultipartCodec[T]
  ): PartialServerEndpointWithSecurityOutput[
    (Seq[Option[String]], Option[String], Method, Option[String]),
    SD,
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
    SD,
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
    session: SD,
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
    getResource(None),
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
