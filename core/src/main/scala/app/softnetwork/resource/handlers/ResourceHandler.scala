package app.softnetwork.resource.handlers

import akka.actor.typed.ActorSystem
import akka.cluster.sharding.typed.scaladsl.EntityTypeKey
import app.softnetwork.persistence.typed.scaladsl.EntityPattern
import app.softnetwork.resource.message.ResourceMessages._
import app.softnetwork.resource.persistence.typed.ResourceBehavior
import app.softnetwork.persistence.typed.CommandTypeKey
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.Future
import scala.language.implicitConversions
import scala.reflect.ClassTag

/** Created by smanciot on 30/04/2020.
  */
trait ResourceTypeKey extends CommandTypeKey[ResourceCommand] {
  override def TypeKey(implicit tTag: ClassTag[ResourceCommand]): EntityTypeKey[ResourceCommand] =
    ResourceBehavior.TypeKey
}

trait GenericResourceHandler extends EntityPattern[ResourceCommand, ResourceResult] {
  _: CommandTypeKey[ResourceCommand] =>
}

trait ResourceHandler extends GenericResourceHandler with ResourceTypeKey

trait ResourceDao extends ResourceHandler {
  def createResource(entityId: String, bytes: Array[Byte], uri: Option[String])(implicit
    system: ActorSystem[_]
  ): Future[ResourceResult] = {
    ?(
      entityId,
      CreateResource(entityId, bytes, uri)
    )
  }

  def updateResource(entityId: String, bytes: Array[Byte], uri: Option[String])(implicit
    system: ActorSystem[_]
  ): Future[ResourceResult] = {
    ?(
      entityId,
      UpdateResource(entityId, bytes, uri)
    )
  }

  def deleteResource(entityId: String)(implicit system: ActorSystem[_]): Future[ResourceResult] = {
    ?(
      entityId,
      DeleteResource(entityId)
    )
  }

  def loadResource(entityId: String)(implicit system: ActorSystem[_]): Future[ResourceResult] = {
    ?(
      entityId,
      LoadResource(entityId)
    )
  }
}

object ResourceDao extends ResourceDao {
  lazy val log: Logger = LoggerFactory.getLogger(getClass.getName)
}
