package app.softnetwork.resource.scalatest

import akka.actor.testkit.typed.scaladsl.TestProbe
import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{ContentTypes, Multipart, StatusCodes}
import akka.http.scaladsl.server.Route
import app.softnetwork.api.server.config.ServerSettings.RootPath
import app.softnetwork.persistence.environment
import app.softnetwork.resource.config.ResourceSettings.{ResourceDirectory, ResourcePath}
import app.softnetwork.resource.message.ResourceEvents.{
  ResourceCreatedEvent,
  ResourceDeletedEvent,
  ResourceEvent,
  ResourceUpdatedEvent
}
import app.softnetwork.resource.model.Resource
import app.softnetwork.resource.service.{GenericResourceService, LocalFileSystemResourceService}
import app.softnetwork.session.scalatest.SessionServiceRoute
import org.scalatest.{Assertion, Suite}

import java.net.URLEncoder
import java.nio.file.{Files, Path, Paths}

trait ResourceToLocalFileSystemRouteTestKit
    extends GenericResourceRouteTestKit[Resource]
    with ResourceToLocalFileSystemTestKit { _: Suite =>

  override def resourceService: ActorSystem[_] => GenericResourceService = system =>
    LocalFileSystemResourceService(system)

  override def apiRoutes(system: ActorSystem[_]): Route =
    resourceService(system).route ~
    SessionServiceRoute(system).route

  val probe: TestProbe[ResourceEvent] = createTestProbe[ResourceEvent]()
  subscribeProbe(probe)

  val rootDir = s"$ResourceDirectory/$environment"

  def addResource(
    path: Path,
    name: String = "file",
    sessionId: String,
    uuid: String,
    uri: Option[String] = None,
    update: Boolean = false
  ): Assertion = {
    invalidateSession()
    createSession(sessionId)
    val sessionUuid = s"$sessionId#$uuid"
    val encodedSessionUuid = URLEncoder.encode(sessionUuid, "UTF-8")
    withCookies(
      (if (update) {
         Put(
           s"/$RootPath/$ResourcePath${uri.getOrElse("")}/$uuid",
           entity = Multipart.FormData
             .fromPath(
               name,
               ContentTypes.`application/octet-stream`,
               path,
               100000
             )
             .toEntity
         )
       } else {
         Post(
           s"/$RootPath/$ResourcePath${uri.getOrElse("")}/$uuid",
           entity = Multipart.FormData
             .fromPath(
               name,
               ContentTypes.`application/octet-stream`,
               path,
               100000
             )
             .toEntity
         )
       }).withHeaders(
        RawHeader("Content-Type", "application/x-www-form-urlencoded"),
        RawHeader("Content-Type", "multipart/form-data")
      )
    ) ~> routes ~> check {
      if (update) {
        status shouldEqual StatusCodes.OK
        probe.expectMessageType[ResourceUpdatedEvent]
      } else {
        status shouldEqual StatusCodes.Created
        probe.expectMessageType[ResourceCreatedEvent]
      }
      assert(Files.exists(Paths.get(s"$rootDir/$sessionUuid")))
      Get(s"/$RootPath/$ResourcePath${uri.getOrElse("")}/$encodedSessionUuid") ~> routes ~> check {
        status shouldEqual StatusCodes.OK
      }
    }
  }

  def deleteResource(sessionId: String, uuid: String, uri: Option[String] = None): Assertion = {
    invalidateSession()
    createSession(sessionId)
    val sessionUuid = s"$sessionId#$uuid"
    val encodedSessionUuid = URLEncoder.encode(sessionUuid, "UTF-8")
    withCookies(
      Delete(s"/$RootPath/$ResourcePath${uri.getOrElse("")}/$uuid")
    ) ~> routes ~> check {
      status shouldEqual StatusCodes.OK
      probe.expectMessageType[ResourceDeletedEvent]
      assert(!Files.exists(Paths.get(s"$rootDir/$sessionUuid")))
      Get(s"/$RootPath/$ResourcePath${uri.getOrElse("")}/$encodedSessionUuid") ~> routes ~> check {
        status shouldEqual StatusCodes.NotFound
      }
    }
  }

  def addImage(path: Path, sessionId: String, uuid: String, update: Boolean = false): Assertion = {
    addResource(path, "picture", sessionId, uuid, Some("/images"), update)
  }

  def getImage(uuid: String, size: Option[String] = None): Assertion = {
    val encodedUuid = URLEncoder.encode(uuid, "UTF-8")
    Get(
      s"/$RootPath/$ResourcePath/images/$encodedUuid${size.map("/" + _).getOrElse("")}"
    ) ~> routes ~> check {
      status shouldEqual StatusCodes.OK
    }
  }

  def deleteImage(sessionId: String, uuid: String): Assertion = {
    deleteResource(sessionId, uuid, Some("/images"))
  }
}
