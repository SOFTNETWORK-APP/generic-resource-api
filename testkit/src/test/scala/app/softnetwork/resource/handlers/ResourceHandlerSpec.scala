package app.softnetwork.resource.handlers

import akka.actor.typed.ActorSystem

import java.io.{ByteArrayInputStream, File}
import org.scalatest.wordspec.AnyWordSpecLike
import app.softnetwork.utils.HashTools
import app.softnetwork.resource.config.ResourceSettings.{BaseUrl, ImageSizes, ResourcePath}
import app.softnetwork.resource.message.ResourceEvents._
import app.softnetwork.resource.scalatest.ResourceToLocalFileSystemTestKit
import app.softnetwork.resource.spi.SizeOption
import app.softnetwork.resource.utils.ResourceTools
import app.softnetwork.session.config.Settings
import org.slf4j.{Logger, LoggerFactory}
import org.softnetwork.session.model.Session

import java.nio.file.{Files, Paths}
import scala.reflect.io.Directory

/** Created by smanciot on 27/04/2020.
  */
class ResourceHandlerSpec
    extends ResourceHandler
    with AnyWordSpecLike
    with ResourceToLocalFileSystemTestKit {

  lazy val log: Logger = LoggerFactory getLogger getClass.getName

  implicit lazy val system: ActorSystem[_] = typedSystem()

  override protected def sessionType: Session.SessionType =
    Settings.Session.SessionContinuityAndTransport

  var bytes: Array[Byte] = _

  var md5: String = _

  val uri: Option[String] = Some("/resources")

  override def beforeAll(): Unit = {
    super.beforeAll()
    val path =
      Paths.get(Thread.currentThread().getContextClassLoader.getResource("avatar.png").getPath)
    bytes = Files.readAllBytes(path)
    md5 = HashTools
      .hashStream(
        new ByteArrayInputStream(bytes)
      )
      .getOrElse("")
    val dir = new Directory(new File(resourceProvider.rootDir))
    dir.deleteRecursively()
  }

  "Resource handler" must {

    "create resource" in {
      createOrUpdateResource("create")
      assert(Files.exists(Paths.get(s"${resourceProvider.rootDir}${uri.getOrElse("")}/create")))
    }

    "update resource" in {
      createOrUpdateResource("update", update = true)
      assert(Files.exists(Paths.get(s"${resourceProvider.rootDir}${uri.getOrElse("")}/update")))
    }

    "load resource" in {
      createOrUpdateResource("load")
      assert(Files.exists(Paths.get(s"${resourceProvider.rootDir}${uri.getOrElse("")}/load")))
      val resource = loadResource("load")
      resource.md5 shouldBe md5
      for (size <- ImageSizes.values) {
        resourceProvider.loadResource("load", uri, None, Seq(SizeOption(size)): _*) match {
          case Some(_) =>
          case _       => fail()
        }
      }
    }

    "delete resource" in {
      createOrUpdateResource("delete")
      assert(Files.exists(Paths.get(s"${resourceProvider.rootDir}${uri.getOrElse("")}/delete")))
      deleteResource("delete")
      assert(!Files.exists(Paths.get(s"${resourceProvider.rootDir}${uri.getOrElse("")}/delete")))
    }

    "list resources" in {
      val resources = resourceProvider.listResources(uri.getOrElse("/"))
      assert(resources.nonEmpty)
      assert(resources.forall(!_.directory))
      val files = resources.map(_.name)
      assert(files.contains("create"))
      assert(files.contains("update"))
      assert(files.contains("load"))
      assert(!files.contains("delete"))
    }
  }

  "Resource tools" must {
    "compute resource uri" in {
      assert(
        ResourceTools.resourceUri(
          "first",
          "second",
          "third#forth"
        ) == s"$BaseUrl/$ResourcePath/first/second/third%23forth"
      )
    }
  }

  private[this] def createOrUpdateResource(
    entityId: String,
    update: Boolean = false
  ): ResourceEvent = {
    if (update) {
      updateResource(entityId, bytes, uri)
    } else {
      createResource(entityId, bytes, uri)
    }
  }
}
