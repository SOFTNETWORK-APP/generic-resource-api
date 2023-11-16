package app.softnetwork.resource.scalatest

import akka.actor.typed.ActorSystem
import app.softnetwork.api.server.ApiEndpoint
import app.softnetwork.persistence.schema.SchemaProvider
import app.softnetwork.resource.launch.{ResourceEndpoints, ResourceGuardian}
import app.softnetwork.resource.model.GenericResource
import app.softnetwork.session.CsrfCheck
import app.softnetwork.session.scalatest.{SessionEndpointsRoutes, SessionTestKit}
import app.softnetwork.session.service.SessionMaterials

trait ResourceEndpointsTestKit[Resource <: GenericResource]
    extends ResourceEndpoints[Resource]
    with SessionEndpointsRoutes {
  _: ResourceGuardian[Resource]
    with SchemaProvider
    with CsrfCheck
    with SessionTestKit
    with SessionMaterials =>

  override def endpoints: ActorSystem[_] => List[ApiEndpoint] =
    system =>
      List(
        sessionServiceEndpoints(system),
        resourceEndpoints(system)
      )
}
