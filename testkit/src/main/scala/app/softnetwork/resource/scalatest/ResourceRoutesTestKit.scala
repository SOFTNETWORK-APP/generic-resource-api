package app.softnetwork.resource.scalatest

import akka.actor.typed.ActorSystem
import app.softnetwork.api.server.ApiRoute
import app.softnetwork.persistence.schema.SchemaProvider
import app.softnetwork.resource.launch.{ResourceGuardian, ResourceRoutes}
import app.softnetwork.resource.model.GenericResource
import app.softnetwork.session.model.{SessionData, SessionDataDecorator}
import app.softnetwork.session.scalatest.{SessionServiceRoutes, SessionTestKit}
import app.softnetwork.session.service.SessionMaterials
import org.scalatest.Suite

trait ResourceRoutesTestKit[SD <: SessionData with SessionDataDecorator[
  SD
], Resource <: GenericResource]
    extends ResourceRoutes[SD, Resource]
    with SessionServiceRoutes[SD] {
  _: Suite
    with ResourceGuardian[Resource]
    with SchemaProvider
    with SessionTestKit[SD]
    with SessionMaterials[SD] =>

  override def apiRoutes: ActorSystem[_] => List[ApiRoute] =
    system =>
      List(
        sessionServiceRoute(system),
        resourceService(system)
      )

}
