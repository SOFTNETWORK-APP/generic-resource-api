package app.softnetwork.resource.api

import akka.actor.typed.ActorSystem
import app.softnetwork.persistence.schema.SchemaProvider
import app.softnetwork.resource.launch.ResourceRoutes
import app.softnetwork.resource.model.Resource
import app.softnetwork.resource.service.{LocalFileSystemResourceService, ResourceService}

trait ResourceToLocalFileSystemRoutesApi
    extends ResourceToLocalFileSystemApi
    with ResourceRoutes[Resource] { _: SchemaProvider =>

  override def resourceService: ActorSystem[_] => ResourceService = system =>
    LocalFileSystemResourceService(system, sessionService(system))
}
