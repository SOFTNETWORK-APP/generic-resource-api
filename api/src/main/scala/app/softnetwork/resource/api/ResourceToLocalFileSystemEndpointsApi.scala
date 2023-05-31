package app.softnetwork.resource.api

import akka.actor.typed.ActorSystem
import app.softnetwork.persistence.schema.SchemaProvider
import app.softnetwork.resource.launch.ResourceEndpoints
import app.softnetwork.resource.model.Resource
import app.softnetwork.resource.service.{
  LocalFileSystemResourceServiceEndpoints,
  ResourceServiceEndpoints
}
import com.softwaremill.session.CsrfCheck

trait ResourceToLocalFileSystemEndpointsApi
    extends ResourceToLocalFileSystemApi
    with ResourceEndpoints[Resource] { _: SchemaProvider with CsrfCheck =>

  override def resourceEndpoints: ActorSystem[_] => ResourceServiceEndpoints = system =>
    LocalFileSystemResourceServiceEndpoints(system, sessionEndpoints(system))
}
