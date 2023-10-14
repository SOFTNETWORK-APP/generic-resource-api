package app.softnetwork.resource.api

import akka.actor.typed.ActorSystem
import app.softnetwork.api.server.Endpoint
import app.softnetwork.persistence.schema.SchemaProvider
import app.softnetwork.resource.launch.ResourceEndpoints
import app.softnetwork.resource.model.Resource
import app.softnetwork.resource.service.{
  LocalFileSystemResourceServiceEndpoints,
  ResourceServiceEndpoints
}
import app.softnetwork.session.CsrfCheck

trait ResourceToLocalFileSystemEndpointsApi
    extends ResourceToLocalFileSystemApi
    with ResourceEndpoints[Resource] { _: SchemaProvider with CsrfCheck =>

  override def resourceEndpoints: ActorSystem[_] => ResourceServiceEndpoints = system =>
    LocalFileSystemResourceServiceEndpoints(system, sessionEndpoints(system))

  override def endpoints: ActorSystem[_] => List[Endpoint] = system =>
    super.endpoints(system) :+ resourceSwagger(system)
}
