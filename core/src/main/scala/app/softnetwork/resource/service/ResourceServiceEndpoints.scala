package app.softnetwork.resource.service

import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.{FileIO, Source}
import akka.util.ByteString
import app.softnetwork.api.server.ApiEndpoint
import app.softnetwork.resource.config.ResourceSettings
import app.softnetwork.resource.handlers.GenericResourceHandler
import app.softnetwork.resource.message.ResourceMessages._
import app.softnetwork.resource.spi.{ResourceProvider, SimpleResource}
import app.softnetwork.session.service.SessionEndpoints
import org.json4s.Formats
import org.softnetwork.session.model.Session
import sttp.capabilities.akka.AkkaStreams
import sttp.model.headers.CookieValueWithMeta
import sttp.model.{Method, Part, StatusCode}
import sttp.monad.FutureMonad
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.json4s.jsonBody
import sttp.tapir.server.ServerEndpoint.Full
import sttp.tapir.server.{PartialServerEndpoint, ServerEndpoint}

import scala.concurrent.Future

trait ResourceServiceEndpoints extends LoadResourceService with ApiEndpoint {
  _: GenericResourceHandler with ResourceProvider =>

  import app.softnetwork.serialization._

  implicit def formats: Formats = commonFormats

  def sessionEndpoints: SessionEndpoints

  def rootEndpoint: PartialServerEndpoint[
    (Seq[Option[String]], Method, Option[String], Option[String]),
    ((Seq[Option[String]], Option[CookieValueWithMeta]), Session),
    Unit,
    ResourceError,
    (Seq[Option[String]], Option[CookieValueWithMeta]),
    Any,
    Future
  ] =
    sessionEndpoints.antiCsrfWithRequiredSession.endpoint
      .in(ResourceSettings.ResourcePath)
      .out(sessionEndpoints.antiCsrfWithRequiredSession.securityOutput)
      .errorOut(
        oneOf[ResourceError](
          oneOfVariant[ResourceNotFound.type](
            statusCode(StatusCode.NotFound)
              .and(emptyOutputAs(ResourceNotFound).description("Resource not found"))
          ),
          oneOfVariant[UnauthorizedError.type](
            statusCode(StatusCode.Unauthorized)
              .and(emptyOutputAs(UnauthorizedError).description("Unauthorized"))
          )
        )
      )
      .serverSecurityLogic { inputs =>
        sessionEndpoints.antiCsrfWithRequiredSession.securityLogic(new FutureMonad())(inputs).map {
          case Left(_)  => Left(UnauthorizedError)
          case Right(r) => Right((r._1, r._2))
        }
      }

  val libraryEndpoint: Full[
    (Seq[Option[String]], Method, Option[String], Option[String]),
    ((Seq[Option[String]], Option[CookieValueWithMeta]), Session),
    List[String],
    ResourceError,
    (Seq[Option[String]], Option[CookieValueWithMeta], List[SimpleResource]),
    Any,
    Future
  ] =
    rootEndpoint.get
      .in("library")
      .in(paths)
      .out(jsonBody[List[SimpleResource]])
      .serverLogicSuccess(principal =>
        segments =>
          Future.successful(
            (principal._1._1, principal._1._2, listResources(segments.mkString("/")))
          )
      )

  def loadResourceBusinessLogic(
    principal: ((Seq[Option[String]], Option[CookieValueWithMeta]), Session)
  ): List[String] => Either[
    ResourceError,
    (Seq[Option[String]], Option[CookieValueWithMeta], Source[ByteString, Any])
  ] =
    segments =>
      loadResource(segments) match {
        case Some((path, _)) => Right((principal._1._1, principal._1._2, FileIO.fromPath(path)))
        case _               => Left(ResourceNotFound)
      }

  val getResourceEndpoint: Full[
    (Seq[Option[String]], Method, Option[String], Option[String]),
    ((Seq[Option[String]], Option[CookieValueWithMeta]), Session),
    List[String],
    ResourceError,
    (Seq[Option[String]], Option[CookieValueWithMeta], Source[ByteString, Any]),
    Any with AkkaStreams,
    Future
  ] =
    rootEndpoint.get
      .in(paths)
      .out(streamBinaryBody(AkkaStreams)(CodecFormat.OctetStream()))
      .serverLogic(principal =>
        segments => Future.successful(loadResourceBusinessLogic(principal)(segments))
      )

  val getImageEndpoint: Full[
    (Seq[Option[String]], Method, Option[String], Option[String]),
    ((Seq[Option[String]], Option[CookieValueWithMeta]), Session),
    List[String],
    ResourceError,
    (Seq[Option[String]], Option[CookieValueWithMeta], Source[ByteString, Any]),
    Any with AkkaStreams,
    Future
  ] =
    rootEndpoint.get
      .in("images")
      .in(paths)
      .out(
        streamBinaryBody(AkkaStreams)(CodecFormat.OctetStream())
      )
      .serverLogic(principal =>
        segments => Future.successful(loadResourceBusinessLogic(principal)(segments))
      )

  val uploadResourceEndpoint: PartialServerEndpoint[
    (Seq[Option[String]], Method, Option[String], Option[String]),
    ((Seq[Option[String]], Option[CookieValueWithMeta]), Session),
    (List[String], UploadResource),
    ResourceError,
    (Seq[Option[String]], Option[CookieValueWithMeta], ResourceResult),
    Any,
    Future
  ] =
    rootEndpoint
      .in(paths.description("URI of the resource"))
      .in(multipartBody[UploadResource].description("Multipart file to upload"))
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

  val addResourceEndpoint: Full[
    (Seq[Option[String]], Method, Option[String], Option[String]),
    ((Seq[Option[String]], Option[CookieValueWithMeta]), Session),
    (List[String], UploadResource),
    ResourceError,
    (Seq[Option[String]], Option[CookieValueWithMeta], ResourceResult),
    Any,
    Future
  ] =
    uploadResourceEndpoint.post
      .description("Add a resource")
      .serverLogic(principal => { case (segments, upload) =>
        uploadResource(principal._2, segments, upload.bytes, update = false) map {
          case Left(l)  => Left(l)
          case Right(r) => Right((principal._1._1, principal._1._2, r))
        }
      })

  val updateResourceEndpoint: Full[
    (Seq[Option[String]], Method, Option[String], Option[String]),
    ((Seq[Option[String]], Option[CookieValueWithMeta]), Session),
    (List[String], UploadResource),
    ResourceError,
    (Seq[Option[String]], Option[CookieValueWithMeta], ResourceResult),
    Any,
    Future
  ] =
    uploadResourceEndpoint.put
      .description("Update the resource")
      .serverLogic(principal => { case (segments, upload) =>
        uploadResource(principal._2, segments, upload.bytes, update = true) map {
          case Left(l)  => Left(l)
          case Right(r) => Right((principal._1._1, principal._1._2, r))
        }
      })

  val uploadImageEndpoint: PartialServerEndpoint[
    (Seq[Option[String]], Method, Option[String], Option[String]),
    ((Seq[Option[String]], Option[CookieValueWithMeta]), Session),
    (List[String], UploadImage),
    ResourceError,
    (Seq[Option[String]], Option[CookieValueWithMeta], ResourceResult),
    Any,
    Future
  ] =
    rootEndpoint
      .in("images")
      .in(paths.description("URI of the image"))
      .in(multipartBody[UploadImage].description("Multipart image to upload"))
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

  val addImageEndpoint: Full[
    (Seq[Option[String]], Method, Option[String], Option[String]),
    ((Seq[Option[String]], Option[CookieValueWithMeta]), Session),
    (List[String], UploadImage),
    ResourceError,
    (Seq[Option[String]], Option[CookieValueWithMeta], ResourceResult),
    Any,
    Future
  ] =
    uploadImageEndpoint.post
      .description("Add an image")
      .serverLogic(principal => { case (segments, upload) =>
        uploadResource(principal._2, segments, upload.bytes, update = false) map {
          case Left(l)  => Left(l)
          case Right(r) => Right((principal._1._1, principal._1._2, r))
        }
      })

  val updateImageEndpoint: Full[
    (Seq[Option[String]], Method, Option[String], Option[String]),
    ((Seq[Option[String]], Option[CookieValueWithMeta]), Session),
    (List[String], UploadImage),
    ResourceError,
    (Seq[Option[String]], Option[CookieValueWithMeta], ResourceResult),
    Any,
    Future
  ] =
    uploadImageEndpoint.put
      .description("Update the image")
      .serverLogic(principal => { case (segments, upload) =>
        uploadResource(principal._2, segments, upload.bytes, update = true) map {
          case Left(l)  => Left(l)
          case Right(r) => Right((principal._1._1, principal._1._2, r))
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

  val deleteResourceEndpoint: Full[
    (Seq[Option[String]], Method, Option[String], Option[String]),
    ((Seq[Option[String]], Option[CookieValueWithMeta]), Session),
    List[String],
    ResourceError,
    (Seq[Option[String]], Option[CookieValueWithMeta]),
    Any,
    Future
  ] =
    rootEndpoint
      .in(paths)
      .delete
      .serverLogic(principal =>
        segments =>
          deleteResourceBusinessLogic(principal._2, segments) map {
            case Left(l)  => Left(l)
            case Right(_) => Right((principal._1._1, principal._1._2))
          }
      )

  val deleteImageEndpoint: Full[
    (Seq[Option[String]], Method, Option[String], Option[String]),
    ((Seq[Option[String]], Option[CookieValueWithMeta]), Session),
    List[String],
    ResourceError,
    (Seq[Option[String]], Option[CookieValueWithMeta]),
    Any,
    Future
  ] =
    rootEndpoint
      .in("images")
      .in(paths)
      .delete
      .serverLogic(principal =>
        segments =>
          deleteResourceBusinessLogic(principal._2, segments) map {
            case Left(l)  => Left(l)
            case Right(_) => Right((principal._1._1, principal._1._2))
          }
      )

  def innerEndpoints: List[ServerEndpoint[Any with AkkaStreams, Future]] = List(
    libraryEndpoint,
    getResourceEndpoint,
    getImageEndpoint
  )

  override def endpoints: List[ServerEndpoint[Any with AkkaStreams, Future]] = List(
    libraryEndpoint,
    addImageEndpoint,
    updateImageEndpoint,
    getImageEndpoint,
    deleteImageEndpoint,
    addResourceEndpoint,
    updateResourceEndpoint,
    getResourceEndpoint,
    deleteResourceEndpoint
  )

  lazy val route: Route = apiRoute
}

trait Upload {
  def bytes: Array[Byte]
}

case class UploadResource(file: Part[Array[Byte]]) extends Upload {
  override lazy val bytes: Array[Byte] = file.body
}

case class UploadImage(picture: Part[Array[Byte]]) extends Upload {
  override lazy val bytes: Array[Byte] = picture.body
}
