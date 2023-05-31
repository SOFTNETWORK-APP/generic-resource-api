package app.softnetwork.resource

import app.softnetwork.resource.config.ResourceSettings.ImageSizes
import app.softnetwork.resource.spi.{ResourceOption, SizeOption}

import scala.language.implicitConversions

package object service {

  case class ResourceDetails(
    uuid: String,
    options: Seq[ResourceOption] = Seq.empty,
    uri: Option[String] = None
  )

  implicit def segmentsToResourceDetails(segments: List[String]): ResourceDetails = {
    if (segments.nonEmpty) {
      var uuid: String = segments.last
      var options: Seq[ResourceOption] = Seq.empty
      val uri: Option[String] =
        if (segments.size > 1) {
          Some(
            (ImageSizes.get(segments.last.toLowerCase()) match {
              case Some(value) =>
                options = Seq(SizeOption(value))
                uuid = segments(segments.size - 2)
                segments.dropRight(2)
              case _ =>
                segments.dropRight(1)
            }).mkString("/")
          )
        } else {
          None
        }
      ResourceDetails(uuid, options, uri)
    } else {
      ResourceDetails("")
    }
  }
}
