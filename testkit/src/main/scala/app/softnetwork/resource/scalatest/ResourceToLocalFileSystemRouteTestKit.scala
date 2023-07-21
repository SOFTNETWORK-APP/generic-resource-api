package app.softnetwork.resource.scalatest

import akka.actor.testkit.typed.scaladsl.TestProbe
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{ContentTypes, Multipart, StatusCodes}
import app.softnetwork.api.server.ApiRoutes
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
import app.softnetwork.resource.spi.SimpleResource
import org.scalatest.Suite

import java.net.URLEncoder
import java.nio.file.{Files, Path, Paths}

trait ResourceToLocalFileSystemRouteTestKit
    extends ResourceRouteTestKit[Resource]
    with ResourceToLocalFileSystemTestKit { _: Suite with ApiRoutes =>

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
  ): Unit = {
    if (httpHeaders.isEmpty) {
      createSession(sessionId)
    }
    val sessionUuid = s"$sessionId#$uuid"
    val encodedSessionUuid = URLEncoder.encode(sessionUuid, "UTF-8")
    withHeaders(
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
      var strictUri = uri.getOrElse("")
      if (strictUri.startsWith("/images")) {
        strictUri = strictUri.substring(7)
      }
      val path = s"$rootDir$strictUri/$sessionUuid"
      logger.info(path)
      assert(Files.exists(Paths.get(path)))
      refreshSession(headers)
      withHeaders(
        Get(s"/$RootPath/$ResourcePath${uri.getOrElse("")}/$encodedSessionUuid")
      ) ~> routes ~> check {
        status shouldEqual StatusCodes.OK
        refreshSession(headers)
      }
    }
  }

  def deleteResource(sessionId: String, uuid: String, uri: Option[String] = None): Unit = {
    if (httpHeaders.isEmpty) {
      createSession(sessionId)
    }
    val sessionUuid = s"$sessionId#$uuid"
    val encodedSessionUuid = URLEncoder.encode(sessionUuid, "UTF-8")
    withHeaders(
      Delete(s"/$RootPath/$ResourcePath${uri.getOrElse("")}/$uuid")
    ) ~> routes ~> check {
      status shouldEqual StatusCodes.OK
      probe.expectMessageType[ResourceDeletedEvent]
      var strictUri = uri.getOrElse("")
      if (strictUri.startsWith("/images")) {
        strictUri = strictUri.substring(7)
      }
      val path = s"$rootDir$strictUri/$sessionUuid"
      logger.info(path)
      assert(!Files.exists(Paths.get(path)))
      refreshSession(headers)
      withHeaders(
        Get(s"/$RootPath/$ResourcePath${uri.getOrElse("")}/$encodedSessionUuid")
      ) ~> routes ~> check {
        status shouldEqual StatusCodes.NotFound
        refreshSession(headers)
      }
    }
  }

  def addImage(path: Path, sessionId: String, uuid: String, update: Boolean = false): Unit = {
    addResource(path, "picture", sessionId, uuid, Some("/images"), update)
  }

  def getImage(uuid: String, size: Option[String] = None): Unit = {
    val encodedUuid = URLEncoder.encode(uuid, "UTF-8")
    withHeaders(
      Get(
        s"/$RootPath/$ResourcePath/images/$encodedUuid${size.map("/" + _).getOrElse("")}"
      )
    ) ~> routes ~> check {
      status shouldEqual StatusCodes.OK
      refreshSession(headers)
    }
  }

  def deleteImage(sessionId: String, uuid: String): Unit = {
    deleteResource(sessionId, uuid, Some("/images"))
  }

  def listResources(uri: Option[String] = None): List[SimpleResource] = {
    import app.softnetwork.serialization._
    withHeaders(
      Get(s"/$RootPath/$ResourcePath/library${uri.getOrElse("")}")
    ) ~> routes ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[List[SimpleResource]]
    }
  }
}
