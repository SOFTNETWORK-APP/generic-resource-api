package app.softnetwork.resource.service

import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import akka.util.ByteString
import app.softnetwork.api.server._
import app.softnetwork.resource.config.ResourceSettings
import app.softnetwork.resource.handlers.GenericResourceHandler
import app.softnetwork.resource.message.ResourceMessages._
import com.softwaremill.session.CsrfDirectives._
import com.softwaremill.session.CsrfOptions._
import org.softnetwork.session.model.Session._
import app.softnetwork.resource.spi._
import app.softnetwork.serialization.commonFormats
import app.softnetwork.session.service.SessionService
import com.typesafe.scalalogging.StrictLogging
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.json4s.{jackson, Formats}
import org.json4s.jackson.Serialization
import org.softnetwork.session.model.Session

/** Created by smanciot on 13/05/2020.
  */
trait ResourceService
    extends Directives
    with DefaultComplete
    with Json4sSupport
    with StrictLogging
    with LoadResourceService
    with ApiRoute {
  _: GenericResourceHandler with ResourceProvider =>

  implicit def serialization: Serialization.type = jackson.Serialization

  implicit def formats: Formats = commonFormats

  def sessionService: SessionService

  val route: Route = {
    pathPrefix(ResourceSettings.ResourcePath) {
      library ~ images ~ resource()
    }
  }

  lazy val images: Route = {
    pathPrefix("images") {
      resource("picture")
    }
  }

  lazy val library: Route = {
    pathPrefix("library") {
      path(Segments(1, 128)) { segments =>
        get {
          complete(HttpResponse(StatusCodes.OK, entity = listResources(segments.mkString("/"))))
        }
      }
    }
  }

  def resource(fieldName: String = "file"): Route = {
    path(Segments(1, 128)) { segments =>
      val resourceDetails: ResourceDetails = segments
      import resourceDetails._
      get {
        loadResource(resourceDetails) match {
          case Some((path, _)) => getFromFile(path.toFile)
          case _               => complete(HttpResponse(StatusCodes.NotFound))
        }
      } ~
      // check anti CSRF token
      hmacTokenCsrfProtection(checkHeader) {
        // check if a session exists
        sessionService.requiredSession { session =>
          post {
            extractRequestContext { ctx =>
              implicit val materializer: Materializer = ctx.materializer
              fileUpload(fieldName) {
                case (_, byteSource) =>
                  completeResource(session, resourceDetails, byteSource, update = false)
                case _ => complete(HttpResponse(StatusCodes.BadRequest))
              }
            }
          } ~
          put {
            extractRequestContext { ctx =>
              implicit val materializer: Materializer = ctx.materializer
              fileUpload(fieldName) {
                case (_, byteSource) =>
                  completeResource(session, resourceDetails, byteSource, update = true)
                case _ => complete(HttpResponse(StatusCodes.BadRequest))
              }
            }
          } ~
          delete {
            run(s"${session.id}#$uuid", DeleteResource(s"${session.id}#$uuid")) completeWith {
              case ResourceDeleted  => complete(HttpResponse(StatusCodes.OK))
              case ResourceNotFound => complete(HttpResponse(StatusCodes.NotFound))
              case r: ResourceError => complete(HttpResponse(StatusCodes.BadRequest, entity = r))
              case _                => complete(HttpResponse(StatusCodes.BadRequest))
            }
          }
        }
      }
    }
  }

  protected def completeResource(
    session: Session,
    resourceDetails: ResourceDetails,
    byteSource: Source[ByteString, Any],
    update: Boolean
  )(implicit materializer: Materializer): Route = {
    val future =
      byteSource.map { s => s.toArray }.runFold(Array[Byte]()) { (acc, bytes) => acc ++ bytes }
    onSuccess(future) { bytes =>
      uploadResource(session, resourceDetails, bytes, update) completeWith {
        case Right(r) =>
          r match {
            case ResourceCreated => complete(StatusCodes.Created)
            case ResourceUpdated => complete(StatusCodes.OK)
          }
        case Left(l) =>
          l match {
            case r: ResourceError => complete(HttpResponse(StatusCodes.BadRequest, entity = r))
            case _                => complete(HttpResponse(StatusCodes.BadRequest))
          }
      }
    }
  }

}
