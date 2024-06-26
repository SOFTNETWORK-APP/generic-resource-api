package app.softnetwork.resource.spi

import app.softnetwork.persistence.environment
import app.softnetwork.resource.config.ResourceSettings.{
  BaseUrl,
  ImageSizes,
  LibraryDirectory,
  ResourceDirectory,
  ResourcePath
}
import app.softnetwork.utils.ImageTools.ImageSize
import app.softnetwork.utils.{Base64Tools, ImageTools}
import org.slf4j.Logger

import java.nio.file.{Files, LinkOption, Path, Paths}
import java.util.stream.Collectors
import scala.collection.JavaConverters._
import scala.util.matching.Regex
import scala.util.{Failure, Success, Try}

trait LocalFileSystemProvider extends ResourceProvider {

  def log: Logger

  lazy val rootDir = s"$ResourceDirectory/$environment"

  def strictUri(uri: String): String = uri.split("\\.\\.").mkString("").split("//").mkString("/")

  /** Upsert the underlying resource referenced by its uuid to the resource provider
    *
    * @param uuid
    *   - the uuid of the resource to upsert
    * @param data
    *   - the base64 encoded resource content
    * @param uri
    *   - the optional uri of the resource
    * @return
    *   whether the resource has been upserted or not
    */
  override def upsertResource(uuid: String, data: String, uri: Option[String] = None): Boolean = {
    Try {
      val dir = Paths.get(rootDir, uri.getOrElse(""))
      if (!Files.exists(dir)) {
        Try(Files.createDirectories(dir)) match {
          case Success(_) => log.info(s"$dir created successfully")
          case Failure(f) => log.error(s"$dir can not be created -> ${f.getMessage}", f)
        }
      }
      val decoded = Base64Tools.decodeBase64(data)
      val path = Paths.get(rootDir, uri.map(strictUri).getOrElse(""), uuid)
      val fos = Files.newOutputStream(path)
      fos.write(decoded)
      fos.close()
      if (ImageTools.isAnImage(path)) {
        ImageTools.generateImages(path, ImageSizes.values.toSeq)
      }
    } match {
      case Success(_) => true
      case Failure(f) =>
        log.error(f.getMessage, f)
        false
    }
  }

  /** @param uuid
    *   - the uuid of the resource to load
    * @param uri
    *   - the optional uri of the resource
    * @param content
    *   - the optional base64 encoded resource content
    * @param option
    *   - the list of resource options
    * @return
    *   the optional path associated with this resource
    */
  override def loadResource(
    uuid: String,
    uri: Option[String],
    content: Option[String],
    option: ResourceOption*
  ): Option[Path] = {
    val path = Paths.get(rootDir, uri.map(strictUri).getOrElse(""), uuid)
    if (Files.exists(path)) {
      if (ImageTools.isAnImage(path)) {
        val size: Option[ResourceOption] = option.find {
          case _: SizeOption => true
          case _             => false
        }
        size match {
          case Some(s) =>
            val imageSize: ImageSize = s.asInstanceOf[SizeOption].size
            val out = imageSize.resizedPath(path, None)
            if (Files.exists(out)) {
              Some(out)
            } else {
              Some(
                ImageTools.getImage(
                  path,
                  Option(imageSize)
                )
              )
            }
          case _ => Some(path)
        }
      } else {
        Some(path)
      }
    } else {
      content match {
        case Some(data) if upsertResource(uuid, data, uri) =>
          loadResource(uuid, uri, None, option: _*)
        case _ => None
      }
    }
  }

  /** Deletes the underlying document referenced by its uuid to the external system
    *
    * @param uuid
    *   - the uuid of the resource to delete
    * @param uri
    *   - the optional uri of the resource
    * @return
    *   whether the operation is successful or not
    */
  override def deleteResource(uuid: String, uri: Option[String] = None): Boolean = {
    Try {
      val dir = Paths.get(rootDir, uri.map(strictUri).getOrElse(""))
      val listFiles: List[Path] =
        Files
          .list(dir)
          .filter(Files.isRegularFile(_))
          .filter { file =>
            file.getFileName.toString.startsWith(uuid)
          }
          .collect(Collectors.toList[Path]())
          .asScala
          .toList
      listFiles.foreach(path => Files.delete(path))
    } match {
      case Success(_) => true
      case Failure(f) =>
        log.error(f.getMessage, f)
        false
    }
  }

  val generatedImage: Regex = ".*(\\.\\d*x\\d*).*".r

  /** @param uri
    *   - the uri from which resources have to be listed
    * @return
    *   the resources located at this uri
    */
  override def listResources(uri: String): List[SimpleResource] = {
    Try {
      val dir = Paths.get(rootDir, LibraryDirectory, strictUri(uri))
      if (!Files.exists(dir)) {
        Try(Files.createDirectories(dir)) match {
          case Success(_) => log.info(s"$dir created successfully")
          case Failure(f) => log.error(s"$dir can not be created -> ${f.getMessage}", f)
        }
      }
      Files
        .list(dir)
        .filter(path =>
          (Files
            .isRegularFile(path) && generatedImage.unapplySeq(path.getFileName.toString).isEmpty) ||
          Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)
        )
        .collect(Collectors.toList[Path]())
        .asScala
        .toList
    } match {
      case Success(files) =>
        files.map(file => {
          val directory = Files.isDirectory(file, LinkOption.NOFOLLOW_LINKS)
          val name: String = file.getFileName.toString
          val image: Boolean = !directory && ImageTools.isAnImage(file)
          val url: String = {
            if (directory) {
              s"$BaseUrl/$ResourcePath/library/$uri/$name"
            } else if (image) {
              val segments = (Seq(LibraryDirectory) ++ uri.split("/"))
                .flatMap(s => if (s.trim.isEmpty) None else Some(s))
              s"$BaseUrl/$ResourcePath/images/${segments.mkString("/")}/$name"
            } else {
              s"$BaseUrl/$ResourcePath/$uri/$name"
            }
          }
          SimpleResource(
            uri,
            name,
            directory,
            image,
            url
          )
        })
      case Failure(f) =>
        log.error(f.getMessage, f)
        List.empty
    }
  }
}
