package app.softnetwork.resource.scalatest

import akka.actor.typed.ActorSystem
import app.softnetwork.api.server.ApiRoute
import app.softnetwork.persistence.schema.SchemaProvider
import app.softnetwork.resource.launch.ResourceRoutes
import app.softnetwork.resource.model.GenericResource
import app.softnetwork.session.scalatest.SessionServiceRoutes
import org.scalatest.Suite

trait ResourceRoutesTestKit[Resource <: GenericResource]
    extends ResourceRoutes[Resource]
    with SessionServiceRoutes { _: Suite with SchemaProvider =>

  override def apiRoutes: ActorSystem[_] => List[ApiRoute] =
    system =>
      List(
        sessionServiceRoute(system),
        resourceService(system)
      )

}
