package app.softnetwork.resource.service

import app.softnetwork.api.server.ApiRoutes
import app.softnetwork.resource.scalatest.ResourceToLocalFileSystemRouteTestKit
import app.softnetwork.session.model.{SessionData, SessionDataDecorator}
import app.softnetwork.session.service.SessionMaterials
import org.scalatest.Suite
import org.scalatest.wordspec.AnyWordSpecLike
import org.slf4j.{Logger, LoggerFactory}

import java.io.File
import java.nio.file.{Path, Paths}
import scala.reflect.io.Directory

trait ResourceServiceSpec[SD <: SessionData with SessionDataDecorator[SD]]
    extends AnyWordSpecLike
    with ResourceToLocalFileSystemRouteTestKit[SD] {
  _: Suite with ApiRoutes with SessionMaterials[SD] =>

  override val refreshableSession: Boolean = false

  lazy val log: Logger = LoggerFactory getLogger getClass.getName

  val sessionId = "session"

  val resourceUuid = "resource"

  val imageUuid = "image"

  val path: Path =
    Paths.get(Thread.currentThread().getContextClassLoader.getResource("avatar.png").getPath)

  override def beforeAll(): Unit = {
    super.beforeAll()
    // pre load routes
    apiRoutes(typedSystem())
    val dir = new Directory(new File(rootDir))
    dir.deleteRecursively()
  }

  "Resource service" must {

    "add resource" in {
      addResource(path, "file", sessionId, resourceUuid)
    }

    "update resource" in {
      addResource(path, "file", sessionId, resourceUuid, update = true)
    }

    "delete resource" in {
      deleteResource(sessionId, resourceUuid)
    }

    "add image" in {
      addImage(path, sessionId, imageUuid)
    }

    "update image" in {
      addImage(path, sessionId, imageUuid, update = true)
    }

    "get icon image" in {
      getImage(s"$sessionId#$imageUuid", Some("icon"))
    }

    "get small image" in {
      getImage(s"$sessionId#$imageUuid", Some("small"))
    }

    "get medium image" in {
      getImage(s"$sessionId#$imageUuid", Some("medium"))
    }

    "get large image" in {
      getImage(s"$sessionId#$imageUuid", Some("large"))
    }

    "delete image" in {
      deleteImage(sessionId, imageUuid)
    }

    "list resources" in {
      addResource(path, "file", sessionId, resourceUuid, Some("/library/sample"))
      assert(listResources(Some("/sample")).nonEmpty)
    }
  }
}
