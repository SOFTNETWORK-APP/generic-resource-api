package app.softnetwork.resource.scalatest

import akka.actor.typed.ActorSystem
import app.softnetwork.api.server.ApiEndpoint
import app.softnetwork.persistence.schema.SchemaProvider
import app.softnetwork.resource.launch.ResourceEndpoints
import app.softnetwork.resource.model.GenericResource
import app.softnetwork.session.scalatest.SessionEndpointsRoutes
import com.softwaremill.session.CsrfCheck

trait ResourceEndpointsTestKit[Resource <: GenericResource]
    extends ResourceEndpoints[Resource]
    with SessionEndpointsRoutes { _: SchemaProvider with CsrfCheck =>

  override def endpoints: ActorSystem[_] => List[ApiEndpoint] =
    system =>
      List(
        sessionServiceEndpoints(system),
        resourceEndpoints(system)
      )
}
