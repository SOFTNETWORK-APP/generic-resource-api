package app.softnetwork.resource.api

import akka.actor.typed.ActorSystem
import app.softnetwork.persistence.launch.PersistentEntity
import app.softnetwork.persistence.launch.PersistenceGuardian._
import app.softnetwork.persistence.schema.SchemaProvider
import app.softnetwork.resource.launch.GenericResourceApplication
import app.softnetwork.resource.message.ResourceEvents.ResourceEvent
import app.softnetwork.resource.message.ResourceMessages.{ResourceCommand, ResourceResult}
import app.softnetwork.resource.model.Resource
import app.softnetwork.resource.persistence.typed.ResourceBehavior

trait ResourceApi extends GenericResourceApplication[Resource] { _: SchemaProvider =>

  override def resourceEntity
    : ActorSystem[_] => PersistentEntity[ResourceCommand, Resource, ResourceEvent, ResourceResult] =
    _ => ResourceBehavior
}
