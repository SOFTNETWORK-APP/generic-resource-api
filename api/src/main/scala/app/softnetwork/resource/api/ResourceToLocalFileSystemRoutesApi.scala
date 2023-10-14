package app.softnetwork.resource.api

import akka.actor.typed.ActorSystem
import app.softnetwork.api.server.ApiRoute
import app.softnetwork.persistence.schema.SchemaProvider
import app.softnetwork.resource.launch.ResourceRoutes
import app.softnetwork.resource.model.Resource
import app.softnetwork.resource.service.{LocalFileSystemResourceService, ResourceService}
import app.softnetwork.session.CsrfCheckHeader

trait ResourceToLocalFileSystemRoutesApi
    extends ResourceToLocalFileSystemApi
    with ResourceRoutes[Resource]
    with CsrfCheckHeader { _: SchemaProvider =>

  override def resourceService: ActorSystem[_] => ResourceService = system =>
    LocalFileSystemResourceService(system, sessionService(system))

  override def apiRoutes: ActorSystem[_] => List[ApiRoute] = system =>
    super.apiRoutes(system) :+ resourceSwagger(system)
}
