package app.softnetwork.resource.service

import app.softnetwork.resource.scalatest.ResourceToLocalFileSystemRouteTestKit
import org.scalatest.wordspec.AnyWordSpecLike

import java.io.File
import java.nio.file.{Path, Paths}
import scala.reflect.io.Directory

class ResourceServiceSpec extends AnyWordSpecLike with ResourceToLocalFileSystemRouteTestKit {

  val sessionId = "session"

  val resourceUuid = "resource"

  val imageUuid = "image"

  val path: Path =
    Paths.get(Thread.currentThread().getContextClassLoader.getResource("avatar.png").getPath)

  override def beforeAll(): Unit = {
    super.beforeAll()
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

  }
}
